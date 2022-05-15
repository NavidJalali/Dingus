package io.navidjalali.dingus

import zio._
import zio.Console
import zio.stream.ZStream

object Main extends ZIOAppDefault {

  val env = ZEnv.live ++ HttpClientConfiguration.default >>> HttpClient.live

  override def run: ZIO[Any with ZIOAppArgs with Scope, Any, Any] =
    HttpClient
      .request(
        HttpRequest.POST(
          URL.unsafeFromString("https://jsonplaceholder.typicode.com/posts"),
          RequestBody.fromStream(
            ZStream.fromIterable(("""
              |{
              |    "title": "foo",
              |    "body": "bar",
              |    "userId": 1
              |}
              |""".stripMargin).getBytes("UTF-8"))),
          Set(Header.contentType("application/json; charset=utf-8"))
        )
      )
      .tap(resp => Console.printLine(resp.statusCode))
      .flatMap(_.bodyAsString)
      .tap(Console.printLine(_))
      .provide(env)
}
