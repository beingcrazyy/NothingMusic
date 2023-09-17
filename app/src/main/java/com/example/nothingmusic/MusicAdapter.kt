package com.example.nothingmusic

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.nothingmusic.databinding.MusicViewBinding

class MusicAdapter(private val context : Context, private var musicList : ArrayList<Music>) : RecyclerView.Adapter<MusicAdapter.NothingMusicHolder>(){
    class NothingMusicHolder(binding : MusicViewBinding) : RecyclerView.ViewHolder(binding.root) {
        val title = binding.songNameMusicView
        val details = binding.songDetailsMusicView
        val image = binding.imageMusicView
        val duration = binding.songDurationMusicView
        val root = binding.root
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NothingMusicHolder {
        return NothingMusicHolder(MusicViewBinding.inflate(LayoutInflater.from(context), parent, false))
    }

    @SuppressLint("CheckResult")
    override fun onBindViewHolder(holder: NothingMusicHolder, position: Int) {
        holder.title.text = musicList[position].title
        holder.details.text = musicList[position].album
        holder.duration.text = formatDuration(musicList[position].duration)
        Glide.with(context)
            .load(musicList[position].artUri)
            .apply(RequestOptions().placeholder(R.drawable.main_logo).centerCrop())
            .into(holder.image)

        holder.root.setOnClickListener{
            when{
                MainActivity.search -> sendIntent(ref = "MusicAdapterSearch", position = position)
                else -> sendIntent(ref = "MusicAdapter", position = position)
            }
        }
    }

    override fun getItemCount(): Int {
        return musicList.size
    }

    @SuppressLint("NotifyDataSetChanged")
    fun updateMusicList(searchList : ArrayList<Music>){
        musicList = ArrayList()
        musicList.addAll(searchList)
        notifyDataSetChanged()
    }

    private fun sendIntent(ref : String, position : Int, ){
        val intent = Intent(context,PlayerActivity::class.java)
        intent.putExtra("index", position)
        intent.putExtra("class", ref)
        ContextCompat.startActivity(context,intent,null)
    }
}