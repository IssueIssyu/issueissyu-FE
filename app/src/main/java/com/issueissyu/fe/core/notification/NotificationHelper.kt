package com.issueissyu.fe.core.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.issueissyu.fe.MainActivity
import com.issueissyu.fe.R

object NotificationHelper {

    const val CHANNEL_LIKE = "channel_like"
    const val CHANNEL_EVENT = "channel_event"
    const val CHANNEL_POPULAR = "channel_popular"
    const val CHANNEL_STORE_AD = "channel_store_ad"

    //앱 시작 -> 알림 채널 생성
    fun createChannel(context: Context) {
        val manager = context.getSystemService(NotificationManager::class.java)

        listOf(
            NotificationChannel(CHANNEL_LIKE, "내 핀 좋아요", NotificationManager.IMPORTANCE_HIGH),
            NotificationChannel(CHANNEL_EVENT, "이벤트", NotificationManager.IMPORTANCE_HIGH),
            NotificationChannel(CHANNEL_POPULAR, "인기 게시글", NotificationManager.IMPORTANCE_HIGH),
            NotificationChannel(CHANNEL_STORE_AD, "가게 홍보", NotificationManager.IMPORTANCE_HIGH),
            ).forEach { manager.createNotificationChannel(it) }
    }

    //화면에 띄우는 알림 팝업
    fun show(context: Context, type: String?, targetId: String?, title: String, body: String) {
        //type으로 channel 분기
        val channelId = when (type) {
            "PIN_LIKED" -> CHANNEL_LIKE
            "PIN_EVENT" -> CHANNEL_EVENT
            "PIN_POPULAR" -> CHANNEL_POPULAR
            "PIN_STORE_AD" -> CHANNEL_STORE_AD
            else -> CHANNEL_LIKE
        }

        val intent = Intent(context, MainActivity::class.java).apply {
            putExtra("type", type)
            putExtra("targetId", targetId)
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
        }

        val pendingIntent = PendingIntent.getActivity(
            context, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, channelId)
            .setContentTitle(title)
            .setContentText(body)
            .setSmallIcon(R.drawable.ic_report) // 나중에 아이콘 교체
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        if (ActivityCompat.checkSelfPermission(
                context,
                android.Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) return

        NotificationManagerCompat.from(context).notify(getNotificationId(type), notification)
    }

    //핀 좋아요 -> 덮어씌워서 보여주기
    private fun getNotificationId(type: String?): Int{
        return when(type){
            "PIN_LIKED" -> 1001
            else -> (System.currentTimeMillis() and 0x7FFFFFFF).toInt()
        }
    }
}