package io.navidjalali.dingus

import zio.ZIO
import zio.stream.{ZPipeline, ZStream}

import java.net.http.{HttpResponse => JHttpResponse}
import java.nio.ByteBuffer
import java.util.concurrent.Flow
import scala.jdk.CollectionConverters.{CollectionHasAsScala, MapHasAsScala}

final case class HttpResponse(
  statusCode: StatusCode,
  headers: Set[Header],
  data: Flow.Publisher[java.util.List[ByteBuffer]]
) {
  def byteStream(bufferSize: => Int = HttpResponse.defaultBufferSize): ZStream[Any, Throwable, Byte] =
    ReactiveAdapters.toStream(data, bufferSize)
  def bodyAsString: ZIO[Any, Throwable, String] = byteStream().via(ZPipeline.utf8Decode).mkString
}

object HttpResponse {

  final val defaultBufferSize: Int = 4096

  def fromJava(javaResponse: JHttpResponse[Flow.Publisher[java.util.List[ByteBuffer]]]): HttpResponse =
    HttpResponse(
      StatusCode(javaResponse.statusCode)
        .getOrElse(throw new IllegalArgumentException(s"Invalid status code ${javaResponse.statusCode}")),
      javaResponse
        .headers()
        .map()
        .asScala
        .flatMap { case (key, values) =>
          values.asScala.map(value => Header(key, value))
        }
        .toSet,
      javaResponse.body()
    )
}
