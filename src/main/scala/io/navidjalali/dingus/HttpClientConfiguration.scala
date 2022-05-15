package io.navidjalali.dingus

import zio.ZLayer

import java.util.concurrent.Executor
import javax.net.ssl.{SSLContext, SSLParameters}
import scala.concurrent.duration.{DurationInt, FiniteDuration}

final case class HttpClientConfiguration(
  poolSize: Int = 128,
  executor: Executor = zio.Runtime.defaultExecutor.asJava,
  connectionTimeout: FiniteDuration = 10.seconds,
  followRedirects: RedirectionOptions = RedirectionOptions.FollowIfSafe,
  sslContext: SSLContext = SSLContext.getDefault,
  sslParameters: SSLParameters = SSLContext.getDefault.getDefaultSSLParameters
)

object HttpClientConfiguration {
  val default = ZLayer.succeed(HttpClientConfiguration())
}
