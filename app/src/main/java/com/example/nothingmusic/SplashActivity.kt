package com.example.nothingmusic

import android.annotation.SuppressLint
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler

@SuppressLint("CustomSplashScreen")
class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        val intentHome = Intent(this, MainActivity::class.java)

        val handler = Handler()
        handler.postDelayed({
            startActivity(intentHome)
            finish()
        }, 2000)


    }
}