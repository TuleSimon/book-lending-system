package com.abby.booklendingsystem.utils

import android.app.Activity
import android.app.AlertDialog
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Handler
import android.widget.ImageView
import android.widget.Toast
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import com.abby.booklendingsystem.model.UsersModel
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import dmax.dialog.SpotsDialog
import es.dmoral.toasty.Toasty


const val PAYMENTMETHOD_CARD = "CARD"
const val PAYMENTMETHOD_PAYPAL = "PAYPAL"
const val PAYMENTMETHOD_APPLEPAY = "APPLEPAY"

fun <T> LiveData<T>.observeOnce(lifecycleOwner: LifecycleOwner, observer: Observer<T>){
    observe(lifecycleOwner, object : Observer<T>{
        override fun onChanged(t: T) {
            removeObserver(this)
            observer.onChanged(t)
        }

    })
}

object Utils {

    fun getUser():UsersModel{
        val user = FirebaseAuth.getInstance().currentUser
        return UsersModel(userId = user!!.uid, email = user.email, fullname = user.displayName)
    }

    lateinit var loader: AlertDialog

    fun currentUserID(): String {
        return FirebaseAuth.getInstance().currentUser!!.uid
    }

    fun currentUser(): FirebaseUser? {
        return FirebaseAuth.getInstance().currentUser
    }

    fun database(): FirebaseFirestore {
        return FirebaseFirestore.getInstance()
    }


    fun copyValue(context: Activity, value: String) {
        val clipboard: ClipboardManager = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip: ClipData = ClipData.newPlainText("", value)
        clipboard.setPrimaryClip(clip)
        Toasty.info(context, "copied", Toast.LENGTH_LONG, true).show()
    }

    fun loadImage(context: Context, src: Any, view: ImageView) {
        try {
            Glide.with(context).load(src).into(view)
        } catch (e: IllegalArgumentException) {
        }

    }


    fun showLoader(context: Context, title: String) {
        loader = SpotsDialog.Builder()
            .setContext(context)
            .setMessage(title)
            .setCancelable(false)
            .build()
            .apply {
            }
        if (loader.isShowing) {
            try {
                loader.dismiss()
            }catch (e:Exception){}

        }else{
            try {
                loader.show()
            }catch (e:Exception){}
        }

    }

    fun dismissLoader() {
        try {
            if (loader.isShowing) {
                loader.dismiss()
            }
        }
        catch (e:Exception){
            e.printStackTrace()
        }
    }
}