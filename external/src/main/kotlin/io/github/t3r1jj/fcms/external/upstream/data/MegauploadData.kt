package io.github.t3r1jj.fcms.external.upstream.data

data class MegauploadSuccessfulResponse(val status: Boolean, val data: MegauploadData)
data class MegauploadData(val file: MegauploadFile)
data class MegauploadFile(val url: MegauploadUrl, val metadata: MegauploadMetadata)
data class MegauploadUrl(val full: String, val short: String)
data class MegauploadMetadata(val id: String,
                              val name: String,
                              val size: MegauploadSize)

data class MegauploadSize(val bytes: Long, val readable: String)

data class MegauploadErrorResponse(val status: Boolean, val error: Error)
data class Error(val message: String, val type: String, val code: Int)