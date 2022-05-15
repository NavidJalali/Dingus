package io.navidjalali.dingus

import io.navidjalali.dingus.URL.StringSyntax
import zio._
import zio.Console
import zio.stream.ZStream

object Main extends ZIOAppDefault {

  val env = ZEnv.live ++ HttpClientConfiguration.default >>> HttpClient.live

  val get =
    HttpClient.get("https://jsonplaceholder.typicode.com/posts".toUrlUnsafe)

  val post =
    HttpClient.post(
      url = "https://jsonplaceholder.typicode.com/posts".toUrlUnsafe,
      body = RequestBody.fromString("""{"title": "foo", "body": "bar", "userId": 1}"""),
      headers = Set(Header.contentTypeJson)
    )

  val put =
    HttpClient.put(
      url = "https://jsonplaceholder.typicode.com/posts/1".toUrlUnsafe,
      body = RequestBody.fromStream(
        ZStream.fromIterable("""{"id": 1, "title": "foo", "body": "bar", "userId": 1}""".getBytes)
      ),
      headers = Set(Header.contentTypeJson)
    )

  val patch =
    HttpClient.patch(
      url = "https://jsonplaceholder.typicode.com/posts/1".toUrlUnsafe,
      body = RequestBody.fromIterable("""{"title": "foo"}""".getBytes),
      headers = Set(Header.contentTypeJson)
    )

  val delete =
    HttpClient.delete("https://jsonplaceholder.typicode.com/posts/1".toUrlUnsafe)

  val head =
    HttpClient.head("https://jsonplaceholder.typicode.com/posts/".toUrlUnsafe)

  val options =
    HttpClient.options("https://jsonplaceholder.typicode.com/posts/".toUrlUnsafe)

  def app(req: ZIO[HttpClient, Throwable, HttpResponse]) =
    for {
      response <- req
      _        <- Console.printLine(s"Status: ${response.statusCode}")
      _        <- Console.printLine(s"Headers: ${response.headers}")
      body     <- response.bodyAsString
      _        <- Console.printLine(s"Body: $body")
    } yield ()

  override def run: ZIO[Any with ZIOAppArgs with Scope, Any, Any] =
    (
      for {
        _ <- app(get)
        _ <- app(post)
        _ <- app(put)
        _ <- app(patch)
        _ <- app(delete)
        _ <- app(head)
        _ <- app(options)
      } yield ()
    ).provide(env)
}
