package com.issueissyu.fe.core.media

import android.content.Context
import android.graphics.Bitmap
import androidx.core.net.toUri
import java.io.File
import java.io.FileOutputStream

object CapturedImageSaver {
    fun saveJpegToCache(
        context: Context,
        bitmap: Bitmap,
        fileNamePrefix: String,
    ): String? {
        return runCatching {
            val file = File(
                context.cacheDir,
                "$fileNamePrefix-${System.currentTimeMillis()}.jpg",
            )
            FileOutputStream(file).use { output ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, 92, output)
            }
            file.toUri().toString()
        }.getOrNull()
    }
}
