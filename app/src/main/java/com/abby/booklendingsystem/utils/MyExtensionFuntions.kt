package com.abby.booklendingsystem.utils

import android.net.Uri
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.navigation.fragment.findNavController
import com.abby.booklendingsystem.R
import com.bumptech.glide.Glide
import com.google.android.material.appbar.MaterialToolbar

import org.joda.time.DateTime
import org.joda.time.DateTimeZone
import org.joda.time.format.DateTimeFormat
import org.joda.time.format.DateTimeFormatter
import org.ocpsoft.prettytime.PrettyTime
import java.util.*





    fun View.hide(){
        if(this.visibility==View.VISIBLE)
        this.visibility = View.GONE
    }
    fun View.show(){
        if(this.visibility==View.INVISIBLE || this.visibility==View.GONE)
        this.visibility = View.VISIBLE
    }

fun View.hide2(){
    if(this.visibility==View.VISIBLE)
        this.visibility = View.GONE
}
fun View.hide3(){
    if(this.visibility==View.VISIBLE)
        this.visibility = View.INVISIBLE
}
fun View.show2(){
    if(this.visibility==View.INVISIBLE || this.visibility==View.GONE)
        this.visibility = View.VISIBLE
}


fun ImageView.load(url:String){
        Glide.with(this.context).load(url).
        into(this)
}

fun ImageView.load(url: Uri){
    Glide.with(this.context).load(url).error(R.drawable.ic_baseline_image_24).
    into(this)
}



fun String.toFormattedDate():String{
        val formatter: DateTimeFormatter = DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ss'+'ssss")
            .withZone(DateTimeZone.UTC)
        val dt: DateTime = formatter.parseDateTime(this).toDateTime(DateTimeZone.getDefault())
    val prettyTime = PrettyTime(Locale.getDefault())
        return prettyTime.format(dt.toDate())
}


fun String.toFormattedDateUTC():String{
    val formatter: DateTimeFormatter = DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ss'")
        .withZone(DateTimeZone.UTC)
    val dt: DateTime = formatter.parseDateTime(this).toDateTime(DateTimeZone.getDefault())
    val prettyTime = PrettyTime(Locale.getDefault())
    return prettyTime.format(dt.toDate())
}






    fun TextView.setIfNotNull(value:String?){
        value?.apply {
            this@setIfNotNull.text = this
        }
    }

fun TextView.setIfNotNull(value:Int?){
    value?.apply {
        this@setIfNotNull.text = this.toString()
    }
}

fun TextView.setIfNotNull(value:String?, prefix:String,suffix:String?=null){
    value?.apply {
        this@setIfNotNull.text = "$prefix: $this"
        suffix?.apply {
            this@setIfNotNull.text = "$prefix: $this $suffix"
        }
    }
}



fun TextView.setIfNotNull(value:Int?, prefix:String,suffix:String?=null){
    value?.apply {
        this@setIfNotNull.text = "$prefix: $this"
        suffix?.apply {
            this@setIfNotNull.text = "$prefix: $this $suffix"
        }
    }

}


fun MaterialToolbar.registerBackAction(fragment:Fragment){
    this.setNavigationOnClickListener {
        fragment.findNavController().navigateUp()
    }

}



