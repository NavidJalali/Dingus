package io.navidjalali.dingus

sealed trait HttpMethod

object HttpMethod {
  case object GET     extends HttpMethod
  case object POST    extends HttpMethod
  case object PUT     extends HttpMethod
  case object PATCH   extends HttpMethod
  case object DELETE  extends HttpMethod
  case object HEAD    extends HttpMethod
  case object OPTIONS extends HttpMethod
}
