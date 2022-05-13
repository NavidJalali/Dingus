package io.navidjalali.dingus

import zio.ZIO
import zio.stream.ZStream

import java.net.http.HttpResponse
import scala.jdk.CollectionConverters.{CollectionHasAsScala, MapHasAsScala}

final case class Response(statusCode: StatusCode, headers: Set[Header], data: ZStream[Any, Throwable, String]) {
  def bodyAsString: ZIO[Any, Throwable, String] = data.runFold("")(_ + _)
}

object Response {
  def fromJava(javaResponse: HttpResponse[java.util.stream.Stream[String]]): Response =
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
      ZStream.fromJavaStream(javaResponse.body)
    )
}
