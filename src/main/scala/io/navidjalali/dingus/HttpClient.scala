package io.navidjalali.dingus

import zio.{Duration, Semaphore, ZIO, ZLayer}

import java.io.UncheckedIOException
import java.net.http.{HttpClient => JHttpClient, HttpResponse => JHttpResponse}
import javax.net.ssl.{SSLContext, SSLParameters}

sealed trait HttpClient { self =>

  val semaphore: Semaphore

  def request(request: HttpRequest): ZIO[Any, Throwable, HttpResponse] =
    semaphore.withPermit(
      for {
        req <- request.asJava
        response <- ZIO
                      .fromCompletableFuture(
                        self.asJava.sendAsync(
                          req,
                          JHttpResponse.BodyHandlers.ofPublisher()
                        )
                      )
                      .map(HttpResponse.fromJava)
      } yield response
    )

  val asJava: JHttpClient
}

object HttpClient {

  def request(request: HttpRequest): ZIO[HttpClient, Throwable, HttpResponse] =
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
