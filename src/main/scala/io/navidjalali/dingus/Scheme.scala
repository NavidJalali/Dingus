package io.navidjalali.dingus

sealed trait Scheme {
  val render: String
  val defaultPort: Int
  override def toString: String = render
}

object Scheme {
  case object Http extends Scheme {
    val render: String = "http"
    val defaultPort: Int = 80
  }

  case object Https extends Scheme {
    val render: String = "https"
    val defaultPort: Int = 443
  }

  def fromString(s: String): Option[Scheme] = Option(s).map(_.toLowerCase).flatMap {
    case "http" => Some(Http)
    case "https" => Some(Https)
    case _ => None
  }
}
