                                                                                                                                                                package com.issueissyu.fe.ui.screens.map

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.Canvas
import android.graphics.Paint
import android.util.LruCache
import androidx.core.content.ContextCompat
import kotlin.math.ceil
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin

data class PinMarkerBitmapStyle(
    val bitmap: Bitmap,
    val anchorY: Float,
)

object PinMarkerBitmapCache {
    private const val MAX_ENTRIES = 24
    private const val STYLE_VERSION = 5

    private val cache = object : LruCache<String, PinMarkerBitmapStyle>(MAX_ENTRIES) {
        override fun sizeOf(key: String, value: PinMarkerBitmapStyle): Int = 1
    }

    fun get(context: Context, iconRes: Int): PinMarkerBitmapStyle {
        val densityKey = context.resources.displayMetrics.density
        val key = "$iconRes:$densityKey:v$STYLE_VERSION"
        synchronized(cache) {
            cache.get(key)?.let { return it }
        }

        val bitmap = createBitmap(context, iconRes)
        synchronized(cache) {
            cache.put(key, bitmap)
        }
        return bitmap
    }

    private fun createBitmap(context: Context, iconRes: Int): PinMarkerBitmapStyle {
        val drawable = ContextCompat.getDrawable(context, iconRes)
            ?: return PinMarkerBitmapStyle(
                bitmap = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888),
                anchorY = 1f,
            )

        val density = context.resources.displayMetrics.density
        val iconWidth = drawable.intrinsicWidth.coerceAtLeast((16 * density).toInt())
        val iconHeight = drawable.intrinsicHeight.coerceAtLeast((16 * density).toInt())
        val sourceInset = ceil(2f * density).toInt()
        val sourceWidth = iconWidth + (sourceInset * 2)
        val sourceHeight = iconHeight + (sourceInset * 2)
        val outlineRadiusPx = (0.85f * density).coerceAtLeast(1f)
        val outlineRadiusInt = outlineRadiusPx.roundToInt().coerceAtLeast(1)
        val outlinePadding = ceil(outlineRadiusPx + 2f * density).toInt()
        val bitmapWidth = sourceWidth + (outlinePadding * 2)
        val bitmapHeight = sourceHeight + (outlinePadding * 2)
        val bitmap = Bitmap.createBitmap(bitmapWidth, bitmapHeight, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        val iconBitmap = Bitmap.createBitmap(sourceWidth, sourceHeight, Bitmap.Config.ARGB_8888)
        val iconCanvas = Canvas(iconBitmap)
        drawable.setBounds(
            sourceInset,
            sourceInset,
            sourceInset + iconWidth,
            sourceInset + iconHeight,
        )
        drawable.draw(iconCanvas)

        // Draw a crisp dark outline (without blur).
        val outlineMask = iconBitmap.extractAlpha()
        val outlinePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#3A3A3A")
        }
        val angleSteps = 32
        for (radius in 1..outlineRadiusInt) {
            for (step in 0 until angleSteps) {
                val angle = (step.toDouble() / angleSteps.toDouble()) * (Math.PI * 2.0)
                val dx = (cos(angle) * radius).toFloat()
                val dy = (sin(angle) * radius).toFloat()
                canvas.drawBitmap(
                    outlineMask,
                    outlinePadding + dx,
                    outlinePadding + dy,
                    outlinePaint,
                )
            }
        }
        canvas.drawBitmap(
            iconBitmap,
            outlinePadding.toFloat(),
            outlinePadding.toFloat(),
            null,
        )
        val anchorY = (outlinePadding + sourceInset + iconHeight).toFloat() / bitmapHeight.toFloat()
        return PinMarkerBitmapStyle(bitmap = bitmap, anchorY = anchorY)
    }
}
