package io.navidjalali.dingus

import zio.{UIO, ZIO}

import java.net.http.HttpRequest.BodyPublishers
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
  private[this] final case class GET(
    url: URL,
    headers: Set[Header],
    version: HttpVersion,
    timeout: FiniteDuration
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

  private[this] final case class POST(
    url: URL,
    body: RequestBody,
    headers: Set[Header],
    version: HttpVersion,
    timeout: FiniteDuration
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

  private[this] final case class PUT(
    url: URL,
    body: RequestBody,
    headers: Set[Header],
    version: HttpVersion,
    timeout: FiniteDuration
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

  private[this] final case class DELETE(
    url: URL,
    headers: Set[Header],
    version: HttpVersion,
    timeout: FiniteDuration
  ) extends HttpRequest {
    override val method: HttpMethod = HttpMethod.DELETE

    override def asJava: UIO[JHttpRequest] = ZIO.succeed({
      var builder =
        JHttpRequest
          .newBuilder()
          .uri(url.asJava)
          .DELETE()
          .timeout(timeout.toJava)
          .version(version.asJava)

      if (headers.nonEmpty) {
        builder = builder.headers(headers.toArray.flatMap(header => Array(header.name, header.value)): _*)
      }

      builder.build()
    })
  }

  private[this] final case class HEAD(
    url: URL,
    headers: Set[Header],
    version: HttpVersion,
    timeout: FiniteDuration
  ) extends HttpRequest {
    override val method: HttpMethod = HttpMethod.HEAD

    override def asJava: UIO[JHttpRequest] = ZIO.succeed({
      var builder =
        JHttpRequest
          .newBuilder()
          .uri(url.asJava)
          .method("HEAD", BodyPublishers.noBody())
          .timeout(timeout.toJava)
          .version(version.asJava)

      if (headers.nonEmpty) {
        builder = builder.headers(headers.toArray.flatMap(header => Array(header.name, header.value)): _*)
      }

      builder.build()
    })
  }

  private[this] final case class OPTIONS(
    url: URL,
    headers: Set[Header],
    version: HttpVersion,
    timeout: FiniteDuration
  ) extends HttpRequest {
    override val method: HttpMethod = HttpMethod.OPTIONS

    override def asJava: UIO[JHttpRequest] = ZIO.succeed({
      var builder =
        JHttpRequest
          .newBuilder()
          .uri(url.asJava)
          .timeout(timeout.toJava)
          .version(version.asJava)
          .method("OPTIONS", BodyPublishers.noBody())

      if (headers.nonEmpty) {
        builder = builder.headers(headers.toArray.flatMap(header => Array(header.name, header.value)): _*)
      }

      builder.build()
    })
  }

  def get(
    url: URL,
    headers: Set[Header] = Set.empty,
    version: HttpVersion = HttpVersion.`1.1`,
    timeout: FiniteDuration = 10.seconds
  ): HttpRequest =
    GET(url, headers, version, timeout)

  def post(
    url: URL,
    body: RequestBody,
    headers: Set[Header] = Set.empty,
    version: HttpVersion = HttpVersion.`1.1`,
    timeout: FiniteDuration = 10.seconds
  ): HttpRequest =
    POST(url, body, headers, version, timeout)

  def put(
    url: URL,
    body: RequestBody,
    headers: Set[Header] = Set.empty,
    version: HttpVersion = HttpVersion.`1.1`,
    timeout: FiniteDuration = 10.seconds
  ): HttpRequest =
    PUT(url, body, headers, version, timeout)

  def delete(
    url: URL,
    headers: Set[Header] = Set.empty,
    version: HttpVersion = HttpVersion.`1.1`,
    timeout: FiniteDuration = 10.seconds
  ): HttpRequest =
    DELETE(url, headers, version, timeout)

  def head(
    url: URL,
    headers: Set[Header] = Set.empty,
    version: HttpVersion = HttpVersion.`1.1`,
    timeout: FiniteDuration = 10.seconds
  ): HttpRequest =
    HEAD(url, headers, version, timeout)

  def options(
    url: URL,
    headers: Set[Header] = Set.empty,
    version: HttpVersion = HttpVersion.`1.1`,
    timeout: FiniteDuration = 10.seconds
  ): HttpRequest =
    OPTIONS(url, headers, version, timeout)
}
