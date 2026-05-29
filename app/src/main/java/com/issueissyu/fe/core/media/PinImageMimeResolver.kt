package com.issueissyu.fe.core.media

import android.content.Context
import android.net.Uri
import android.webkit.MimeTypeMap

enum class PinImageMimeSource {
    CONTENT_RESOLVER,
    EXTENSION,
    BYTE_SIGNATURE,
    FALLBACK_JPEG,
}

data class PinImageMimeResolution(
    val mimeType: String,
    val source: PinImageMimeSource,
    // ContentResolver.getType(uri) 원본. null·빈값·image/*면 2·3단계로 넘어간 경우.
    val contentResolverMime: String?,
)

object PinImageMimeResolver {

    fun resolve(
        context: Context,
        uri: Uri,
        fileName: String,
        bytes: ByteArray,
    ): PinImageMimeResolution {
        val resolverMime = context.contentResolver.getType(uri)
        if (!resolverMime.isNullOrBlank() && resolverMime != "image/*") {
            return PinImageMimeResolution(
                mimeType = resolverMime,
                source = PinImageMimeSource.CONTENT_RESOLVER,
                contentResolverMime = resolverMime,
            )
        }

        val extension = fileName.substringAfterLast('.', "").lowercase()
        if (extension.isNotBlank()) {
            val extensionMime = when (extension) {
                "jpg", "jpeg" -> "image/jpeg"
                "png" -> "image/png"
                "gif" -> "image/gif"
                "webp" -> "image/webp"
                "heic" -> "image/heic"
                "heif" -> "image/heif"
                else -> MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension)
            }
            if (!extensionMime.isNullOrBlank()) {
                return PinImageMimeResolution(
                    mimeType = extensionMime,
                    source = PinImageMimeSource.EXTENSION,
                    contentResolverMime = resolverMime,
                )
            }
        }

        val signatureMime = sniffImageMimeType(bytes)
        if (signatureMime != null) {
            return PinImageMimeResolution(
                mimeType = signatureMime,
                source = PinImageMimeSource.BYTE_SIGNATURE,
                contentResolverMime = resolverMime,
            )
        }

        return PinImageMimeResolution(
            mimeType = "image/jpeg",
            source = PinImageMimeSource.FALLBACK_JPEG,
            contentResolverMime = resolverMime,
        )
    }

    fun ensureExtension(fileName: String, mimeType: String): String {
        if (fileName.contains('.')) return fileName
        val extension = when (mimeType.lowercase()) {
            "image/png" -> "png"
            "image/webp" -> "webp"
            "image/gif" -> "gif"
            "image/heic" -> "heic"
            "image/heif" -> "heif"
            else -> "jpg"
        }
        return "$fileName.$extension"
    }

    private fun sniffImageMimeType(bytes: ByteArray): String? {
        if (bytes.size >= 3 &&
            bytes[0] == 0xFF.toByte() &&
            bytes[1] == 0xD8.toByte() &&
            bytes[2] == 0xFF.toByte()
        ) {
            return "image/jpeg"
        }

        if (bytes.size >= 8 &&
            bytes[0] == 0x89.toByte() &&
            bytes[1] == 0x50.toByte() &&
            bytes[2] == 0x4E.toByte() &&
            bytes[3] == 0x47.toByte() &&
            bytes[4] == 0x0D.toByte() &&
            bytes[5] == 0x0A.toByte() &&
            bytes[6] == 0x1A.toByte() &&
            bytes[7] == 0x0A.toByte()
        ) {
            return "image/png"
        }

        if (bytes.size >= 6) {
            val header = bytes.copyOfRange(0, 6).decodeToString()
            if (header == "GIF87a" || header == "GIF89a") {
                return "image/gif"
            }
        }

        if (bytes.size >= 12) {
            val riff = bytes.copyOfRange(0, 4).decodeToString()
            val webp = bytes.copyOfRange(8, 12).decodeToString()
            if (riff == "RIFF" && webp == "WEBP") {
                return "image/webp"
            }
        }

        if (bytes.size >= 12) {
            val boxType = bytes.copyOfRange(4, 8).decodeToString()
            if (boxType == "ftyp") {
                val majorBrand = bytes.copyOfRange(8, 12).decodeToString().lowercase()
                if (majorBrand.startsWith("hei") || majorBrand.startsWith("hev")) {
                    return "image/heic"
                }
                if (majorBrand.startsWith("mif")) {
                    return "image/heif"
                }
            }
        }

        return null
    }
}
