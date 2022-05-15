package io.navidjalali.dingus

import zio.{Cause, Chunk, ChunkBuilder, Exit, Promise, Queue, Ref, Scope, URIO, ZIO}
import zio.stream.ZStream
import zio.stream.ZStream.Pull

import java.nio.ByteBuffer
import java.util
import java.util.concurrent.Flow.{Publisher, Subscriber, Subscription}
import scala.annotation.tailrec
import scala.jdk.CollectionConverters.CollectionHasAsScala

object ZStreamBody {
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
              val e = new NullPointerException("t was null in onError")
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
