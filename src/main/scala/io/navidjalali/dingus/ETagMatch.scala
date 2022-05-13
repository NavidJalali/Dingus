package io.navidjalali.dingus

sealed trait ETagMatch {
  def render: String
}

object ETagMatch {
  private[this] case object All extends ETagMatch {
    def render: String = "*"
  }

  private[this] sealed trait Exact extends ETagMatch {
    def render: String
  }

  private[this] case class Match(value: ETag) extends Exact {
    def render: String = value.render
  }

  private[this] case class Combine(left: Exact, right: Exact) extends Exact {
    def render: String = s"${left.render}, ${right.render}"
  }

  def single(value: ETag): ETagMatch = Match(value)

  def combine(x: ETag, xs: ETag*): ETagMatch =
    xs.foldLeft[Exact](Match(x)) { case (acc, y) =>
      Combine(acc, Match(y))
    }

  def all: ETagMatch = All
}
