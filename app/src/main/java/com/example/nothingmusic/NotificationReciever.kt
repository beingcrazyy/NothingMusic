package com.example.nothingmusic

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.widget.Toast
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions

class NotificationReciever : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        when(intent?.action){
            ApplicationClass.FAVOURITE -> Toast.makeText(context,"Favourite Clicked", Toast.LENGTH_SHORT).show()

            ApplicationClass.PREVIOUS -> {
                if (PlayerActivity.songPosition == 0) PlayerActivity.songPosition =
                    PlayerActivity.musicListPlayerActivity.size - 1
                else --PlayerActivity.songPosition
                nextPrev(context = context!!)
            }

            ApplicationClass.PLAY ->
                if(PlayerActivity.isPlaying){
                    pauseMusic()
                }
                else{
                    playMusic()
                }

            ApplicationClass.NEXT -> {
                if (PlayerActivity.songPosition == PlayerActivity.musicListPlayerActivity.size - 1) PlayerActivity.songPosition =
                    0
                else ++PlayerActivity.songPosition
                nextPrev(context = context!!)
            }

            ApplicationClass.ADD_TO_PLAYLIST -> Toast.makeText(context,"Add to playlist Clicked", Toast.LENGTH_SHORT).show()

        }
    }

    private fun playMusic(){
        PlayerActivity.isPlaying = true
        PlayerActivity.musicService!!.mediaPlayer!!.start()
        PlayerActivity.musicService!!.showNotification(R.drawable.pause_icon)
        PlayerActivity.binding.playPauseButton.setImageResource(R.drawable.pause)
    }

    private fun pauseMusic(){
        PlayerActivity.isPlaying = false
        PlayerActivity.musicService!!.mediaPlayer!!.pause()
        PlayerActivity.musicService!!.showNotification(R.drawable.play_icon)
        PlayerActivity.binding.playPauseButton.setImageResource(R.drawable.play)
    }

    private fun nextPrev(context : Context){

        PlayerActivity.musicService!!.createMediaPlayer()
        Glide.with(context)
            .load(PlayerActivity.musicListPlayerActivity[PlayerActivity.songPosition].artUri)
            .apply(RequestOptions().placeholder(R.drawable.image_content).centerCrop())
            .into(PlayerActivity.binding.shapeableImageView)

        PlayerActivity.binding.musicTitle.text = PlayerActivity.musicListPlayerActivity[PlayerActivity.songPosition].title
        PlayerActivity.binding.musicDecription.text = PlayerActivity.musicListPlayerActivity[PlayerActivity.songPosition].decription
        PlayerActivity.binding.musicEndTime.text = formatDuration(PlayerActivity.musicListPlayerActivity[PlayerActivity.songPosition].duration)
        playMusic()
    }

}