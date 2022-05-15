package io.navidjalali.dingus

import zio.{Cause, Chunk, ChunkBuilder, Exit, Promise, Queue, Ref, Scope, URIO, ZIO}
import zio.stream.{ZSink, ZStream}
import zio.stream.ZStream.Pull

import java.nio.ByteBuffer
import java.util
import java.util.concurrent.Flow.{Publisher, Subscriber, Subscription}
import scala.annotation.tailrec
import scala.jdk.CollectionConverters.CollectionHasAsScala

object ReactiveAdapters {
  def toStream(publisher: Publisher[util.List[ByteBuffer]], bufferSize: Int): ZStream[Any, Throwable, Byte] = {
    val pullOrFail =
      for {
        subscriberP    <- makeSubscriber[util.List[ByteBuffer]](bufferSize)
        (subscriber, p) = subscriberP
        _              <- ZIO.succeed(publisher.subscribe(subscriber))
        subQ           <- p.await
        (sub, q)        = subQ
        process <- process(q, sub).map(_.map(_.map { jList =>
                     jList.asScala.flatMap { buffer =>
                       val arr = Array.fill(buffer.remaining())(null.asInstanceOf[Byte])
                       buffer.get(arr)
                       arr
                     }
                   }))
      } yield process
    val pull = pullOrFail.catchAll(e => ZIO.succeed(ZIO.fail(Some(e))))
    ZStream.fromPull(pull).flatMap(ZStream.fromIterable(_))
  }

  def toPublisher[R, E <: Throwable, O](stream: ZStream[R, E, O]): ZIO[R, Nothing, Publisher[O]] =
    ZIO.runtime.map { runtime =>
      val publisher: Publisher[O] = subscriber => {
        if (subscriber == null) {
          throw new NullPointerException("Subscriber cannot be null.")
        } else {
          runtime.unsafeRunAsync(
            for {
              demand <- Queue.unbounded[Long]
              _      <- ZIO.succeed(subscriber.onSubscribe(createSubscription(subscriber, demand, runtime)))
              _ <- stream
                     .run(demandUnfoldSink(subscriber, demand))
                     .catchAll(e => ZIO.succeed(subscriber.onError(e)))
                     .forkDaemon
            } yield ()
          )
        }
      }
      publisher
    }

  def demandUnfoldSink[I](
    subscriber: Subscriber[_ >: I],
    demand: Queue[Long]
  ): ZSink[Any, Nothing, I, I, Unit] =
    ZSink
      .foldChunksZIO[Any, Nothing, I, Long](0L)(_ >= 0L) { (bufferedDemand, chunk) =>
        ZIO
          .iterate((chunk, bufferedDemand))(!_._1.isEmpty) { case (chunk, bufferedDemand) =>
            demand.isShutdown.flatMap {
              case true => ZIO.succeed((Chunk.empty, -1))
              case false =>
                if (chunk.size.toLong <= bufferedDemand)
                  ZIO
                    .foreach(chunk)(a => ZIO.succeed(subscriber.onNext(a)))
                    .as((Chunk.empty, bufferedDemand - chunk.size.toLong))
                else
                  ZIO.foreach(chunk.take(bufferedDemand.toInt))(a => ZIO.succeed(subscriber.onNext(a))) *>
                    demand.take.map((chunk.drop(bufferedDemand.toInt), _))
            }
          }
          .map(_._2)
      }
      .mapZIO(_ => demand.isShutdown.flatMap(is => ZIO.succeed(subscriber.onComplete()).when(!is).unit))

  def createSubscription[A](
    subscriber: Subscriber[_ >: A],
    demand: Queue[Long],
    runtime: zio.Runtime[_]
  ): Subscription =
    new Subscription {
      override def request(n: Long): Unit = {
        if (n <= 0) subscriber.onError(new IllegalArgumentException("non-positive subscription request"))
        runtime.unsafeRunAsync(demand.offer(n).unit)
      }
      override def cancel(): Unit = runtime.unsafeRun(demand.shutdown)
    }

