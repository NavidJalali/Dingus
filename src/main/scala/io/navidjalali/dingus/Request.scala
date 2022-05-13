package io.navidjalali.dingus

import java.net.http.HttpRequest
import scala.concurrent.duration.{DurationInt, FiniteDuration}
import scala.jdk.DurationConverters.ScalaDurationOps

sealed trait Request {
  val url: URL
  val method: HttpMethod
  val headers: Set[Header]
  def asJava: HttpRequest
}

object Request {
  final case class GET(
    url: URL,
    headers: Set[Header] = Set.empty,
    version: HttpVersion = HttpVersion.`1.1`,
    timeout: FiniteDuration = 10.seconds
  ) extends Request {
    override val method: HttpMethod = HttpMethod.GET

    override def asJava: HttpRequest = {
      var builder =
        HttpRequest
          .newBuilder()
          .uri(url.asJava)
          .GET()
          .timeout(timeout.toJava)
          .version(version.asJava)

      if (headers.nonEmpty) {
        builder = builder.headers(headers.toArray.flatMap(header => Array(header.name, header.value)): _*)
      }

      builder.build()
    }
  }

  final case class POST(
    url: URL,
    body: RequestBody,
    headers: Set[Header] = Set.empty,
    version: HttpVersion = HttpVersion.`1.1`,
    timeout: FiniteDuration = 10.seconds
  ) extends Request {
    override val method: HttpMethod = HttpMethod.POST

    override def asJava: HttpRequest = {
      var builder =
        HttpRequest
          .newBuilder()
          .uri(url.asJava)
          .POST(body.bodyPublisher)
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
  ) extends Request {
    override val method: HttpMethod = HttpMethod.PUT

    override def asJava: HttpRequest = {
      var builder =
        HttpRequest
          .newBuilder()
          .uri(url.asJava)
          .PUT(body.bodyPublisher)
          .timeout(timeout.toJava)
          .version(version.asJava)

      if (headers.nonEmpty) {
        builder = builder.headers(headers.toArray.flatMap(header => Array(header.name, header.value)): _*)
      }

      builder.build()
    }
  }
}
