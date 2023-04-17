package com.example.clothingsuggester.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import com.example.clothingsuggester.databinding.ItemCardImageBinding
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.Adapter
import com.example.clothingsuggester.R

class ImagesViewPagerAdapter(private val images: List<Int>) :Adapter<ImagesViewPagerAdapter.ImagesViewHolder>(){
    class ImagesViewHolder(val binding:ItemCardImageBinding): RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImagesViewHolder {
        val binding= ItemCardImageBinding.inflate(LayoutInflater.from(parent.context),parent,false)
   return ImagesViewHolder(binding) }

    override fun getItemCount(): Int =images.size
    private fun bind(
        position: Int,
        holder: ImagesViewHolder
    ) {
        val currentItem = images[position]
        holder.binding.image.setImageResource(currentItem)
    }
    override fun onBindViewHolder(holder: ImagesViewHolder, position: Int) {
        bind(position, holder)
    }

}