package com.abby.booklendingsystem.model

import android.net.Uri

class UsersModel(
    val email: String? = null,
    val fullname: String? = null,
    val number: String? = null,
    val role: String? = null,
    val address:String?=null,
    val password:String?=null,
    var userId:String?=null,
    var profile_image: String? = null,
) {
    var about:String ?=null

}