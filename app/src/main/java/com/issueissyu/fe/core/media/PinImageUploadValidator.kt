package com.issueissyu.fe.core.media

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import com.issueissyu.fe.core.constants.PinImageUploadConstraints

object PinImageUploadValidator {

    fun validate(context: Context, imageUris: List<String>): Result<Unit> {
        if (imageUris.size > PinImageUploadConstraints.MAX_COUNT) {
            return Result.failure(
                IllegalArgumentException(
                    "사진은 최대 ${PinImageUploadConstraints.MAX_COUNT}장까지 첨부할 수 있습니다.",
                ),
            )
        }

        var totalBytes = 0L
        imageUris.forEach { uriString ->
            val uri = Uri.parse(uriString)
            val bytes = resolveUriSizeBytes(context, uri)
                ?: return Result.failure(IllegalArgumentException("첨부한 사진을 읽을 수 없습니다."))
            if (bytes <= 0L) {
                return Result.failure(IllegalArgumentException("첨부한 사진을 확인할 수 없습니다."))
            }
            totalBytes += bytes
            if (totalBytes > PinImageUploadConstraints.MAX_TOTAL_BYTES) {
                return Result.failure(IllegalArgumentException("사진 전체 용량은 45MB를 넘을 수 없습니다."))
            }
        }

        return Result.success(Unit)
    }

    private fun resolveUriSizeBytes(context: Context, uri: Uri): Long? {
        context.contentResolver.query(
            uri,
            arrayOf(OpenableColumns.SIZE),
            null,
            null,
            null,
        )?.use { cursor ->
            if (cursor.moveToFirst()) {
                val index = cursor.getColumnIndex(OpenableColumns.SIZE)
                if (index >= 0 && !cursor.isNull(index)) {
                    val size = cursor.getLong(index)
                    if (size > 0L) return size
                }
            }
        }

        return runCatching {
            context.contentResolver.openAssetFileDescriptor(uri, "r")?.use { descriptor ->
                descriptor.length.takeIf { length -> length > 0L }
            }
        }.getOrNull()
    }
}
