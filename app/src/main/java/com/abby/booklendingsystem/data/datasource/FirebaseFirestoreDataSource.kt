package com.abby.booklendingsystem.data.datasource

import androidx.lifecycle.MutableLiveData
import com.abby.booklendingsystem.enums.NetworkResult
import com.abby.booklendingsystem.model.BookModel
import com.abby.booklendingsystem.model.CategoryModel
import com.abby.booklendingsystem.model.NotificationModel
import com.example.digitalbooks.model.ImageListModel
import com.google.firebase.firestore.Query
import kotlinx.coroutines.CompletableDeferred
import javax.inject.Inject

class FirebaseFirestoreDataSource @Inject constructor(

) {

    fun getSliderImages( value:MutableLiveData<NetworkResult<List<ImageListModel>>>) {
        value.value = NetworkResult.Loading()
        val list = mutableListOf<ImageListModel>()
        FirebaseCollections.getSliderImagesCollections().get()
            .addOnCompleteListener {
                if (it.isSuccessful && !it.result.isEmpty) {
                    val documents = it.result.documents
                    for (data in documents) {
                        val item = data.toObject(ImageListModel::class.java)
                        item?.apply {
                            list.add(item)
                        }
                    }
                    value.value = NetworkResult.Success(list)
                }
                else{
                    value.value = NetworkResult.Error(it.exception?.message?:"An error occurred")
                }
            }
    }

    fun getNewBooks( value:MutableLiveData<NetworkResult<List<BookModel>>>) {
        val ref = FirebaseCollections.getNewBooksCollection()
        setBookValue(ref,value)

    }


    fun getNewBooks(id:String, value:MutableLiveData<NetworkResult<List<BookModel>>>) {
        val ref = FirebaseCollections.getNewBooksCollection().whereEqualTo("authorID",id.trim())
        setBookValue(ref,value)
    }


    fun getNewBooksByCategory(category:String, value:MutableLiveData<NetworkResult<List<BookModel>>>) {
        val ref = FirebaseCollections.getNewBooksCollection().whereEqualTo("category",category.toUpperCase().trim())
        setBookValue(ref,value)
    }


    fun getCategories( value:MutableLiveData<NetworkResult<List<CategoryModel>>>) {
        value.value = NetworkResult.Loading()
        val list = mutableListOf<CategoryModel>()
        FirebaseCollections.CategoriesCollection().get()
            .addOnCompleteListener {
                if (it.isSuccessful && !it.result.isEmpty) {
                    val documents = it.result.documents
                    for (data in documents) {
                        val item = data.toObject(CategoryModel::class.java)
                        item?.apply {
                            categoryID = data.id
                            list.add(item)
                        }
                    }
                    value.value = NetworkResult.Success(list)
                }
                else if(it.isSuccessful && it.result.isEmpty){
                    value.value = NetworkResult.Success(mutableListOf())
                }
                else{
                    value.value = NetworkResult.Error(it.exception?.message?:"An error occurred")
                }
            }
    }









    fun getUsedBooks( value:MutableLiveData<NetworkResult<List<BookModel>>>) {
        val ref = FirebaseCollections.getUsedBooksCollection()
        setBookValue(ref,value)
    }


    fun getUsedBooks(id:String, value:MutableLiveData<NetworkResult<List<BookModel>>>) {

        val ref = FirebaseCollections.getUsedBooksCollection().whereEqualTo("authorID",id.trim())
        setBookValue(ref,value)
    }


    fun getUsedBooksByCategory(category:String, value:MutableLiveData<NetworkResult<List<BookModel>>>) {
        val ref = FirebaseCollections.getUsedBooksCollection().whereEqualTo("category",category.toUpperCase().trim())
        setBookValue(ref,value)
    }


    private fun setBookValue(query: Query, value:MutableLiveData<NetworkResult<List<BookModel>>>){
        value.value = NetworkResult.Loading()
        val list = mutableListOf<BookModel>()
        query.get()
            .addOnCompleteListener {
                if (it.isSuccessful && !it.result.isEmpty) {
                    val documents = it.result.documents
                    for (data in documents) {
                        val item = data.toObject(BookModel::class.java)
                        item?.apply {
                            list.add(item)
                        }
                    }
                    value.value = NetworkResult.Success(list)
                }
                else if(it.isSuccessful && it.result.isEmpty){
                    value.value = NetworkResult.Success(mutableListOf())
                }
                else{
                    value.value = NetworkResult.Error(it.exception?.message?:"An error occurred")
                }
            }
    }




    fun getRecommendedBooks( value:MutableLiveData<NetworkResult<List<BookModel>>>) {
        val ref = FirebaseCollections.getNewBooksCollection().whereEqualTo("recommend",true)
        setBookValue(ref,value)
    }



    fun getNotifications( value:MutableLiveData<NetworkResult<List<NotificationModel>>>) {
        val ref = FirebaseCollections.userNotificationCollection()
        setNotificationValue(ref,value)
    }

    suspend fun updateToRead( notificationModel: NotificationModel):Boolean {
        val result = CompletableDeferred<Boolean>()
        val ref = FirebaseCollections.userNotificationCollection().document(notificationModel.id.toString())
        ref.update("read",true).addOnCompleteListener {
            result.complete(it.isSuccessful)
        }
        return result.await()
    }


    private fun setNotificationValue(query: Query, value:MutableLiveData<NetworkResult<List<NotificationModel>>>){
        value.value = NetworkResult.Loading()
        val list = mutableListOf<NotificationModel>()
        query.get()
            .addOnCompleteListener {
                if (it.isSuccessful && !it.result.isEmpty) {
                    val documents = it.result.documents
                    for (data in documents) {
                        val item = data.toObject(NotificationModel::class.java)
                        item?.apply {
                            item.id = data.id
                            list.add(item)
                        }
                    }
                    value.value = NetworkResult.Success(list)
                }
                else if(it.isSuccessful && it.result.isEmpty){
                    value.value = NetworkResult.Success(mutableListOf())
                }
                else{
                    value.value = NetworkResult.Error(it.exception?.message?:"An error occurred")
                }
            }
    }

}