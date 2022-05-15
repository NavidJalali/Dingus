package io.navidjalali.dingus

import java.net.http.HttpClient

sealed trait RedirectionOptions {
  def asJava: HttpClient.Redirect
}

object RedirectionOptions {
  case object AlwaysFollow extends RedirectionOptions {
    override def asJava: HttpClient.Redirect = HttpClient.Redirect.ALWAYS
  }

  case object NeverFollow extends RedirectionOptions {
    override def asJava: HttpClient.Redirect = HttpClient.Redirect.NEVER
  }

  case object FollowIfSafe extends RedirectionOptions {
    override def asJava: HttpClient.Redirect = HttpClient.Redirect.NORMAL
  }
}
