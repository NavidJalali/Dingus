package io.navidjalali.dingus

sealed trait ETag {
  protected val value: String
  def render: String
}

object ETag {
  private[this] final case class Weak(value: String) extends ETag {
    def render: String = s"""W/"$value""""
  }

  private[this] final case class Value(value: String) extends ETag {
    def render: String = s"""$value"""
  }

  def makeWeak(value: String): ETag = Weak(value)
  def make(value: String): ETag     = Value(value)
}
