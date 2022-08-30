package com.abby.booklendingsystem.ui.purchases

import android.graphics.Bitmap
import android.net.Uri
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.abby.booklendingsystem.data.datasource.*
import com.abby.booklendingsystem.enums.NetworkResult
import com.abby.booklendingsystem.model.*
import com.example.digitalbooks.model.ImageListModel
import com.google.firebase.storage.StorageReference
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PurchasedBooksViewModel @Inject constructor(
    private val firebaseFirestoreDataSource: FirebaseFirestoreDataSource,
    private val firebaseAuthDataSource: FirebaseAuthDataSource,
    private val firebasePurchasesDataSource: FirebasePurchasesDataSource,
    private val firebaseFireStoreApiSources: FirebaseFireStoreApiSources
) : ViewModel() {


    val purchasedBooksResponse = MutableLiveData<NetworkResult<List<BookModel>>>()



    suspend fun getPurchasedBooks() = viewModelScope.launch {
        firebasePurchasesDataSource.getPurchasedBooks(purchasedBooksResponse)
    }


    suspend fun getPurchasedBooks(userid:String) = viewModelScope.launch {
        firebasePurchasesDataSource.getPurchasedBooks(purchasedBooksResponse,userid)
    }

    suspend fun getMySales(userid:String) = viewModelScope.launch {
        firebasePurchasesDataSource.getMySales(purchasedBooksResponse,userid)
    }

    suspend fun getUserInfo(id:String):UsersModel?{
        return firebaseAuthDataSource.getUserInfo(id)
    }

    fun updateBook(bookModel: BookModel)  = viewModelScope.launch{
        firebasePurchasesDataSource.updateBook(bookModel)
    }


    suspend fun addReview(bookModel: BookModel,commentModel: ReviewCommentModel):Boolean{
        return firebasePurchasesDataSource.reviewBook(bookModel,commentModel)
    }

}