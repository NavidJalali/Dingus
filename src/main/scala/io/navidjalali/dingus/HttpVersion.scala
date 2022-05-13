package io.navidjalali.dingus

import java.net.http.HttpClient

sealed trait HttpVersion {
  def asJava: HttpClient.Version
}
object HttpVersion {
  case object `2` extends HttpVersion {
    override def asJava: HttpClient.Version = HttpClient.Version.HTTP_2
  }

  case object `1.1` extends HttpVersion {
    override def asJava: HttpClient.Version = HttpClient.Version.HTTP_1_1
  }
}
