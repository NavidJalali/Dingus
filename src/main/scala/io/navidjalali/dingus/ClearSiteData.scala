package io.navidjalali.dingus

sealed trait ClearSiteData {
  def render: String
}

object ClearSiteData {

  case object All extends ClearSiteData {
    override def render: String = "*"
  }

  trait Exact extends ClearSiteData {
    def ++(that: Exact): Exact =
      if (this == that) this
      else Combine(this, that)
  }

  case object Cookies extends Exact {
    override def render: String = "cookies"
  }

  case object Cache extends Exact {
    override def render: String = "cache"
  }

  case object Storage extends Exact {
    override def render: String = "storage"
  }

  case object ExecutionContexts extends Exact {
    override def render: String = "executionContexts"
  }

  final case class Combine(left: Exact, right: Exact) extends Exact {
    override def render: String = s"${left.render}, ${right.render}"
  }
}
