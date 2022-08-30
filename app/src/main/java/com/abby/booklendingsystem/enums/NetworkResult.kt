package com.abby.booklendingsystem.enums

sealed class NetworkResult<T>(val data:T?=null, val message:String?=null){
    class Success<T>(data:T): NetworkResult<T>(data)
    class Error<T>(message: String):NetworkResult<T>(message = message)
    class Loading<T>(message: String?=null) :NetworkResult<T>(message=message)
}
