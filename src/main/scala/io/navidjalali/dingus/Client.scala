package io.navidjalali.dingus

import zio.ZIO

import java.net.http.{HttpClient, HttpResponse}
import java.time.Duration
import javax.net.ssl.SSLContext

sealed trait Client {
  def request(request: Request): ZIO[Any, Throwable, Response] =
    ZIO
      .fromCompletableFuture(Client.default.asJava.sendAsync(request.asJava, HttpResponse.BodyHandlers.ofLines()))
      .map(Response.fromJava)

  val asJava: HttpClient
}

object Client {
  lazy val default: Client = new Client {
    override val asJava: HttpClient =
      HttpClient
        .newBuilder()
        .followRedirects(HttpClient.Redirect.ALWAYS)
        .connectTimeout(Duration.ofSeconds(60))
        .sslContext(SSLContext.getDefault)
        .sslParameters(SSLContext.getDefault.getDefaultSSLParameters)
        .executor(zio.Runtime.default.executor.asJava)
        .build()
  }

  // Todo: make a dsl for creating clients
}
