package com.example.nothingmusic

import android.media.MediaMetadataRetriever
import java.util.concurrent.TimeUnit
import kotlin.system.exitProcess

data class Music(val id : String, val title : String, val decription : String, val path : String, val duration: Long = 0, val album : String,
val artUri: String)

fun formatDuration(duration:Long) :String{
    val minutes = TimeUnit.MINUTES.convert(duration,TimeUnit.MILLISECONDS)
    val seconds = TimeUnit.SECONDS.convert(duration, TimeUnit.MILLISECONDS) % 60

    return String.format("%02d:%02d", minutes, seconds)
}

fun getImageArt(path: String): ByteArray? {
    val retriever = MediaMetadataRetriever()
    retriever.setDataSource(path)
    return retriever.embeddedPicture
}
fun exitApp(){
    PlayerActivity.musicService!!.stopForeground(true)
    PlayerActivity.musicService!!.mediaPlayer!!.release()
    PlayerActivity.musicService = null
    exitProcess(1)
}

