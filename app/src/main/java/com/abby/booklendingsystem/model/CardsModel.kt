package com.abby.booklendingsystem.model

class CardsModel(
    var id: String? = null,
    var cardnumber: String? = null,
    var holder:String?=null,
    val cvv: String? = null,
    val expiry: String? = null,
    val type: String? = null,

) {
    constructor():this(null)
}