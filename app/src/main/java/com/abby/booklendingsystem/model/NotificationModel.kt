package com.abby.booklendingsystem.model

class NotificationModel() {
    constructor(read:Boolean,title:String,message:String):this(){
        this.read = read
        this.title = title
        this.message = message
    }
    var read:Boolean?=null
    var title:String?=null
    var message:String?=null
    var id:String?=null

}