package com.img.locationupdateapp

import android.Manifest
import android.annotation.TargetApi
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.Location
import android.os.Build
import android.os.IBinder
import android.os.Looper
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority

class ActivityTrackingService: Service() {
    private var notification: NotificationCompat.Builder? = null
    private lateinit var workManager: WorkManager

    override fun onBind(intent: Intent): IBinder? { return null }
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)

        val workRequest = OneTimeWorkRequest.Builder(LocationUpdateWorker::class.java).build()
        workManager = WorkManager.getInstance(applicationContext)
        workManager.enqueue(workRequest)

        val notificationId = 1001
        notification = createLocationNotification("Track Activity","update data here")
        startForeground(notificationId, notification?.build())

        return START_STICKY
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        super.onTaskRemoved(rootIntent)
        Log.d("taskRemove", "onTaskRemoved: ")
    }

    private fun createLocationNotification (contentTitle: String, contentText: String) : NotificationCompat.Builder {

        val chatIntent = Intent(this, MainActivity::class.java)
        chatIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
        val locationPendingIntent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S){
            PendingIntent.getActivity(applicationContext, 100,
                chatIntent, PendingIntent.FLAG_IMMUTABLE)
        } else {
            PendingIntent.getActivity(applicationContext, 100,
                chatIntent, PendingIntent.FLAG_UPDATE_CURRENT)
        }

        val notification = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createNotificationWithChannel(createNotificationChannel(),contentTitle,contentText,locationPendingIntent)
        } else {
            NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.ic_launcher_background)
                .setContentTitle(contentTitle)
                .setContentText(contentText)
                .setAutoCancel(false)
                .setOngoing(true)
                .setContentIntent(locationPendingIntent)
                .setGroup("locationUpdate")
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setColor(Color.rgb(214, 10, 37))
        }
        return notification

    }

    private fun createNotificationWithChannel(
        channelId: String,
        contentTitle: String,
        contentText: String,
        pendingIntent: PendingIntent
    ): NotificationCompat.Builder {

        return NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(contentTitle)
            .setContentText(contentText)
            .setAutoCancel(false)
            .setOngoing(true)
            .setContentIntent(pendingIntent)
            .setGroup("locationUpdate")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
    }

    @TargetApi(Build.VERSION_CODES.O)
    private fun createNotificationChannel() : String{
        val channelId = "location"
        val channelName = "locationUpdate"
        val importance = NotificationManager.IMPORTANCE_HIGH
        val locationChannel = NotificationChannel(channelId, channelName, importance)
        locationChannel.lightColor = ContextCompat.getColor(this,R.color.white)
        locationChannel.lockscreenVisibility = Notification.VISIBILITY_PRIVATE
        val notificationManager = getSystemService(NotificationManager::class.java)
        notificationManager.createNotificationChannel(locationChannel)

        return channelId
    }

    override fun onDestroy() {
        workManager.cancelAllWork()
        stopSelf()
        super.onDestroy()
    }
}