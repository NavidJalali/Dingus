package io.navidjalali.dingus

import java.net.http.HttpRequest.BodyPublisher

final case class RequestBody(bodyPublisher: BodyPublisher)
