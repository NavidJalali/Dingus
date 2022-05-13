package io.navidjalali.dingus

sealed trait TransferEncoding {
  def render: String
}

object TransferEncoding {
  case object Identity extends TransferEncoding {
    override def render: String = "identity"
  }

  case object Chunked extends TransferEncoding {
    override def render: String = "chunked"
  }

  case object Compress extends TransferEncoding {
    override def render: String = "compress"
  }

  case object Deflate extends TransferEncoding {
    override def render: String = "deflate"
  }

  case object Gzip extends TransferEncoding {
    override def render: String = "gzip"
  }
}
