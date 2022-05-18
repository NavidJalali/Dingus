package io.navidjalali.dingus

final case class QueryParameters(underlying: Map[String, String]) {
  def get(key: String): Option[String]                     = underlying.get(key)
  def getOrElse(key: String, default: => String): String   = underlying.getOrElse(key, default)
  def ++(other: QueryParameters): QueryParameters          = QueryParameters(underlying ++ other.underlying)
  def +(other: (String, String)): QueryParameters          = QueryParameters(underlying + other)
  def -(key: String): QueryParameters                      = QueryParameters(underlying - key)
  def render: String                                       = underlying.map { case (k, v) => s"$k=$v" }.mkString("&")
  def updated(key: String, value: String): QueryParameters = QueryParameters(underlying.updated(key, value))
  override def toString: String                            = render
}

object QueryParameters {
  def empty                                                     = QueryParameters(Map.empty)
  def fromTuple(pair: (String, String)): QueryParameters        = QueryParameters(Map(pair))
  def fromKeyValue(key: String, value: String): QueryParameters = QueryParameters(Map(key -> value))
  def fromString(str: String): QueryParameters =
    if (str eq null) empty
    else
      QueryParameters(
        str.split("&").map(s => s.split("=")).map(a => (a(0), a(1))).toMap
      )
}
