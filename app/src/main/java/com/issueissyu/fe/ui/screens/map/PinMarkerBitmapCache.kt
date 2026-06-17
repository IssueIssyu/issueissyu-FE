                                                                                                                                                                package com.issueissyu.fe.ui.screens.map

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.Canvas
import android.graphics.Paint
import android.util.LruCache
import com.issueissyu.fe.R
import androidx.core.content.ContextCompat
import kotlin.math.ceil
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.roundToInt
import kotlin.math.sin

data class PinMarkerBitmapStyle(
    val bitmap: Bitmap,
    val anchorY: Float,
)

object PinMarkerBitmapCache {
    private const val MAX_ENTRIES = 24
    private const val STYLE_VERSION = 12
    private const val SALE_BADGE_SIZE_RATIO = 0.62f
    private const val SALE_BADGE_OVERLAP_RATIO = 0.52f

    private val cache = object : LruCache<String, PinMarkerBitmapStyle>(MAX_ENTRIES) {
        override fun sizeOf(key: String, value: PinMarkerBitmapStyle): Int = 1
    }

    fun get(
        context: Context,
        iconRes: Int,
        hasDiscount: Boolean = false,
    ): PinMarkerBitmapStyle {
        val densityKey = context.resources.displayMetrics.density
        val key = "$iconRes:$densityKey:$hasDiscount:v$STYLE_VERSION"
        synchronized(cache) {
            cache.get(key)?.let { return it }
        }

        val bitmap = createBitmap(
            context = context,
            iconRes = iconRes,
            hasDiscount = hasDiscount,
        )
        synchronized(cache) {
            cache.put(key, bitmap)
        }
        return bitmap
    }

    private fun createBitmap(
        context: Context,
        iconRes: Int,
        hasDiscount: Boolean,
    ): PinMarkerBitmapStyle {
        val drawable = ContextCompat.getDrawable(context, iconRes)
            ?: return PinMarkerBitmapStyle(
                bitmap = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888),
                anchorY = 1f,
            )
        val saleBadgeDrawable = if (hasDiscount) {
            ContextCompat.getDrawable(context, R.drawable.ic_on_sale)
        } else {
            null
        }
        val shouldDrawSaleBadge = hasDiscount && saleBadgeDrawable != null

        val density = context.resources.displayMetrics.density
        val iconWidth = drawable.intrinsicWidth.coerceAtLeast((16 * density).toInt())
        val iconHeight = drawable.intrinsicHeight.coerceAtLeast((16 * density).toInt())
        val sourceInset = ceil(2f * density).toInt()
        val sourceWidth = iconWidth + (sourceInset * 2)
        val sourceHeight = iconHeight + (sourceInset * 2)
        val outlineRadiusPx = (0.85f * density).coerceAtLeast(1f)
        val outlineRadiusInt = outlineRadiusPx.roundToInt().coerceAtLeast(1)
        val outlinePadding = ceil(2f * density).toInt()
        val minBadgeSize = (12f * density).roundToInt().coerceAtLeast(1)
        val badgeSize = if (shouldDrawSaleBadge) {
            max((sourceWidth * SALE_BADGE_SIZE_RATIO).roundToInt(), minBadgeSize)
        } else {
            0
        }
        val badgeLift = if (shouldDrawSaleBadge) {
            (badgeSize * SALE_BADGE_OVERLAP_RATIO).roundToInt()
        } else {
            0
        }
        val badgeTopPadding = if (shouldDrawSaleBadge) {
            badgeLift + ceil(2f * density).toInt()
        } else {
            0
        }
        val contentLeft = outlinePadding
        val contentTop = outlinePadding + badgeTopPadding
        val bitmapWidth = sourceWidth + (outlinePadding * 2)
        val bitmapHeight = sourceHeight + (outlinePadding * 2) + badgeTopPadding
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

