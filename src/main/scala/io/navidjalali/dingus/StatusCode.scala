package io.navidjalali.dingus

sealed trait StatusCode {
  val code: Int
  def isInformational: Boolean = code >= 100 && code < 200
  def isSuccess: Boolean       = code >= 200 && code < 300
  def isRedirect: Boolean      = code >= 300 && code < 400
  def isClientError: Boolean   = code >= 400 && code < 500
}

object StatusCode {

  def apply(code: Int): Either[IllegalArgumentException, StatusCode] =
    if (code >= 100 && code < 200) Informational(code)
    else if (code >= 200 && code < 300) Success(code)
    else if (code >= 300 && code < 400) Redirect(code)
    else if (code >= 400 && code < 500) ClientError(code)
    else if (code >= 500 && code < 600) ServerError(code)
    else Left(new IllegalArgumentException(s"$code is not a valid status code"))

  sealed trait Informational extends StatusCode

  object Informational {

    def apply(code: Int): Either[IllegalArgumentException, Informational] =
      code match {
        case 100 => Right(Continue)
        case 101 => Right(SwitchingProtocols)
        case 102 => Right(Processing)
        case 103 => Right(EarlyHints)
        case _   => Left(new IllegalArgumentException(s"$code is not an informational status code"))
      }

    case object Continue           extends Informational { val code = 100 }
    case object SwitchingProtocols extends Informational { val code = 101 }
    case object Processing         extends Informational { val code = 102 }
    case object EarlyHints         extends Informational { val code = 103 }
  }

  sealed trait Success extends StatusCode

  object Success {

    def apply(code: Int): Either[IllegalArgumentException, Success] =
      code match {
        case 200 => Right(OK)
        case 201 => Right(Created)
        case 202 => Right(Accepted)
        case 203 => Right(NonAuthoritativeInformation)
        case 204 => Right(NoContent)
        case 205 => Right(ResetContent)
        case 206 => Right(PartialContent)
        case 207 => Right(MultiStatus)
        case 208 => Right(AlreadyReported)
        case 226 => Right(IMUsed)
        case _   => Left(new IllegalArgumentException(s"$code is not a success status code"))
      }

    case object OK                          extends Success { val code = 200 }
    case object Created                     extends Success { val code = 201 }
    case object Accepted                    extends Success { val code = 202 }
    case object NonAuthoritativeInformation extends Success { val code = 203 }
    case object NoContent                   extends Success { val code = 204 }
    case object ResetContent                extends Success { val code = 205 }
    case object PartialContent              extends Success { val code = 206 }
    case object MultiStatus                 extends Success { val code = 207 }
    case object AlreadyReported             extends Success { val code = 208 }
    case object IMUsed                      extends Success { val code = 226 }
  }

  sealed trait Redirect extends StatusCode

  object Redirect {

    def apply(code: Int): Either[IllegalArgumentException, Redirect] =
      code match {
        case 300 => Right(MultipleChoices)
        case 301 => Right(MovedPermanently)
        case 302 => Right(Found)
        case 303 => Right(SeeOther)
        case 304 => Right(NotModified)
        case 305 => Right(UseProxy)
        case 307 => Right(TemporaryRedirect)
        case 308 => Right(PermanentRedirect)
        case _   => Left(new IllegalArgumentException(s"$code is not a redirect status code"))
      }

    case object MultipleChoices   extends Redirect { val code = 300 }
    case object MovedPermanently  extends Redirect { val code = 301 }
    case object Found             extends Redirect { val code = 302 }
    case object SeeOther          extends Redirect { val code = 303 }
    case object NotModified       extends Redirect { val code = 304 }
    case object UseProxy          extends Redirect { val code = 305 }
    case object TemporaryRedirect extends Redirect { val code = 307 }
    case object PermanentRedirect extends Redirect { val code = 308 }
  }

  sealed trait ClientError extends StatusCode

  object ClientError {

