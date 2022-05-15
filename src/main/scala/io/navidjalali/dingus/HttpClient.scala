package io.navidjalali.dingus

import zio.{Duration, Semaphore, ZIO, ZLayer}

import java.io.UncheckedIOException
import java.net.http.{HttpClient => JHttpClient, HttpResponse => JHttpResponse}
import javax.net.ssl.{SSLContext, SSLParameters}
import scala.concurrent.duration.{DurationInt, FiniteDuration}

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

  def get(
    url: URL,
    headers: Set[Header] = Set.empty,
    version: HttpVersion = HttpVersion.`1.1`,
    timeout: FiniteDuration = 10.seconds
  ): ZIO[Any, Throwable, HttpResponse] =
    self.request(HttpRequest.get(url, headers, version, timeout))

  def post(
    url: URL,
    body: RequestBody,
    headers: Set[Header] = Set.empty,
    version: HttpVersion = HttpVersion.`1.1`,
    timeout: FiniteDuration = 10.seconds
  ): ZIO[Any, Throwable, HttpResponse] =
    self.request(HttpRequest.post(url, body, headers, version, timeout))

  def put(
    url: URL,
    body: RequestBody,
    headers: Set[Header] = Set.empty,
    version: HttpVersion = HttpVersion.`1.1`,
    timeout: FiniteDuration = 10.seconds
  ): ZIO[Any, Throwable, HttpResponse] =
    self.request(HttpRequest.put(url, body, headers, version, timeout))

  def patch(
    url: URL,
    body: RequestBody,
    headers: Set[Header] = Set.empty,
    version: HttpVersion = HttpVersion.`1.1`,
    timeout: FiniteDuration = 10.seconds
  ): ZIO[Any, Throwable, HttpResponse] =
    self.request(HttpRequest.patch(url, body, headers, version, timeout))

  def delete(
    url: URL,
    headers: Set[Header] = Set.empty,
    version: HttpVersion = HttpVersion.`1.1`,
    timeout: FiniteDuration = 10.seconds
  ): ZIO[Any, Throwable, HttpResponse] =
    self.request(HttpRequest.delete(url, headers, version, timeout))

  def head(
    url: URL,
    headers: Set[Header] = Set.empty,
    version: HttpVersion = HttpVersion.`1.1`,
    timeout: FiniteDuration = 10.seconds
  ): ZIO[Any, Throwable, HttpResponse] =
    self.request(HttpRequest.head(url, headers, version, timeout))

  def options(
    url: URL,
    headers: Set[Header] = Set.empty,
    version: HttpVersion = HttpVersion.`1.1`,
    timeout: FiniteDuration = 10.seconds
  ): ZIO[Any, Throwable, HttpResponse] =
    self.request(HttpRequest.options(url, headers, version, timeout))

  val asJava: JHttpClient
}

object HttpClient {

  def request(request: HttpRequest): ZIO[HttpClient, Throwable, HttpResponse] =
    ZIO.environmentWithZIO[HttpClient](_.get.request(request))

  def get(
    url: URL,
    headers: Set[Header] = Set.empty,
    version: HttpVersion = HttpVersion.`1.1`,
    timeout: FiniteDuration = 10.seconds
  ): ZIO[HttpClient, Throwable, HttpResponse] =
    ZIO.environmentWithZIO[HttpClient](_.get.get(url, headers, version, timeout))

  def post(
    url: URL,
    body: RequestBody,
    headers: Set[Header] = Set.empty,
    version: HttpVersion = HttpVersion.`1.1`,
    timeout: FiniteDuration = 10.seconds
  ): ZIO[HttpClient, Throwable, HttpResponse] =
    ZIO.environmentWithZIO[HttpClient](_.get.post(url, body, headers, version, timeout))

  def put(
    url: URL,
    body: RequestBody,
    headers: Set[Header] = Set.empty,
    version: HttpVersion = HttpVersion.`1.1`,
    timeout: FiniteDuration = 10.seconds
  ): ZIO[HttpClient, Throwable, HttpResponse] =
    ZIO.environmentWithZIO[HttpClient](_.get.put(url, body, headers, version, timeout))

  def patch(
    url: URL,
    body: RequestBody,
    headers: Set[Header] = Set.empty,
    version: HttpVersion = HttpVersion.`1.1`,
    timeout: FiniteDuration = 10.seconds
  ): ZIO[HttpClient, Throwable, HttpResponse] =
    ZIO.environmentWithZIO[HttpClient](_.get.patch(url, body, headers, version, timeout))

  def delete(
    url: URL,
    headers: Set[Header] = Set.empty,
    version: HttpVersion = HttpVersion.`1.1`,
    timeout: FiniteDuration = 10.seconds
  ): ZIO[HttpClient, Throwable, HttpResponse] =
    ZIO.environmentWithZIO[HttpClient](_.get.delete(url, headers, version, timeout))

  def head(
    url: URL,
    headers: Set[Header] = Set.empty,
    version: HttpVersion = HttpVersion.`1.1`,
    timeout: FiniteDuration = 10.seconds
  ): ZIO[HttpClient, Throwable, HttpResponse] =
    ZIO.environmentWithZIO[HttpClient](_.get.head(url, headers, version, timeout))

  def options(
    url: URL,
    headers: Set[Header] = Set.empty,
    version: HttpVersion = HttpVersion.`1.1`,
    timeout: FiniteDuration = 10.seconds
  ): ZIO[HttpClient, Throwable, HttpResponse] =
    ZIO.environmentWithZIO[HttpClient](_.get.options(url, headers, version, timeout))

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
