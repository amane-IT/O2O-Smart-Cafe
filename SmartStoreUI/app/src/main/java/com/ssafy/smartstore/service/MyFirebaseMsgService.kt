package com.ssafy.smartstore.service

import android.app.PendingIntent
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.ssafy.smartstore.MainActivity
import com.ssafy.smartstore.R

// Firebase에서 보낸 메시지를 받아서 Notification에 전달하는 서비스
class MyFirebaseMsgService : FirebaseMessagingService() {
    override fun onNewToken(token: String) {
        super.onNewToken(token)
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        remoteMessage.notification.let { message ->
            val msgTitle = message!!.title
            val msgContent = message!!.body
            val mainIntent = Intent(this, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
            val mainPendingIntent: PendingIntent = PendingIntent.getActivity(this, 0, mainIntent, 0)
            val builder1 = NotificationCompat.Builder(this, MainActivity.channel_id)
                .setSmallIcon(R.drawable.logo)
                .setContentTitle(msgTitle)
                .setContentText(msgContent)
                .setContentIntent(mainPendingIntent)

            NotificationManagerCompat.from(this).apply {
                notify(101, builder1.build())
            }
        }
    }
}