    def apply(code: Int): Either[IllegalArgumentException, ClientError] =
      code match {
        case 400 => Right(BadRequest)
        case 401 => Right(Unauthorized)
        case 402 => Right(PaymentRequired)
        case 403 => Right(Forbidden)
        case 404 => Right(NotFound)
        case 405 => Right(MethodNotAllowed)
        case 406 => Right(NotAcceptable)
        case 407 => Right(ProxyAuthenticationRequired)
        case 408 => Right(RequestTimeout)
        case 409 => Right(Conflict)
        case 410 => Right(Gone)
        case 411 => Right(LengthRequired)
        case 412 => Right(PreconditionFailed)
        case 413 => Right(PayloadTooLarge)
        case 414 => Right(UriTooLong)
        case 415 => Right(UnsupportedMediaType)
        case 416 => Right(RangeNotSatisfiable)
        case 417 => Right(ExpectationFailed)
        case 418 => Right(ImATeapot)
        case 421 => Right(MisdirectedRequest)
        case 422 => Right(UnprocessableEntity)
        case 423 => Right(Locked)
        case 424 => Right(FailedDependency)
        case 426 => Right(UpgradeRequired)
        case 428 => Right(PreconditionRequired)
        case 429 => Right(TooManyRequests)
        case 431 => Right(RequestHeaderFieldsTooLarge)
        case 451 => Right(UnavailableForLegalReasons)
        case _   => Left(new IllegalArgumentException(s"$code is not a client error status code"))
      }

    case object BadRequest                  extends ClientError { val code = 400 }
    case object Unauthorized                extends ClientError { val code = 401 }
    case object PaymentRequired             extends ClientError { val code = 402 }
    case object Forbidden                   extends ClientError { val code = 403 }
    case object NotFound                    extends ClientError { val code = 404 }
    case object MethodNotAllowed            extends ClientError { val code = 405 }
    case object NotAcceptable               extends ClientError { val code = 406 }
    case object ProxyAuthenticationRequired extends ClientError { val code = 407 }
    case object RequestTimeout              extends ClientError { val code = 408 }
    case object Conflict                    extends ClientError { val code = 409 }
    case object Gone                        extends ClientError { val code = 410 }
    case object LengthRequired              extends ClientError { val code = 411 }
    case object PreconditionFailed          extends ClientError { val code = 412 }
    case object PayloadTooLarge             extends ClientError { val code = 413 }
    case object UriTooLong                  extends ClientError { val code = 414 }
    case object UnsupportedMediaType        extends ClientError { val code = 415 }
    case object RangeNotSatisfiable         extends ClientError { val code = 416 }
    case object ExpectationFailed           extends ClientError { val code = 417 }
    case object ImATeapot                   extends ClientError { val code = 418 }
    case object MisdirectedRequest          extends ClientError { val code = 421 }
    case object UnprocessableEntity         extends ClientError { val code = 422 }
    case object Locked                      extends ClientError { val code = 423 }
    case object FailedDependency            extends ClientError { val code = 424 }
    case object UpgradeRequired             extends ClientError { val code = 426 }
    case object PreconditionRequired        extends ClientError { val code = 428 }
    case object TooManyRequests             extends ClientError { val code = 429 }
    case object RequestHeaderFieldsTooLarge extends ClientError { val code = 431 }
    case object UnavailableForLegalReasons  extends ClientError { val code = 451 }
  }

  sealed trait ServerError extends StatusCode

  object ServerError {

    def apply(code: Int): Either[IllegalArgumentException, ServerError] =
      code match {
        case 500 => Right(InternalServerError)
        case 501 => Right(NotImplemented)
        case 502 => Right(BadGateway)
        case 503 => Right(ServiceUnavailable)
        case 504 => Right(GatewayTimeout)
        case 505 => Right(HttpVersionNotSupported)
        case 506 => Right(VariantAlsoNegotiates)
        case 507 => Right(InsufficientStorage)
        case 508 => Right(LoopDetected)
        case 510 => Right(NotExtended)
        case 511 => Right(NetworkAuthenticationRequired)
        case _   => Left(new IllegalArgumentException(s"$code is not a server error status code"))
      }

    case object InternalServerError           extends ServerError { val code = 500 }
    case object NotImplemented                extends ServerError { val code = 501 }
    case object BadGateway                    extends ServerError { val code = 502 }
    case object ServiceUnavailable            extends ServerError { val code = 503 }
    case object GatewayTimeout                extends ServerError { val code = 504 }
    case object HttpVersionNotSupported       extends ServerError { val code = 505 }
    case object VariantAlsoNegotiates         extends ServerError { val code = 506 }
    case object InsufficientStorage           extends ServerError { val code = 507 }
    case object LoopDetected                  extends ServerError { val code = 508 }
    case object NotExtended                   extends ServerError { val code = 510 }
    case object NetworkAuthenticationRequired extends ServerError { val code = 511 }
  }
}