        // Inside-aligned stroke per alpha island so internal gaps stay visible.
        val outlinePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#3A3A3A")
        }
        iconBitmap.extractAlphaComponents().forEach { componentMask ->
            drawInsideOutline(
                canvas = canvas,
                outlineMask = componentMask.extractAlpha(),
                left = contentLeft.toFloat(),
                top = contentTop.toFloat(),
                outlineRadiusInt = outlineRadiusInt,
                paint = outlinePaint,
            )
        }
        canvas.drawBitmap(
            iconBitmap,
            contentLeft.toFloat(),
            contentTop.toFloat(),
            null,
        )

        if (shouldDrawSaleBadge) {
            saleBadgeDrawable?.let { badge ->
                val badgeLeft = contentLeft + ((sourceWidth - badgeSize) / 2f)
                val badgeTop = (contentTop - (badgeSize * SALE_BADGE_OVERLAP_RATIO)).coerceAtLeast(0f)
                badge.setBounds(
                    badgeLeft.roundToInt(),
                    badgeTop.roundToInt(),
                    (badgeLeft + badgeSize).roundToInt(),
                    (badgeTop + badgeSize).roundToInt(),
                )
                badge.draw(canvas)
            }
        }

        val anchorY = (contentTop + sourceInset + iconHeight).toFloat() / bitmapHeight.toFloat()
        return PinMarkerBitmapStyle(bitmap = bitmap, anchorY = anchorY)
    }

    private fun drawInsideOutline(
        canvas: Canvas,
        outlineMask: Bitmap,
        left: Float,
        top: Float,
        outlineRadiusInt: Int,
        paint: Paint,
    ) {
        val angleSteps = 32
        for (radius in -outlineRadiusInt..-1) {
            for (step in 0 until angleSteps) {
                val angle = (step.toDouble() / angleSteps.toDouble()) * (Math.PI * 2.0)
                val dx = (cos(angle) * radius).toFloat()
                val dy = (sin(angle) * radius).toFloat()
                canvas.drawBitmap(
                    outlineMask,
                    left + dx,
                    top + dy,
                    paint,
                )
            }
        }
    }

    private fun Bitmap.extractAlphaComponents(alphaThreshold: Int = 128): List<Bitmap> {
        val width = width
        val height = height
        if (width == 0 || height == 0) return emptyList()

        val sourcePixels = IntArray(width * height)
        getPixels(sourcePixels, 0, width, 0, 0, width, height)
        val visited = BooleanArray(width * height)
        val components = mutableListOf<Bitmap>()

        fun index(x: Int, y: Int) = y * width + x
        fun isOpaque(x: Int, y: Int) = (sourcePixels[index(x, y)] ushr 24) >= alphaThreshold

        for (y in 0 until height) {
            for (x in 0 until width) {
                val startIndex = index(x, y)
                if (visited[startIndex] || !isOpaque(x, y)) continue

                val componentPixels = IntArray(width * height)
                val queue = ArrayDeque<Pair<Int, Int>>()
                queue.add(x to y)
                visited[startIndex] = true

                while (queue.isNotEmpty()) {
                    val (currentX, currentY) = queue.removeFirst()
                    val currentIndex = index(currentX, currentY)
                    componentPixels[currentIndex] = sourcePixels[currentIndex]

                    val neighbors = listOf(
                        currentX - 1 to currentY,
                        currentX + 1 to currentY,
                        currentX to currentY - 1,
                        currentX to currentY + 1,
                    )
                    for ((neighborX, neighborY) in neighbors) {
                        if (neighborX !in 0 until width || neighborY !in 0 until height) continue
                        val neighborIndex = index(neighborX, neighborY)
                        if (visited[neighborIndex] || !isOpaque(neighborX, neighborY)) continue
                        visited[neighborIndex] = true
                        queue.add(neighborX to neighborY)
                    }
                }

                val component = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
                component.setPixels(componentPixels, 0, width, 0, 0, width, height)
                components.add(component)
            }
        }

        return components
    }
}
