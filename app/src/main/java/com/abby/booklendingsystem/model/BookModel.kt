package com.abby.booklendingsystem.model

import android.net.Uri
import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.util.*

@Parcelize
data class BookModel(
    var name:String ?=null, var author:String?=null, var authorID:String?=null, val description:String?=null,
    var price:String?=null,
    var rating:Float?=null, var category:String?=null, var recommend:Boolean?=null,
    var available:Boolean?=null,var date:Date?=null,
    var type:String?=null,var thumbb:String?=null,
    var uid:String?=null,
    var isPurchased:Boolean ?= false,
    var isPickedUp:Boolean ?= false,
    var isReturned:Boolean ?= false,
    var datePurchased:Date?=null,
    var purchasedUser:String?=null,
    var purchasedMethod:String ?=null,
    var deliveryMethod:String?=null
    ):Parcelable {


    constructor():this(null)
}