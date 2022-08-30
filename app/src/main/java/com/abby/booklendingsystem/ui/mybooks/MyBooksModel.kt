package com.abby.booklendingsystem.ui.mybooks

import android.graphics.Bitmap
import android.net.Uri
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.abby.booklendingsystem.data.datasource.*
import com.abby.booklendingsystem.enums.NetworkResult
import com.abby.booklendingsystem.model.BookModel
import com.abby.booklendingsystem.model.CategoryModel
import com.example.digitalbooks.model.ImageListModel
import com.google.firebase.storage.StorageReference
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MyBooksModel @Inject constructor(
    private val firebaseFirestoreDataSource: FirebaseFirestoreDataSource,
    private val firebaseAuthDataSource: FirebaseAuthDataSource,
    private val firebaseUploader: FirebaseUploader,
    private val firebaseFireStoreApiSources: FirebaseFireStoreApiSources
) : ViewModel() {


    val newBooks = MutableLiveData<NetworkResult<List<BookModel>>>()




    suspend fun getNewBooks()= viewModelScope.launch{
        firebaseFirestoreDataSource.getNewBooks(newBooks)
    }

    suspend fun getNewBooks(name:String) = viewModelScope.launch{
        firebaseFirestoreDataSource.getNewBooks(name,newBooks)
    }

    suspend fun getNewBooksByCategory(category:String) = viewModelScope.launch {
        firebaseFirestoreDataSource.getNewBooksByCategory(category,newBooks)
    }



}