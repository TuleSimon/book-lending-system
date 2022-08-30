package com.abby.booklendingsystem.ui.home

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
class HomeViewModel @Inject constructor(
    private val firebaseFirestoreDataSource: FirebaseFirestoreDataSource,
    private val firebaseAuthDataSource: FirebaseAuthDataSource,
    private val firebaseUploader: FirebaseUploader,
    private val firebaseFireStoreApiSources: FirebaseFireStoreApiSources
) : ViewModel() {

    val sliderImages = MutableLiveData<NetworkResult<List<ImageListModel>>>()

    val newBooks = MutableLiveData<NetworkResult<List<BookModel>>>()
    val _newBooks = MutableLiveData<NetworkResult<List<BookModel>>>()

    val progress = MutableStateFlow<Long>(0)
    val totalProgress =  MutableStateFlow<Long>(10)
    val uploadUri = MutableLiveData<NetworkResult<Uri>>()
    val bookuploadUri = MutableLiveData<NetworkResult<Uri>>()
    val updatePhotoResult = MutableLiveData<NetworkResult<Boolean>>()
    val publishBOokResult = MutableLiveData<NetworkResult<Boolean>>()

    

    suspend fun getImages(){
        firebaseFirestoreDataSource.getSliderImages(sliderImages)
    }

    suspend fun getNewBooks()= viewModelScope.launch{
        firebaseFirestoreDataSource.getNewBooks(newBooks)
    }

    suspend fun getNewBooks(name:String) = viewModelScope.launch{
        firebaseFirestoreDataSource.getNewBooks(name,newBooks)
    }

    suspend fun getNewBooksByCategory(category:String) = viewModelScope.launch {
        firebaseFirestoreDataSource.getNewBooksByCategory(category,newBooks)
    }


    suspend fun uploadImage(bitmap: Bitmap,ref:StorageReference, listenToProgress:Boolean){
        if(listenToProgress)
            firebaseUploader.uploadProductImage(bitmap,ref,uploadUri,progress,totalProgress)
        else
            firebaseUploader.uploadProductImage(bitmap,ref,uploadUri)
    }

    suspend fun uploadImage2(bitmap: Bitmap,ref:StorageReference, listenToProgress:Boolean){
        if(listenToProgress)
            firebaseUploader.uploadProductImage(bitmap,ref,bookuploadUri,progress,totalProgress)
        else
            firebaseUploader.uploadProductImage(bitmap,ref,bookuploadUri)
    }


     fun updateUserPhoto(uri:Uri) = viewModelScope.launch {
        firebaseAuthDataSource.updatePhoto(uri,updatePhotoResult)
    }

    fun uploadBookImage(
       imageBitmap:Bitmap
    )= viewModelScope.launch {
                progress.value = 0
                totalProgress.value = 10
                uploadImage2(imageBitmap,FirebaseCollections.BookImageStorage(),true)
    }

    fun publishBook( book:BookModel) = viewModelScope.launch{
        firebaseFireStoreApiSources.saveNewBook(book,publishBOokResult)
    }


    fun updateBook( book:BookModel) = viewModelScope.launch{
        firebaseFireStoreApiSources.updateBook(book,publishBOokResult)
    }

    val categoriesResult = MutableLiveData<NetworkResult<List<CategoryModel>>>()
    fun getCategories() = viewModelScope.launch {
        firebaseFirestoreDataSource.getCategories(categoriesResult)
    }




    val recommendedBooksResult = MutableLiveData<NetworkResult<List<BookModel>>>()
    fun getRecommendedBooks() = viewModelScope.launch {
        firebaseFirestoreDataSource.getRecommendedBooks(recommendedBooksResult)
    }
}