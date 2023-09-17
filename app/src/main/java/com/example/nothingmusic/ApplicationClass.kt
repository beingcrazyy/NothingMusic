package com.example.nothingmusic

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager

class ApplicationClass : Application(){
    companion object{
        const val CHANNEL_ID = "channel"
        const val FAVOURITE = "favourite"
        const val PLAY = "play"
        const val NEXT = "next"
        const val PREVIOUS = "previous"
        const val ADD_TO_PLAYLIST = "add_to_playlist"

    }

    override fun onCreate() {
        super.onCreate()

        val notificationChannel = NotificationChannel(CHANNEL_ID,"Now playing ", NotificationManager.IMPORTANCE_LOW)
        notificationChannel.description = "This is a important channel for showing songs!!"
        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(notificationChannel)





    }
}