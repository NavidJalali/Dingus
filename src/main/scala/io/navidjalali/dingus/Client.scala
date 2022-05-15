package io.navidjalali.dingus

import zio.{Semaphore, ZIO, ZLayer}

import java.io.UncheckedIOException
import java.net.http.{HttpClient, HttpResponse}
import javax.net.ssl.{SSLContext, SSLParameters}

sealed trait Client { self =>

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

  val asJava: HttpClient
}

object Client {

  def request(request: Request): ZIO[Client, Throwable, Response] =
    ZIO.environmentWithZIO[Client](_.get.request(request))

  val live =
    ZLayer.fromZIO {
      for {
        config  <- ZIO.environmentWith[ClientConfiguration](_.get)
        permits <- Semaphore.make(config.poolSize)
        client <- ZIO.attempt {
                    new Client {
                      override val semaphore: Semaphore = permits
                      override val asJava: HttpClient =
                        HttpClient
                          .newBuilder()
                          .sslContext(SSLContext.getDefault)
                          .sslParameters(new SSLParameters)
                          .executor(config.executor)
                          .connectTimeout(config.connectionTimeout)
                          .build()
                    }
                  }.refineToOrDie[UncheckedIOException]
      } yield client
    }

  val default = ClientConfiguration.default >>> live
}
