package dev.falcon.garagefinderpro

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.util.Log
import com.google.firebase.messaging.FirebaseMessagingService
import io.karn.notify.Notify
import io.karn.notify.entities.Payload

class FCMNotificationService : FirebaseMessagingService() {
    override fun onMessageReceived(remoteMessage: com.google.firebase.messaging.RemoteMessage) {
        super.onMessageReceived(remoteMessage)

        val notificationManager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                Notify.CHANNEL_DEFAULT_KEY,
                "Default Channel",
                NotificationManager.IMPORTANCE_HIGH
            )
            notificationManager.createNotificationChannel(channel)
        }

        Notify.defaultConfig {
            header {
                color = resources.getColor(R.color.grey2)
                icon = R.drawable.garage
                showTimestamp = true
            }
            alerting(Notify.CHANNEL_DEFAULT_KEY) {
                lightColor = resources.getColor(R.color.grey2)
                Log.d("FCM", Notify.IMPORTANCE_MAX.toString())
                channelImportance = Notify.IMPORTANCE_MAX
            }
        }

        Log.d("FCM", remoteMessage.data.toString())

        if (remoteMessage.data["notificationType"]=="sendRequest") {
            Log.d("FCM", "sendRequest")

            Notify.with(applicationContext)
                .asBigText {
                    title = remoteMessage.data["title"].toString()
                    bigText = "\nDetails:\n" +
                            "Service Type: " + remoteMessage.data["serviceType"].toString() +
                            "\nVehicle Name: " + remoteMessage.data["vehicleName"].toString() +
                            " " + remoteMessage.data["vehicleType"].toString() +
                            "\nFuel Type: " + remoteMessage.data["vehicleFuelType"].toString() +
                            "\nVehicle Model: " + remoteMessage.data["vehicleModel"].toString() +
                            "\nTow Service: " + remoteMessage.data["towLocation"].toString()
                    expandedText = remoteMessage.data["body"].toString()
                }
                .show()
        }
    }
}

