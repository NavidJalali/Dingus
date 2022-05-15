package io.navidjalali.dingus

import zio.ZIO
import zio.stream.{ZPipeline, ZStream}

import java.net.http.HttpResponse
import java.nio.ByteBuffer
import java.util.concurrent.Flow
import scala.jdk.CollectionConverters.{CollectionHasAsScala, MapHasAsScala}

final case class Response(
  statusCode: StatusCode,
  headers: Set[Header],
  data: Flow.Publisher[java.util.List[ByteBuffer]]
) {
  def byteStream: ZStream[Any, Throwable, Byte] = ZStreamBody.toStream(data, 128)
  def bodyAsString: ZIO[Any, Throwable, String] = byteStream.via(ZPipeline.utf8Decode).mkString
}

object Response {
  def fromJava(javaResponse: HttpResponse[Flow.Publisher[java.util.List[ByteBuffer]]]): Response =
    Response(
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
