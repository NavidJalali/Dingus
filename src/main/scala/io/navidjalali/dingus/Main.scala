package io.navidjalali.dingus

import io.navidjalali.dingus.URL.StringSyntax
import zio._
import zio.Console
import zio.stream.ZStream

object Main extends ZIOAppDefault {

  val env = ZEnv.live ++ HttpClientConfiguration.default >>> HttpClient.live

  override def run: ZIO[Any with ZIOAppArgs with Scope, Any, Any] =
    HttpClient
      .get("https://jsonplaceholder.typicode.com/posts".toUrlUnsafe)
      .tap(resp => Console.printLine(resp.statusCode))
      .flatMap(_.bodyAsString)
      .tap(Console.printLine(_))
      .provide(env)
}
