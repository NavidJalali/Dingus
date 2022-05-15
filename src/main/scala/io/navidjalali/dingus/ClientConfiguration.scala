package io.navidjalali.dingus

import zio.ZLayer

import java.net.http.HttpClient
import java.time.Duration
import java.util.concurrent.Executor
import javax.net.ssl.{SSLContext, SSLParameters}

final case class ClientConfiguration(
  poolSize: Int = 128,
  executor: Executor = zio.Runtime.defaultExecutor.asJava,
  connectionTimeout: Duration = Duration.ofSeconds(10),
  redirect: HttpClient.Redirect = HttpClient.Redirect.ALWAYS,
  sslContext: SSLContext = SSLContext.getDefault,
  sslParameters: SSLParameters = SSLContext.getDefault.getDefaultSSLParameters
)

object ClientConfiguration {
  val default = ZLayer.succeed(ClientConfiguration())
}
