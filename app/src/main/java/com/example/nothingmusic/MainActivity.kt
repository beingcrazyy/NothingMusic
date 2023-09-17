package com.example.nothingmusic

import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.text.Editable
import android.text.TextWatcher
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.nothingmusic.databinding.ActivityMainBinding
import java.io.File
import kotlin.system.exitProcess

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var toggle: ActionBarDrawerToggle
    private lateinit var musicAdapter: MusicAdapter

    companion object{
        lateinit var MusicListMA : ArrayList<Music>
        lateinit var MusicListSearch : ArrayList<Music>
        var search : Boolean = false
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initializeLayout()

        binding.navView.setNavigationItemSelectedListener{
            when(it.itemId){
                R.id.feedback -> Toast.makeText(this, "Feedback", Toast.LENGTH_SHORT).show()
                R.id.about -> Toast.makeText(this, "About", Toast.LENGTH_SHORT).show()
                R.id.Settings -> Toast.makeText(this, "Settings", Toast.LENGTH_SHORT).show()
                R.id.exit -> exitProcess(1)
            }
            true
        }

        binding.libraryButton.setOnClickListener {
            val intent = Intent(this@MainActivity, LibraryActivity::class.java)
            startActivity(intent)
        }
        binding.shuffleButton.setOnClickListener{
            val intent = Intent(this@MainActivity, PlayerActivity::class.java)
            intent.putExtra("index", 0)
            intent.putExtra("class", "MainActivity")
            startActivity(intent)
        }

        binding.searchPanal.addTextChangedListener(object : TextWatcher{
            override fun beforeTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) = Unit

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                MusicListSearch = ArrayList()
                if(s != null){
                    val userInput = s.toString().lowercase()
                    for(song in MusicListMA){
                        if(song.title.lowercase().contains(userInput)){
                            MusicListSearch.add(song)
                        }
                    }
                    search = true
                    musicAdapter.updateMusicList(searchList = MusicListSearch)
                }
            }

            override fun afterTextChanged(s: Editable?): Unit =  Unit
        })
    }
    private fun requestRuntimePermission(){
        if(ActivityCompat.checkSelfPermission(this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
            != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.WRITE_EXTERNAL_STORAGE), 13)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if(requestCode == 13){
            if(grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                Toast.makeText(this, "permission granted", Toast.LENGTH_SHORT).show()
            else
                ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.WRITE_EXTERNAL_STORAGE), 13)
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if(toggle.onOptionsItemSelected(item))
            return true
        return super.onOptionsItemSelected(item)
    }

    private fun initializeLayout(){
        search = false

        requestRuntimePermission()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val toolbar = binding.myToolbar
        setSupportActionBar(toolbar)

        toggle = ActionBarDrawerToggle(this, binding.root, R.string.open, R.string.close)
        binding.root.addDrawerListener(toggle)
        toggle.syncState()
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        supportActionBar?.setHomeAsUpIndicator(R.drawable.side_bar_icon)

        MusicListMA = getAllAudio()


        binding.musicRecyclerView.setHasFixedSize(true)
        binding.musicRecyclerView.setItemViewCacheSize(13)
        binding.musicRecyclerView.layoutManager = LinearLayoutManager(this@MainActivity)
        musicAdapter = MusicAdapter(this@MainActivity, MusicListMA)
        binding.musicRecyclerView.adapter = musicAdapter

    }

    @SuppressLint("Recycle")
    private fun getAllAudio() : ArrayList<Music>{
        val tempList = ArrayList<Music>()
        val selection  = MediaStore.Audio.Media.IS_MUSIC + "!= 0"
        val projection = arrayOf(MediaStore.Audio.Media._ID, MediaStore.Audio.Media.TITLE, MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.DATA, MediaStore.Audio.Media.DURATION, MediaStore.Audio.Media.ALBUM, MediaStore.Audio.Media.DATE_ADDED,
        MediaStore.Audio.Media.ALBUM_ID)

        val cursor = this.contentResolver.query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, projection, selection, null,
            MediaStore.Audio.Media.DATE_ADDED + " DESC" , null)

        if (cursor != null) {
            if (cursor.moveToFirst()) {
                val idColumnIndex = cursor.getColumnIndex(MediaStore.Audio.Media._ID)
                val titleColumnIndex = cursor.getColumnIndex(MediaStore.Audio.Media.TITLE)
                val artistColumnIndex = cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST)
                val dataColumnIndex = cursor.getColumnIndex(MediaStore.Audio.Media.DATA)
                val durationColumnIndex = cursor.getColumnIndex(MediaStore.Audio.Media.DURATION)
                val albumColumnIndex = cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM)
                val albumIdColumnIndex = cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID)

                do {
                    val cursorId = cursor.getString(idColumnIndex)
                    val cursorTitle = cursor.getString(titleColumnIndex)
                    val cursorArtist = cursor.getString(artistColumnIndex)
                    val cursorData = cursor.getString(dataColumnIndex)
                    val cursorDuration = cursor.getLong(durationColumnIndex)
                    val cursorAlbum = cursor.getString(albumColumnIndex)
                    val cursorAlbumId = cursor.getLong(albumIdColumnIndex).toString()
                    val uri = Uri.parse( "content://media/external/audio/albumart")
                    val cursorArtUri = Uri.withAppendedPath(uri, cursorAlbumId).toString()

                    val music = Music(id = cursorId, title = cursorTitle, decription = cursorArtist, path = cursorData, duration = cursorDuration, album = cursorAlbum,
                    artUri = cursorArtUri)
                    val file = File(music.path)

                    if (file.exists())
                        tempList.add(music)

                } while (cursor.moveToNext())

                cursor.close()
            }
        }
        return tempList
    }

    @Suppress("DEPRECATION")
    override fun onDestroy() {
        super.onDestroy()
        exitApp()
    }
}