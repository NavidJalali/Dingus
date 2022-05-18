package io.navidjalali.dingus

import zio.{Chunk, UIO, ZIO}
import zio.stream.ZStream

import java.net.http.HttpRequest.{BodyPublisher, BodyPublishers}
import java.nio.ByteBuffer

sealed trait RequestBody {
  def toJava: UIO[BodyPublisher]
}

object RequestBody {
  private[this] final case class Pure(publisher: BodyPublisher) extends RequestBody {
    override def toJava: UIO[BodyPublisher] = ZIO.succeed(publisher)
  }

  private[this] final case class Stream(stream: ZStream[Any, Throwable, Byte]) extends RequestBody {
    override def toJava: UIO[BodyPublisher] =
      ReactiveAdapters
        .toPublisher(stream.chunks.map(chunk => ByteBuffer.wrap(chunk.toArray)))
        .map(BodyPublishers.fromPublisher)
  }

  def fromString(body: => String): RequestBody =
    Pure(BodyPublishers.ofString(body))

  def fromArray(body: => Array[Byte]): RequestBody =
    Pure(BodyPublishers.ofByteArray(body))

  def fromChunk(chunk: => Chunk[Byte]): RequestBody =
    Pure(BodyPublishers.ofByteArray(chunk.toArray))

  def fromIterable(body: => Iterable[Byte]): RequestBody =
    Pure(BodyPublishers.ofByteArray(body.toArray))

  def fromBodyPublisher(body: => BodyPublisher): RequestBody =
    Pure(body)

  def fromStream(body: => ZStream[Any, Throwable, Byte]): RequestBody =
    Stream(body)

  def fromFile(file: => java.io.File, chunkSize: => Int = ZStream.DefaultChunkSize): RequestBody =
    fromStream(ZStream.fromFile(file, chunkSize))

  def fromFileName(path: => String, chunkSize: => Int = ZStream.DefaultChunkSize): RequestBody =
    fromStream(ZStream.fromFileName(path, chunkSize))

  def fromFileURI(uri: => java.net.URI, chunkSize: => Int = ZStream.DefaultChunkSize): RequestBody =
    fromStream(ZStream.fromFileURI(uri, chunkSize))
}
