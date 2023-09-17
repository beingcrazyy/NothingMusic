package com.example.nothingmusic

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.graphics.BitmapFactory
import android.media.MediaPlayer
import android.os.Binder
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.support.v4.media.session.MediaSessionCompat
import androidx.core.app.NotificationCompat

class MusicService :  Service() {
    private var myBinder = MyBinder()
    var mediaPlayer : MediaPlayer? = null
    private lateinit var mediaSession: MediaSessionCompat
    private  lateinit var runnable: Runnable

    override fun onBind(intent: Intent?): IBinder {
        mediaSession= MediaSessionCompat(baseContext,"My Music")
        return myBinder
    }

    inner class MyBinder : Binder(){
        fun currentService() : MusicService{
            return  this@MusicService
        }
    }

    @SuppressLint("UnspecifiedImmutableFlag", "ResourceAsColor")
    fun showNotification(playPauseBtn : Int){

        val favIntent = Intent(baseContext, NotificationReciever::class.java).setAction(ApplicationClass.FAVOURITE)
        val favPendingIntent = PendingIntent.getBroadcast(baseContext,0,favIntent, PendingIntent.FLAG_IMMUTABLE)

        val prevIntent = Intent(baseContext, NotificationReciever::class.java).setAction(ApplicationClass.PREVIOUS)
        val prevPendingIntent = PendingIntent.getBroadcast(baseContext,0,prevIntent, PendingIntent.FLAG_IMMUTABLE)

        val playIntent = Intent(baseContext, NotificationReciever::class.java).setAction(ApplicationClass.PLAY)
        val playPendingIntent = PendingIntent.getBroadcast(baseContext,0,playIntent, PendingIntent.FLAG_IMMUTABLE)

        val nextIntent = Intent(baseContext, NotificationReciever::class.java).setAction(ApplicationClass.NEXT)
        val nextPendingIntent = PendingIntent.getBroadcast(baseContext,0,nextIntent, PendingIntent.FLAG_IMMUTABLE)

        val addIntent = Intent(baseContext, NotificationReciever::class.java).setAction(ApplicationClass.ADD_TO_PLAYLIST)
        val addPendingIntent = PendingIntent.getBroadcast(baseContext,0,addIntent, PendingIntent.FLAG_IMMUTABLE)

        val imageArt = getImageArt(PlayerActivity.musicListPlayerActivity[PlayerActivity.songPosition].path)
        val image = if(imageArt  != null){
            BitmapFactory.decodeByteArray(imageArt,0,imageArt.size)
        }
        else{
            BitmapFactory.decodeResource(resources,R.drawable.image_content)
        }


        val notificationTitle = PlayerActivity.musicListPlayerActivity[PlayerActivity.songPosition].title
        val maxTitleLength = 30
        val truncatedTitle = if (notificationTitle.length > maxTitleLength) {
            notificationTitle.substring(0, maxTitleLength) + "..."
        } else {
            notificationTitle
        }

        val notification = NotificationCompat.Builder(baseContext,ApplicationClass.CHANNEL_ID)
            .setContentTitle(truncatedTitle)
            .setContentText(PlayerActivity.musicListPlayerActivity[PlayerActivity.songPosition].album)
            .setSmallIcon(R.drawable.header_logo_element)
            .setLargeIcon(image)
            .setStyle(androidx.media.app.NotificationCompat.MediaStyle().setMediaSession(mediaSession.sessionToken))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setColor(R.color.black)
            .setOnlyAlertOnce(true)
            .addAction(R.drawable.fav_empty_icon,"favourites",favPendingIntent)
            .addAction(R.drawable.prev_icon,"previous",prevPendingIntent)
            .addAction(playPauseBtn,"play",playPendingIntent)
            .addAction(R.drawable.next_icon,"next",nextPendingIntent)
            .addAction(R.drawable.add_playlist_icon,"add_to_playlist",addPendingIntent)
            .setOngoing(true)
            .build()

        startForeground(13,notification)

    }


    fun createMediaPlayer(){
        if (PlayerActivity.musicService!!.mediaPlayer != null) {
            PlayerActivity.musicService!!.mediaPlayer!!.stop()
            PlayerActivity.musicService!!.mediaPlayer!!.release()
            PlayerActivity.musicService!!.mediaPlayer = null
        }
        try {
            PlayerActivity.musicService!!.mediaPlayer = MediaPlayer()
            PlayerActivity.musicService!!.mediaPlayer!!.setDataSource(PlayerActivity.musicListPlayerActivity[PlayerActivity.songPosition].path)
            PlayerActivity.musicService!!.mediaPlayer!!.prepare()
            PlayerActivity.musicService!!.mediaPlayer!!.start()

            PlayerActivity.isPlaying = true
            PlayerActivity.binding.playPauseButton.setImageResource(R.drawable.pause)
            PlayerActivity.musicService!!.showNotification(R.drawable.pause_icon)

            PlayerActivity.binding.musicStartTime.text = formatDuration(PlayerActivity.musicService!!.mediaPlayer!!.currentPosition.toLong())
            PlayerActivity.binding.musicEndTime.text = formatDuration(PlayerActivity.musicService!!.mediaPlayer!!.duration.toLong())
            PlayerActivity.binding.seekBar.progress = 0
            PlayerActivity.binding.seekBar.max = PlayerActivity.musicService!!.mediaPlayer!!.duration
        }
        catch (e: java.lang.Exception) {return}
    }


    fun seekbarSetup(){
        runnable = Runnable {
            PlayerActivity.binding.musicStartTime.text = formatDuration(mediaPlayer!!.currentPosition.toLong())
            PlayerActivity.binding.seekBar.progress = mediaPlayer!!.currentPosition
            Handler(Looper.getMainLooper()).postDelayed(runnable,200)
        }
        Handler(Looper.getMainLooper()).postDelayed(runnable,0)
    }


}