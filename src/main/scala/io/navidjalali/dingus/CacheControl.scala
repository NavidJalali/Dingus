package io.navidjalali.dingus

sealed trait CacheControl {
  def render: String
}

object CacheControl {
  sealed trait RequestCacheControl extends CacheControl {
    def ++(that: RequestCacheControl): RequestCacheControl =
      if (this == that) this else CombineRequest(this, that)
  }

  sealed trait ResponseCacheControl extends CacheControl {
    def ++(that: ResponseCacheControl): ResponseCacheControl =
      if (this == that) this else CombineResponse(this, that)
  }

  case object NoCache extends RequestCacheControl with ResponseCacheControl {
    override def render: String = "no-cache"
  }

  case object NoStore extends RequestCacheControl with ResponseCacheControl {
    override def render: String = "no-store"
  }

  final case class MaxAge(value: Long) extends RequestCacheControl with ResponseCacheControl {
    override def render: String = s"max-age=$value"
  }

  final case class SMaxAge(value: Long) extends ResponseCacheControl {
    override def render: String = s"s-maxage=$value"
  }

  final case class MaxStale(value: Long) extends RequestCacheControl {
    override def render: String = s"max-stale=$value"
  }

  final case class MinFresh(value: Long) extends RequestCacheControl {
    override def render: String = s"min-fresh=$value"
  }

  case object OnlyIfCached extends RequestCacheControl {
    override def render: String = "only-if-cached"
  }

  case object NoTransform extends RequestCacheControl with ResponseCacheControl {
    override def render: String = "no-transform"
  }

  case object StaleIfError extends RequestCacheControl {
    override def render: String = "stale-if-error"
  }

  case object MustRevalidate extends ResponseCacheControl {
    override def render: String = "must-revalidate"
  }

  case object MustUnderstand extends ResponseCacheControl {
    override def render: String = "must-understand"
  }

  case object ProxyRevalidate extends ResponseCacheControl {
    override def render: String = "proxy-revalidate"
  }

  case object Public extends ResponseCacheControl {
    override def render: String = "public"
  }

  case object Private extends ResponseCacheControl {
    override def render: String = "private"
  }

  case object Immutable extends ResponseCacheControl {
    override def render: String = "immutable"
  }

  case object StaleWhileRevalidate extends ResponseCacheControl {
    override def render: String = "stale-while-revalidate"
  }

  final case class CombineRequest(left: RequestCacheControl, right: RequestCacheControl) extends RequestCacheControl {
    override def render: String = s"${left.render}, ${right.render}"
  }

  final case class CombineResponse(left: ResponseCacheControl, right: ResponseCacheControl)
      extends ResponseCacheControl {
    override def render: String = s"${left.render}, ${right.render}"
  }
}