  private def process[R, A](
    q: Queue[Exit[Option[Throwable], A]],
    sub: Subscription
  ): ZIO[Scope, Nothing, ZIO[Any, Option[Throwable], Chunk[A]]] = {
    val capacity = q.capacity.toLong - 1 // leave space for End or Fail
    for {
      _         <- ZIO.succeed(sub.request(capacity))
      requested <- Ref.Synchronized.make(capacity)
      lastP     <- Promise.make[Option[Throwable], Chunk[A]]
    } yield {

      @tailrec
      def takesToPull(
        builder: ChunkBuilder[A] = ChunkBuilder.make[A]()
      )(takes: List[Exit[Option[Throwable], A]]): Pull[Any, Throwable, A] =
        takes match {
          case Exit.Success(a) :: tail =>
            builder += a
            takesToPull(builder)(tail)
          case Exit.Failure(cause) :: _ =>
            val chunk = builder.result()
            val pull = Cause.flipCauseOption(cause) match {
              case Some(cause) => ZIO.failCause(cause.map(Some(_)))
              case None        => ZIO.fail(None)
            }
            if (chunk.isEmpty) pull else lastP.complete(pull) *> ZIO.succeed(chunk)
          case Nil =>
            val chunk = builder.result()
            val pull  = ZIO.succeed(chunk)

            if (chunk.isEmpty) pull
            else {
              val chunkSize = chunk.size
              val request =
                requested.getAndUpdateZIO {
                  case `chunkSize` => ZIO.succeed(sub.request(capacity)).as(capacity)
                  case n           => ZIO.succeed(n - chunkSize)
                }
              request *> pull
            }
        }

      lastP.isDone.flatMap {
        case true  => lastP.await
        case false => q.takeBetween(1, q.capacity).map(_.toList).flatMap(takesToPull())
      }

    }
  }

  private def makeSubscriber[A](
    capacity: Int
  ): URIO[Scope, (Subscriber[A], Promise[Throwable, (Subscription, Queue[Exit[Option[Throwable], A]])])] =
    for {
      q <- ZIO.acquireRelease(Queue.bounded[Exit[Option[Throwable], A]](capacity))(_.shutdown)
      p <- ZIO.acquireRelease(Promise.make[Throwable, (Subscription, Queue[Exit[Option[Throwable], A]])])(p =>
             p.poll.flatMap(_.fold(ZIO.unit)(_.foldZIO(_ => ZIO.unit, { case (sub, _) => ZIO.succeed(sub.cancel()) })))
           )
      runtime <- ZIO.runtime[Any]
    } yield {

      val subscriber =
        new Subscriber[A] {
          override def onSubscribe(s: Subscription): Unit =
            if (s == null) {
              val e = new NullPointerException("s was null in onSubscribe")
              runtime.unsafeRun(p.fail(e))
              throw e
            } else {
              runtime.unsafeRun(p.succeed((s, q)).flatMap {
                // `whenZIO(q.isShutdown)`, the Stream has been interrupted or completed before we received `onSubscribe`
                case true  => ZIO.succeed(s.cancel()).whenZIO(q.isShutdown)
                case false => ZIO.succeed(s.cancel())
              })
            }

          override def onNext(t: A): Unit =
            if (t == null) {
              val e = new NullPointerException("t was null in onNext")
              runtime.unsafeRunSync(q.offer(Exit.fail(Some(e))))
              throw e
            } else {
              runtime.unsafeRunSync(q.offer(Exit.succeed(t)))
              ()
            }

          override def onError(e: Throwable): Unit =
            if (e == null) {
              val e = new NullPointerException("throwable was null in onError")
              runtime.unsafeRunSync(q.offer(Exit.fail(Some(e))))
              throw e
            } else {
              runtime.unsafeRunSync(q.offer(Exit.fail(Some(e))))
              ()
            }

          override def onComplete(): Unit = {
            runtime.unsafeRunSync(q.offer(Exit.fail(None)))
            ()
          }
        }
      (subscriber, p)
    }
}
