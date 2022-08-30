package com.abby.booklendingsystem.data.datasource

import androidx.lifecycle.MutableLiveData
import com.abby.booklendingsystem.enums.NetworkResult
import com.abby.booklendingsystem.model.BookModel
import com.abby.booklendingsystem.ui.mybooks.NEW_BOOK
import java.util.*
import javax.inject.Inject

class FirebaseFireStoreApiSources @Inject constructor(
) {

    suspend fun saveNewBook(book:BookModel,listener:MutableLiveData<NetworkResult<Boolean>>){
        listener.value = NetworkResult.Loading()
        val ref = if(book.type== NEW_BOOK)FirebaseCollections.getNewBooksCollection().document()
        else FirebaseCollections.getUsedBooksCollection().document()

            ref.set(book).addOnCompleteListener {
            if(it.isSuccessful){
                ref.update("uid",ref.id).addOnCompleteListener {
                    if(it.isSuccessful){
                        listener.value = NetworkResult.Success(true)
                    }
                    else{
                        listener.value = NetworkResult.Error(it.exception?.message?:"An error occured")
                        ref.delete()
                    }
                }
            }
            else{
                listener.value = NetworkResult.Error(it.exception?.message?:"An error occured")
            }
        }
    }

    suspend fun updateBook(book:BookModel,listener:MutableLiveData<NetworkResult<Boolean>>){
        listener.value = NetworkResult.Loading()

        book.uid?.apply {
            val ref = if(book.type== NEW_BOOK)FirebaseCollections.getNewBooksCollection().document(this)
            else FirebaseCollections.getUsedBooksCollection().document(this)


        ref.set(book).addOnCompleteListener {
            if(it.isSuccessful){
                listener.value = NetworkResult.Success(true)
            }
            else{
                listener.value = NetworkResult.Error(it.exception?.message?:"An error occured")
            }
        }
        }?: run { listener.value = NetworkResult.Error("Book id not found") }
    }

    suspend fun deleteBook(book:BookModel,listener:MutableLiveData<NetworkResult<Boolean>>){
        listener.value = NetworkResult.Loading()
        book.uid?.apply {

            val ref = if(book.type== NEW_BOOK)FirebaseCollections.getNewBooksCollection().document(this)
            else FirebaseCollections.getUsedBooksCollection().document(this)


            ref.delete().addOnCompleteListener {
                if(it.isSuccessful){
                    listener.value = NetworkResult.Success(true)
                }
                else{
                    listener.value = NetworkResult.Error(it.exception?.message?:"An error occured")
                }
            }
        }?: run { listener.value = NetworkResult.Error("Book id not found") }
    }


}