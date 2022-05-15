package io.navidjalali.dingus

import zio._
import zio.Console

object Main extends ZIOAppDefault {

  val env = ZEnv.live ++ ClientConfiguration.default >>> Client.live

  override def run: ZIO[Any with ZIOAppArgs with Scope, Any, Any] =
    Client
      .request(Request.GET(URL.unsafeFromString("https://jsonplaceholder.typicode.com/posts/1")))
      .flatMap(_.bodyAsString)
      .tap(Console.printLine(_))
      .provide(env)
}
