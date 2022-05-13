package io.navidjalali.dingus

import zio._
import zio.Console

object Main extends ZIOAppDefault {
  override def run: ZIO[Any with ZIOAppArgs with Scope, Any, Any] =
    Client
      .default
      .request(Request.GET(URL.unsafeFromString("https://jsonplaceholder.typicode.com/posts/1")))
      .flatMap(_.bodyAsString)
      .tap(Console.printLine(_))
}
