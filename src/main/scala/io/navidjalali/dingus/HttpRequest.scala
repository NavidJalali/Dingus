package io.navidjalali.dingus

import zio.{UIO, ZIO}

import java.net.http.{HttpRequest => JHttpRequest}
import scala.concurrent.duration.{DurationInt, FiniteDuration}
import scala.jdk.DurationConverters.ScalaDurationOps

sealed trait HttpRequest {
  val url: URL
  val method: HttpMethod
  val headers: Set[Header]
  def asJava: UIO[JHttpRequest]
}

object HttpRequest {
  final case class GET(
    url: URL,
    headers: Set[Header] = Set.empty,
    version: HttpVersion = HttpVersion.`1.1`,
    timeout: FiniteDuration = 10.seconds
  ) extends HttpRequest {
    override val method: HttpMethod = HttpMethod.GET

    override def asJava: UIO[JHttpRequest] = ZIO.succeed({
      var builder =
        JHttpRequest
          .newBuilder()
          .uri(url.asJava)
          .GET()
          .timeout(timeout.toJava)
          .version(version.asJava)

      if (headers.nonEmpty) {
        builder = builder.headers(headers.toArray.flatMap(header => Array(header.name, header.value)): _*)
      }

      builder.build()
    })
  }

  final case class POST(
    url: URL,
    body: RequestBody,
    headers: Set[Header] = Set.empty,
    version: HttpVersion = HttpVersion.`1.1`,
    timeout: FiniteDuration = 10.seconds
  ) extends HttpRequest {
    override val method: HttpMethod = HttpMethod.POST

    override def asJava: UIO[JHttpRequest] = body.toJava.map { requestBody =>
      var builder =
        JHttpRequest
          .newBuilder()
          .uri(url.asJava)
          .POST(requestBody)
          .timeout(timeout.toJava)
          .version(version.asJava)

      if (headers.nonEmpty) {
        builder = builder.headers(headers.toArray.flatMap(header => Array(header.name, header.value)): _*)
      }

      builder.build()
    }
  }

  final case class PUT(
    url: URL,
    body: RequestBody,
    headers: Set[Header] = Set.empty,
    version: HttpVersion = HttpVersion.`1.1`,
    timeout: FiniteDuration = 10.seconds
  ) extends HttpRequest {
    override val method: HttpMethod = HttpMethod.PUT

    override def asJava: UIO[JHttpRequest] = body.toJava.map { requestBody =>
      var builder =
        JHttpRequest
          .newBuilder()
          .uri(url.asJava)
          .PUT(requestBody)
          .timeout(timeout.toJava)
          .version(version.asJava)

      if (headers.nonEmpty) {
        builder = builder.headers(headers.toArray.flatMap(header => Array(header.name, header.value)): _*)
      }

      builder.build()
    }
  }
}
