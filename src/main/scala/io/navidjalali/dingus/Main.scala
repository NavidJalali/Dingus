package io.navidjalali.dingus

import zio._
import zio.Console
import zio.stream.ZStream

object Main extends ZIOAppDefault {

  val env = ZEnv.live ++ HttpClientConfiguration.default >>> HttpClient.live

  val get =
    HttpClient.get(URL("https://jsonplaceholder.typicode.com/posts"))

  val post =
    HttpClient.post(
      url = URL("https://jsonplaceholder.typicode.com/posts"),
      body = RequestBody.fromString("""{"title": "foo", "body": "bar", "userId": 1}"""),
      headers = Set(Header.contentTypeJson)
    )

  val put =
    HttpClient.put(
      url = URL("https://jsonplaceholder.typicode.com/posts/1"),
      body = RequestBody.fromStream(
        ZStream.fromIterable("""{"id": 1, "title": "foo", "body": "bar", "userId": 1}""".getBytes)
      ),
      headers = Set(Header.contentTypeJson)
    )

  val patch =
    HttpClient.patch(
      url = URL("https://jsonplaceholder.typicode.com/posts/1"),
      body = RequestBody.fromIterable("""{"title": "foo"}""".getBytes),
      headers = Set(Header.contentTypeJson)
    )

  val delete =
    HttpClient.delete(URL("https://jsonplaceholder.typicode.com/posts/1"))

  val head =
    HttpClient.head(URL("https://jsonplaceholder.typicode.com/posts/"))

  val options =
    HttpClient.options(URL("https://jsonplaceholder.typicode.com/posts/"))

  def app(req: ZIO[HttpClient, Throwable, HttpResponse]) =
    for {
      response <- req
      _        <- Console.printLine(s"Status: ${response.statusCode}")
      _        <- Console.printLine(s"Headers: ${response.headers}")
      body     <- response.bodyAsString
      _        <- Console.printLine(s"Body: $body")
    } yield ()

  override def run: ZIO[Any with ZIOAppArgs with Scope, Any, Any] = {
    app(get).tap(Console.printLine(_)).provide(env)
  }
}
