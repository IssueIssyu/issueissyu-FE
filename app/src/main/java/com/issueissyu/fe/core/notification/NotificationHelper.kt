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

    const val CHANNEL_ID = "issueissyu_channel"

    //앱 시작 -> 알림 채널 생성 (1회)
    fun createChannel(context: Context) {
        val channel = NotificationChannel(
            CHANNEL_ID,
            "이슈이슈 알림",
            NotificationManager.IMPORTANCE_DEFAULT
        )
        context.getSystemService(NotificationManager::class.java)
            .createNotificationChannel(channel)
    }

    //화면에 띄우는 알림 팝업
    fun show(context: Context, type: String?, targetId: String?, title: String, body: String) {
        val intent = Intent(context, MainActivity::class.java).apply {
            putExtra("type", type)
            putExtra("targetId", targetId)
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            context, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
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

            //TODO: 타입값은 백엔드랑 맞춰야 함
            "PIN_LIKED" -> 1001
            "EVENT" -> System.currentTimeMillis().toInt()
            "POPULAR_POST" -> System.currentTimeMillis().toInt()
            "STORE_PROMO" -> System.currentTimeMillis().toInt()
            else -> System.currentTimeMillis().toInt()
        }
    }
}