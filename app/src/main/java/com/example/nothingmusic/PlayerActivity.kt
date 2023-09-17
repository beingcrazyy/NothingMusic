package com.example.nothingmusic

import android.annotation.SuppressLint
import android.content.ComponentName
import android.content.Intent
import android.content.ServiceConnection
import android.graphics.Color
import android.media.MediaPlayer
import android.media.audiofx.AudioEffect
import android.net.Uri
import android.os.Bundle
import android.os.IBinder
import android.view.MotionEvent
import android.widget.LinearLayout
import android.widget.SeekBar
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.nothingmusic.databinding.ActivityPlayerBinding
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class PlayerActivity : AppCompatActivity(), ServiceConnection, MediaPlayer.OnCompletionListener{


    companion object{
        lateinit var musicListPlayerActivity: ArrayList<Music>
        var songPosition : Int = 0
        var isPlaying : Boolean = false

        var musicService:MusicService? = null

        @SuppressLint("StaticFieldLeak")
        lateinit var binding: ActivityPlayerBinding

        var repeat : Boolean = false

        var min15 : Boolean = false
        var min30 : Boolean = false
        var min45 : Boolean = false
        var min60 : Boolean = false

    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPlayerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val intent =  Intent(this, MusicService::class.java)
        bindService(intent,this, BIND_AUTO_CREATE)

        initializeLayout()

        binding.playPauseButton.setOnClickListener{
            if(isPlaying) pauseMusic()
            else playMusic()
        }

        binding.seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener{
            override fun onProgressChanged(seekbar: SeekBar?, progress: Int, fromUser: Boolean) {
                if(fromUser) musicService!!.mediaPlayer!!.seekTo(progress)
            }

            override fun onStartTrackingTouch(p0: SeekBar?) = Unit

            override fun onStopTrackingTouch(p0: SeekBar?) = Unit
        })

        binding.prevButton.setOnTouchListener { view, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    view.scaleX = 1.4f
                    view.scaleY = 1.4f
                }
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    view.scaleX = 1.5f
                    view.scaleY = 1.5f

                    if(songPosition == 0) songPosition = musicListPlayerActivity.size-1
                    else --songPosition
                    setLayout()
                    createMediaPlayer()
                }
            }
            false // Return false to allow click events to propagate
        }

        binding.nextButton.setOnTouchListener { view, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    view.scaleX = 1.4f
                    view.scaleY = 1.4f
                }
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    view.scaleX = 1.5f
                    view.scaleY = 1.5f

                    if(songPosition == musicListPlayerActivity.size-1) songPosition = 0
                    else ++songPosition
                    setLayout()
                    createMediaPlayer()
                }
            }
            false
        }

        binding.loopButton.setOnClickListener {
            if (!repeat) {
                repeat = true
                binding.loopButton.setImageResource(R.drawable.repeat_once)
            }
            else{
                repeat = false
                binding.loopButton.setImageResource(R.drawable.loop)
            }
        }

        binding.backButton.setOnClickListener { finish() }

        binding.equilizerButton.setOnClickListener {
            val eqIntent = Intent(AudioEffect.ACTION_DISPLAY_AUDIO_EFFECT_CONTROL_PANEL)
            eqIntent.putExtra(AudioEffect.EXTRA_AUDIO_SESSION, musicService!!.mediaPlayer!!.audioSessionId)
            eqIntent.putExtra(AudioEffect.EXTRA_PACKAGE_NAME,baseContext.packageName)
            eqIntent.putExtra(AudioEffect.EXTRA_CONTENT_TYPE, AudioEffect.CONTENT_TYPE_MUSIC)
            try{
                startActivityForResult(eqIntent,13)
            }
            catch (e:Exception){ Toast.makeText(this,"Equalizer Feature not supported",Toast.LENGTH_SHORT).show()}

        }

        binding.timerButton.setOnClickListener {
            if(min15 || min30 || min45 || min60){
                val builder = MaterialAlertDialogBuilder(this)
                builder.setTitle("Stop Timer")
                    .setMessage("Do you want to stop Running Timer?")
                    .setPositiveButton("Yes"){_, _->
                        min15 = false
                        min30 = false
                        min45 = false
                        min60 = false
                    }
                    .setNegativeButton("No"){dialog,_ ->
                        dialog.dismiss()
                    }
                val customDialog = builder.create()
                customDialog.show()
                customDialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(Color.WHITE)
                customDialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(Color.WHITE)

                binding.timerButton.setImageResource(R.drawable.timer)
            }
            else  {  showTimerDialog() }
        }

        binding.shareButton.setOnClickListener {
            val shareIntent = Intent()
            shareIntent.action = Intent.ACTION_SEND
            shareIntent.type = "audio/*"
            shareIntent.putExtra(Intent.EXTRA_STREAM, Uri.parse(musicListPlayerActivity[songPosition].path))
            startActivity(Intent.createChooser(shareIntent, "Sharing file via : "))
        }
    }

    private fun setLayout(){
        Glide.with(this)
            .load(musicListPlayerActivity[songPosition].artUri)
            .apply(RequestOptions().placeholder(R.drawable.image_content).centerCrop())
            .into(binding.shapeableImageView)

        binding.musicTitle.text = musicListPlayerActivity[songPosition].title
        binding.musicDecription.text = musicListPlayerActivity[songPosition].decription

        if(min15 || min30 || min45 || min60){
            binding.timerButton.setImageResource(R.drawable.timer_on)
        }

        if(repeat) binding.loopButton.setImageResource(R.drawable.repeat_once)
    }

    private fun createMediaPlayer(){
        if (musicService!!.mediaPlayer != null) {
            musicService!!.mediaPlayer!!.stop()
            musicService!!.mediaPlayer!!.release()
            musicService!!.mediaPlayer = null
        }
        try {
            musicService!!.mediaPlayer = MediaPlayer()
            musicService!!.mediaPlayer!!.setDataSource(musicListPlayerActivity[songPosition].path)
            musicService!!.mediaPlayer!!.prepare()
            musicService!!.mediaPlayer!!.start()

            isPlaying = true
            binding.playPauseButton.setImageResource(R.drawable.pause)
            musicService!!.showNotification(R.drawable.pause_icon)

            binding.musicStartTime.text = formatDuration(musicService!!.mediaPlayer!!.currentPosition.toLong())
            binding.musicEndTime.text = formatDuration(musicService!!.mediaPlayer!!.duration.toLong())
            binding.seekBar.progress = 0
            binding.seekBar.max = musicService!!.mediaPlayer!!.duration
            musicService!!.mediaPlayer!!.setOnCompletionListener(this)
        }
        catch (e: java.lang.Exception) {return}
    }

    private fun initializeLayout(){
        songPosition = intent.getIntExtra("index", 0)
        when(intent.getStringExtra("class")){
            "MusicAdapterSearch" ->{
                musicListPlayerActivity = ArrayList()
                musicListPlayerActivity.addAll(MainActivity.MusicListSearch)
                setLayout()
            }
            "MusicAdapter" -> {
                musicListPlayerActivity = ArrayList()
                musicListPlayerActivity.addAll(MainActivity.MusicListMA)
                setLayout()
            }
            "MainActivity" ->{
                musicListPlayerActivity = ArrayList()
                musicListPlayerActivity.addAll(MainActivity.MusicListMA)
                musicListPlayerActivity.shuffle()
                setLayout()
            }
        }
    }

    private fun pauseMusic(){
        isPlaying = false
        musicService!!.mediaPlayer!!.pause()
        musicService!!.showNotification(R.drawable.play_icon)
        binding.playPauseButton.setImageResource((R.drawable.play))
    }

    private fun playMusic(){
        isPlaying = true
        musicService!!.mediaPlayer!!.start()
        musicService!!.showNotification(R.drawable.pause_icon)
        binding.playPauseButton.setImageResource(R.drawable.pause)
    }

    override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
        val binder = service as MusicService.MyBinder
        musicService = binder.currentService()
        createMediaPlayer()
        musicService!!.showNotification(R.drawable.pause_icon)
        musicService!!.seekbarSetup()
    }

    override fun onServiceDisconnected(name: ComponentName?) {
        musicService = null
    }

    override fun onCompletion(p0: MediaPlayer?) {
        if(!repeat) {
            if (songPosition == musicListPlayerActivity.size - 1) songPosition = 0
            else ++songPosition
        }
        try{setLayout()} catch (e:java.lang.Exception){return}
        createMediaPlayer()
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode == 13 || resultCode == RESULT_OK)
            return
    }

    private fun showTimerDialog(){
        val dialog = BottomSheetDialog(this@PlayerActivity)
        dialog.setContentView(R.layout.bottom_timer_layout)
        dialog.show()

        dialog.findViewById<LinearLayout>(R.id.min_15)?.setOnClickListener {
            Toast.makeText(baseContext,"Music will stop after 15 minutes", Toast.LENGTH_SHORT).show()
            binding.timerButton.setImageResource(R.drawable.timer_on)
            min15 = true
            Thread{
                Thread.sleep(15*60000)
                if(min15) exitApp()
            }.start()
            dialog.dismiss()
        }

        dialog.findViewById<LinearLayout>(R.id.min_30)?.setOnClickListener {
            Toast.makeText(baseContext,"Music will stop after 30 minutes", Toast.LENGTH_SHORT).show()
            binding.timerButton.setImageResource(R.drawable.timer_on)
            min15 = true
            Thread{
                Thread.sleep(30*60000)
                if(min15) exitApp()
            }.start()
            dialog.dismiss()
        }

        dialog.findViewById<LinearLayout>(R.id.min_45)?.setOnClickListener {
            Toast.makeText(baseContext,"Music will stop after 45 minutes", Toast.LENGTH_SHORT).show()
            binding.timerButton.setImageResource(R.drawable.timer_on)
            min15 = true
            Thread{
                Thread.sleep(45*60000)
                if(min15) exitApp()
            }.start()
            dialog.dismiss()
        }

        dialog.findViewById<LinearLayout>(R.id.min_60)?.setOnClickListener {
            Toast.makeText(baseContext,"Music will stop after 60 minutes", Toast.LENGTH_SHORT).show()
            binding.timerButton.setImageResource(R.drawable.timer_on)
            min15 = true
            Thread{
                Thread.sleep(60*60000)
                if(min15) exitApp()
            }.start()
            dialog.dismiss()
        }

    }
}