package io.navidjalali.dingus

import zio.{Duration, Semaphore, ZIO, ZLayer}

import java.io.UncheckedIOException
import java.net.http.{HttpClient => JHttpClient, HttpResponse}
import javax.net.ssl.{SSLContext, SSLParameters}

sealed trait HttpClient { self =>

  val semaphore: Semaphore

  def request(request: Request): ZIO[Any, Throwable, Response] =
    semaphore.withPermit(
      ZIO
        .fromCompletableFuture(
          self.asJava.sendAsync(
            request.asJava,
            HttpResponse.BodyHandlers.ofPublisher()
          )
        )
        .map(Response.fromJava)
    )

  val asJava: JHttpClient
}

object HttpClient {

  def request(request: Request): ZIO[HttpClient, Throwable, Response] =
    ZIO.environmentWithZIO[HttpClient](_.get.request(request))

  val live =
    ZLayer.fromZIO {
      for {
        config  <- ZIO.environmentWith[HttpClientConfiguration](_.get)
        permits <- Semaphore.make(config.poolSize)
        client <- ZIO.attempt {
                    new HttpClient {
                      override val semaphore: Semaphore = permits
                      override val asJava: JHttpClient =
                        JHttpClient
                          .newBuilder()
                          .sslContext(SSLContext.getDefault)
                          .sslParameters(new SSLParameters)
                          .executor(config.executor)
                          .connectTimeout(Duration(config.connectionTimeout.length, config.connectionTimeout.unit))
                          .followRedirects(config.followRedirects.asJava)
                          .build()
                    }
                  }.refineToOrDie[UncheckedIOException]
      } yield client
    }

  val default = HttpClientConfiguration.default >>> live
}
