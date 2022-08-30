package com.abby.booklendingsystem.adaper

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import com.abby.booklendingsystem.databinding.HomeSliderRowBinding
import com.bumptech.glide.Glide
import com.example.digitalbooks.model.ImageListModel
import com.smarteist.autoimageslider.SliderViewAdapter

class ImageSliderAdapter (val context: Context, var itemList: List<ImageListModel>)
    : SliderViewAdapter<ImageSliderAdapter.SliderAdapterVH>() {

    override fun onCreateViewHolder(parent: ViewGroup): SliderAdapterVH {
        val inflate = LayoutInflater.from(parent.context)
        val binding = HomeSliderRowBinding.inflate(inflate,parent,false)
        return SliderAdapterVH(binding)
    }

    override fun onBindViewHolder(viewHolder: SliderAdapterVH, position: Int) {
        val sliderItem = itemList[position]
        viewHolder.image.scaleType = ImageView.ScaleType.CENTER_CROP
        Glide.with(context).load(sliderItem.image_url).into(viewHolder.image)
    }

    override fun getCount(): Int {
        return itemList.size
    }

    inner class SliderAdapterVH(binding: HomeSliderRowBinding) : ViewHolder(binding.root) {
        var image: ImageView = binding.homeSliderDesignImage

    }

}