package com.example.campusconnect

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class MyFirebaseMessagingService : FirebaseMessagingService() {

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)

        Log.d("FCM", "From: ${remoteMessage.from}")
        Log.d("FCM", "Message data payload: ${remoteMessage.data}")

        val data = remoteMessage.data
        val title = data["title"] ?: "New Message"
        val message = data["message"] ?: ""
        val type = data["type"]
        val senderId = data["senderId"]

        sendNotification(title, message, type, senderId)
    }

    private fun sendNotification(
        title: String,
        messageBody: String,
        type: String?,
        senderId: String?
    ) {
        val channelId = "CampusConnectChannel"

        val intent = Intent(this, HomeActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE
        )

        val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

        val notificationBuilder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.ic_chat)
            .setContentTitle(title)
            .setContentText(messageBody)
            .setAutoCancel(true)
            .setSound(defaultSoundUri)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_HIGH)

        if (type == "friend_request" && senderId != null) {
            // Create intent for "Accept"
            val acceptIntent = Intent(this, NotificationActionService::class.java).apply {
                action = "accept"
                putExtra("action", "accept")
                putExtra("type", "friend_request")
                putExtra("senderId", senderId)
            }
            val acceptPendingIntent = PendingIntent.getService(
                this, 1, acceptIntent,
                PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE
            )
            notificationBuilder.addAction(
                R.drawable.ic_check,
                "Accept",
                acceptPendingIntent
            )

            val declineIntent = Intent(this, NotificationActionService::class.java).apply {
                action = "decline"
                putExtra("action", "decline")
                putExtra("type", "friend_request")
                putExtra("senderId", senderId)
            }
            val declinePendingIntent = PendingIntent.getService(
                this, 2, declineIntent,
                PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE
            )
            notificationBuilder.addAction(
                R.drawable.ic_close,
                "Decline",
                declinePendingIntent
            )
        }

        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Campus Connect Notifications",
                NotificationManager.IMPORTANCE_HIGH
            )
            notificationManager.createNotificationChannel(channel)
        }

        notificationManager.notify(System.currentTimeMillis().toInt(), notificationBuilder.build())
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d("FCM", "Refreshed token: $token")
    }
}