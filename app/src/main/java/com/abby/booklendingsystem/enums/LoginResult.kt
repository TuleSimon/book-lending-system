package com.abby.booklendingsystem.enums

sealed class LoginResult<T>(val data:T?=null,val message:String?=null){
    class Success<T>(data:T): LoginResult<T>(data)
    class Error<T>(message: String):LoginResult<T>(message = message)
    class Loading<T>(message: String?=null) :LoginResult<T>(message=message)
}
