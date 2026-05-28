package com.issueissyu.fe.core.media

import android.content.Context
import android.net.Uri
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
            val bytes = context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
                ?: return Result.failure(IllegalArgumentException("첨부한 사진을 읽을 수 없습니다."))
            totalBytes += bytes.size
            if (totalBytes > PinImageUploadConstraints.MAX_TOTAL_BYTES) {
                return Result.failure(IllegalArgumentException("사진 전체 용량은 45MB를 넘을 수 없습니다."))
            }
        }

        return Result.success(Unit)
    }
}
