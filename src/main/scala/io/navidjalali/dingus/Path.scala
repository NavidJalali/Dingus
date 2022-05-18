package io.navidjalali.dingus

import java.net.URLEncoder
import java.nio.charset.StandardCharsets

final case class Path(underlying: List[String]) {
  def /(name: String): Path       = Path(underlying :+ Path.encode(name))
  def /(path: Path): Path         = Path(underlying ++ path.underlying)
  def ++(path: Path): Path        = Path(underlying ++ path.underlying)
  def prepend(name: String): Path = Path(Path.encode(name) :: underlying)
  def prepend(path: Path): Path   = Path(path.underlying ++ underlying)
  def render: String              = "/" + underlying.mkString("/")
  override def toString: String   = render
}

object Path {
  private def encode(s: String): String = URLEncoder.encode(s, StandardCharsets.UTF_8)
  val Root                              = Path(Nil)
  def apply(str: String): Path = Path(
    str
      .split("/")
      .collect {
        case s if s.nonEmpty => encode(s)
      }
      .toList
  )
}
