package com.webasyst.x.photoFeature

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.webasyst.x.databinding.RowPhotoItemBinding
import models.Photos

class PhotoListAdapter(val installation: String) :
    ListAdapter<Photos.Photo, PhotoListAdapter.PhotoViewHolder>(Companion) {
    class PhotoViewHolder(private val binding: RowPhotoItemBinding, val installation: String) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(photo: Photos.Photo) {
            Glide
                .with(binding.root)
                .load("$installation${photo.image_url}")
                .into(binding.imageViewPhoto);
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PhotoViewHolder =
        RowPhotoItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            .let { PhotoViewHolder(it, installation = installation) }

    override fun onBindViewHolder(holder: PhotoViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    companion object : DiffUtil.ItemCallback<Photos.Photo>() {
        override fun areItemsTheSame(oldItem: Photos.Photo, newItem: Photos.Photo): Boolean =
            oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: Photos.Photo, newItem: Photos.Photo): Boolean =
            oldItem == newItem
    }
}
