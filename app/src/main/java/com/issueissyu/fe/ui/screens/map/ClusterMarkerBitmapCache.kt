package com.issueissyu.fe.ui.screens.map

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.util.LruCache
import androidx.compose.ui.graphics.toArgb
import com.issueissyu.fe.domain.model.MapPinCluster
import com.issueissyu.fe.domain.model.pin.PinCategory
import com.issueissyu.fe.ui.theme.Communication
import com.issueissyu.fe.ui.theme.Festival
import com.issueissyu.fe.ui.theme.Issue
import com.issueissyu.fe.ui.theme.Shop

object ClusterMarkerBitmapCache {
    private const val MAX_ENTRIES = 64

    private val cache = object : LruCache<String, Bitmap>(MAX_ENTRIES) {
        override fun sizeOf(key: String, value: Bitmap) = 1
    }

    fun get(context: Context, cluster: MapPinCluster): Bitmap {
        val key = cacheKey(context, cluster)
        synchronized(cache) {
            cache.get(key)?.let { return it }
        }

        val bitmap = createClusterBitmap(context, cluster)
        synchronized(cache) {
            cache.put(key, bitmap)
        }
        return bitmap
    }

    private fun cacheKey(context: Context, cluster: MapPinCluster): String {
        val densityKey = context.resources.displayMetrics.density
        val categoryBreakdown = cluster.pins
            .groupingBy { it.category }
            .eachCount()
            .toSortedMap(compareBy(PinCategory::ordinal))
            .entries
            .joinToString(separator = "|") { (category, count) ->
                "${category.ordinal}:$count"
            }
        return "$densityKey:${cluster.pinCount}:$categoryBreakdown"
    }

    private fun createClusterBitmap(context: Context, cluster: MapPinCluster): Bitmap {
        val density = context.resources.displayMetrics.density
        val markerSize = (52 * density).toInt()
        val bitmap = Bitmap.createBitmap(markerSize, markerSize, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val center = markerSize / 2f

        val circlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = android.graphics.Color.rgb(72, 119, 255)
            style = Paint.Style.FILL
            setShadowLayer(4 * density, 0f, 2 * density, android.graphics.Color.argb(70, 0, 0, 0))
        }
        val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            strokeWidth = 4 * density
            strokeCap = Paint.Cap.BUTT
        }
        val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = android.graphics.Color.WHITE
            textAlign = Paint.Align.CENTER
            textSize = 17 * density
            typeface = android.graphics.Typeface.DEFAULT_BOLD
        }

        val radius = center - 5 * density
        canvas.drawCircle(center, center, radius, circlePaint)
        val categoryCounts = cluster.pins
            .groupingBy { it.category }
            .eachCount()
            .toSortedMap(compareBy(PinCategory::ordinal))
        val totalCategoryPins = categoryCounts.values.sum()
        val borderBounds = RectF(
            center - radius,
            center - radius,
            center + radius,
            center + radius,
        )
        if (totalCategoryPins == 0) {
            borderPaint.color = android.graphics.Color.WHITE
            canvas.drawCircle(center, center, radius, borderPaint)
        } else {
            var startAngle = -90f
            categoryCounts.forEach { (category, count) ->
                val sweepAngle = 360f * count / totalCategoryPins
                borderPaint.color = category.toClusterBorderColor()
                canvas.drawArc(borderBounds, startAngle, sweepAngle, false, borderPaint)
                startAngle += sweepAngle
            }
        }
        val textY = center - (textPaint.ascent() + textPaint.descent()) / 2f
        canvas.drawText(cluster.pinCount.toString(), center, textY, textPaint)
        return bitmap
    }

    private fun PinCategory.toClusterBorderColor(): Int {
        return when (this) {
            PinCategory.ISSUE -> Issue.toArgb()
            PinCategory.COMMUNICATION -> Communication.toArgb()
            PinCategory.SHOP -> Shop.toArgb()
            PinCategory.FESTIVAL -> Festival.toArgb()
        }
    }
}
