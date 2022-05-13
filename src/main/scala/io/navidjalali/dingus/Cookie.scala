package io.navidjalali.dingus

final case class Cookie (name: String, value: String) {
  def render: String = s"$name=$value"
}
