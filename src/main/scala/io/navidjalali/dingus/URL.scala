package io.navidjalali.dingus

import java.net.URI

final case class URL private (underlying: URI) {
  def asJava: URI = underlying
}

object URL {
  def apply(url: String): Either[IllegalArgumentException, URL] =
    try {
      Right(URL(URI.create(url)))
    } catch {
      case e: IllegalArgumentException => Left(e)
    }

  def unsafeFromString(url: String): URL = URL(url).fold(e => throw e, identity)
}
