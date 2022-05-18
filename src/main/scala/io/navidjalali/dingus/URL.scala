package io.navidjalali.dingus

import java.net.URI

final case class URL private (underlying: URI) { self =>
  underlying.getAuthority
  def asJava: URI                      = underlying
  def queryParameters: QueryParameters = QueryParameters.fromString(underlying.getQuery)
  def host: String                     = underlying.getHost
  def port: Int = {
    val p = underlying.getPort
    if (p == -1) scheme.defaultPort else p
  }

  def path: Path               = Path(underlying.getPath)
  def scheme: Scheme           = Scheme.fromString(underlying.getScheme).get
  def authority: String        = underlying.getAuthority
  def userInfo: Option[String] = Option(underlying.getUserInfo)
  def fragment: Option[String] = Option(underlying.getFragment)

  def withPath(path: Path): URL =
    URL(
      new URI(
        underlying.getScheme,
        underlying.getUserInfo,
        underlying.getHost,
        underlying.getPort,
        (self.path ++ path).render,
        underlying.getQuery,
        underlying.getFragment
      )
    )

  def withQueryParameters(queryParameters: QueryParameters): URL =
    URL(
      new URI(
        underlying.getScheme,
        underlying.getAuthority,
        underlying.getPath,
        (self.queryParameters ++ queryParameters).render,
        underlying.getFragment
      )
    )

  def withQueryParameters(queryParameters: Map[String, String]): URL =
    withQueryParameters(QueryParameters(queryParameters))

  def withQueryParameters(parameters: (String, String)*): URL =
    withQueryParameters(QueryParameters(parameters.toMap))

  def withQueryParameter(key: String, value: String): URL =
    withQueryParameters(QueryParameters(Map(key -> value)))

  def withQueryParameter(key: String, values: Seq[String]): URL =
    withQueryParameters(QueryParameters(Map(key -> values.mkString(","))))

  def withQueryParameter(key: String, values: Set[String]): URL =
    withQueryParameters(QueryParameters(Map(key -> values.mkString(","))))
}

object URL {
  def fromUri(uri: URI): URL =
    Scheme
      .fromString(uri.getScheme)
      .fold[URL](throw new IllegalArgumentException(s"Invalid scheme: ${uri.getScheme}")) { _ =>
        URL(uri)
      }

  def apply(url: String): URL = fromUri(URI.create(url))

  def fromString(url: String): Either[IllegalArgumentException, URL] =
    try {
      Right(URL(URI.create(url)))
    } catch {
      case e: IllegalArgumentException => Left(e)
    }

  implicit final class StringOps(private val self: String) extends AnyVal {
    def toURL: URL = apply(self)
  }
}
