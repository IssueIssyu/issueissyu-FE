package com.issueissyu.fe.core.media

import android.util.Log

data class PinImageUploadMeta(
    val uriScheme: String,
    val fileName: String,
    val resolvedMime: String,
    val mimeSource: PinImageMimeSource,
    /** getType(uri) 원본 (null이면 시스템이 MIME을 주지 않은 경우) */
    val contentResolverMime: String?,
    /** 파일 앞 16바이트 hex — 시그니처 판별·fallback 원인 분석용 */
    val header: String,
)

object PinImageUploadDiagnostics {
    private const val TAG = "PIN_IMAGE_UPLOAD"

    fun formatHeaderHex(bytes: ByteArray, length: Int = 16): String =
        bytes.take(length).joinToString("") { "%02X".format(it) }

    fun logMimeFallback(
        resolution: PinImageMimeResolution,
        fileName: String,
        uriScheme: String,
        bytes: ByteArray? = null,
    ) {
        if (resolution.source != PinImageMimeSource.FALLBACK_JPEG) return
        val header = bytes?.let { formatHeaderHex(it) } ?: "unknown"
        val resolverMime = resolution.contentResolverMime?.ifBlank { null } ?: "null"
        Log.w(
            TAG,
            "mime_inference_fallback resolverMime=$resolverMime fileName=$fileName " +
                "uriScheme=$uriScheme resolvedMime=${resolution.mimeType} header=$header",
        )
    }

    fun logUploadFailure(
        httpStatus: Int?,
        serverCode: String?,
        metas: List<PinImageUploadMeta>,
        responseBody: String? = null,
    ) {
        if (!responseBody.isNullOrBlank()) {
            Log.w(
                TAG,
                "upload_failed_response status=$httpStatus code=${serverCode ?: "null"} body=$responseBody",
            )
        }
        metas.forEachIndexed { index, meta ->
            Log.w(
                TAG,
                "upload_failed status=$httpStatus code=$serverCode index=$index " +
                    "mime=${meta.resolvedMime} resolverMime=${meta.contentResolverMime ?: "null"} " +
                    "file=${meta.fileName} scheme=${meta.uriScheme} mimeSource=${meta.mimeSource} " +
                    "header=${meta.header}",
            )
        }
        if (metas.isEmpty()) {
            Log.w(TAG, "upload_failed status=$httpStatus code=$serverCode imageCount=0")
        }
    }
}
