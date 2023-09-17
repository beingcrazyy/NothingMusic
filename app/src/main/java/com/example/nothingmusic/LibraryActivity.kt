package com.example.nothingmusic

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.nothingmusic.databinding.ActivityPlaylistBinding

class LibraryActivity : AppCompatActivity() {

    private lateinit var binding : ActivityPlaylistBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPlaylistBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.backButton.setOnClickListener { finish() }
    }
}