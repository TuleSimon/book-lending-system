package com.abby.booklendingsystem.data.datasource

import androidx.lifecycle.MutableLiveData
import com.abby.booklendingsystem.enums.NetworkResult
import com.abby.booklendingsystem.model.*
import com.abby.booklendingsystem.ui.mybooks.NEW_BOOK
import com.example.digitalbooks.model.ImageListModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.Query
import kotlinx.coroutines.*
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.flow.MutableStateFlow
import java.util.*
import javax.inject.Inject

class FirebasePurchasesDataSource @Inject constructor(

) {

    fun addToCart(bookModel: BookModel, value: MutableLiveData<NetworkResult<Boolean>>) {
        val ref = FirebaseCollections.getUserCart()
        bookModel.uid?.let {
            ref.document(it).set(bookModel).addOnCompleteListener {
                if (it.isSuccessful) {
                    value.value = NetworkResult.Success(true)
                } else {
                    value.value = NetworkResult.Error(it.exception?.message ?: "An error occured")
                }
            }.addOnFailureListener {
                value.value = NetworkResult.Error(it.message ?: "An error occured")
            }
        }
    }

    suspend fun addToWishlist(bookModel: BookModel):Boolean {
        val ref = FirebaseCollections.getWishList()
            val task = CompletableDeferred<Boolean>()

                bookModel.uid?.let {
                    ref.document(it).set(bookModel).addOnCompleteListener {
                        if (it.isSuccessful) {
                            task.complete(true)
                            return@addOnCompleteListener
                        } else {
                            task.complete(false)
                            return@addOnCompleteListener
                        }
                    }
                }

        return task.await()
    }

    fun getCartItems(value: MutableLiveData<NetworkResult<List<BookModel>>>) {
        val ref = FirebaseCollections.getUserCart()
        setBookValue(ref, value)
    }


    fun getWishLists(value: MutableLiveData<NetworkResult<List<BookModel>>>) {
        val ref = FirebaseCollections.getWishList()
        setBookValue(ref, value)
    }


    suspend fun checkOut(list: List<BookModel>,method: String,
                         deliveryMethod:String) :Boolean{

        for (i in list.iterator()) {
            addToPurchases(i,method,deliveryMethod)
        }
        return true
    }


    suspend fun addToPurchases(bookModel: BookModel,method:String,deliveryMethod:String): Boolean {
        val result = CompletableDeferred<Boolean>()
        bookModel.isPurchased = true
        bookModel.purchasedMethod = method
        bookModel.deliveryMethod = deliveryMethod
        bookModel.isReturned=false
        bookModel.purchasedUser = FirebaseAuth.getInstance().currentUser?.uid
        bookModel.datePurchased = Calendar.getInstance().time
        val ref = FirebaseCollections.getBookPurchasesCollection()
        ref.document(bookModel.uid.toString()).set(bookModel).addOnCompleteListener {
            if (it.isSuccessful) {
                removeFromCart(bookModel)
                result.complete(true)
            } else {
                result.complete(false)
            }
        }.addOnFailureListener {
            result.complete(false)
        }
        val notification = NotificationModel(false,"${bookModel.name} purchased",
            "Hello ${FirebaseAuth.getInstance().currentUser?.displayName}, you recently purchased the book:" +
                    " ${bookModel.name}, your payment has been confirmed, your book will be delivered to you or you pick it up " +
                    "from the nearest supported book store close to you depending on your selected delivery method."
        )

        FirebaseCollections.userNotificationCollection().document().set(notification)

        CoroutineScope(IO).launch {
            timer().collect{
                if(it==20)
                    if(!result.isCompleted){
                    result.complete(false)

                    }
            }
        }

        return result.await()
    }

    suspend fun timer():MutableStateFlow<Int>{
        val time = MutableStateFlow<Int>(0)
        var int = 0
        while(int<21){
            delay(1000)
            int += 1
            time.emit(int)
        }
        return time
    }


    private fun setBookValue(query: Query, value: MutableLiveData<NetworkResult<List<BookModel>>>) {
        value.value = NetworkResult.Loading()
        val list = mutableListOf<BookModel>()
        query.get()
            .addOnCompleteListener {
                if (it.isSuccessful && !it.result.isEmpty) {
                    val documents = it.result.documents
                    for (data in documents) {
                        val item = data.toObject(BookModel::class.java)
                        item?.apply {
                            if(item.uid==null)
                                item.uid = data.id
                            list.add(item)
                        }
                    }
                    value.value = NetworkResult.Success(list)
                } else if (it.isSuccessful && it.result.isEmpty) {
                    value.value = NetworkResult.Success(mutableListOf())
                } else {
                    value.value = NetworkResult.Error(it.exception?.message ?: "An error occurred")
                }

            }
    }

    fun removeFromCart(bookModel: BookModel) {
        val ref = bookModel.uid?.let { FirebaseCollections.getUserCart().document(it) }
        ref?.delete()
    }



    fun getUsersCards(value: MutableLiveData<NetworkResult<List<CardsModel>>>){
        val ref = FirebaseCollections.getUsersCardsCollections()
        value.value = NetworkResult.Loading()
        val list = mutableListOf<CardsModel>()
        ref.get()
            .addOnCompleteListener {
                if (it.isSuccessful && !it.result.isEmpty) {
                    val documents = it.result.documents
                    for (data in documents) {
                        val item = data.toObject(CardsModel::class.java)
                        item?.apply {
                            item.id = data.id
                            list.add(item)
                        }
                    }
                    value.value = NetworkResult.Success(list)
                } else if (it.isSuccessful && it.result.isEmpty) {
                    value.value = NetworkResult.Success(mutableListOf())
                } else {
                    value.value = NetworkResult.Error(it.exception?.message ?: "An error occurred")
                }

            }
    }

    suspend fun saveUserCard(cardsModel: CardsModel): Boolean {
        val result = CompletableDeferred<Boolean>()
        val ref = FirebaseCollections.getUsersCardsCollections()
        ref.document().set(cardsModel).addOnCompleteListener {
            if(it.isSuccessful){
                result.complete(true)
            }
            else{
                result.complete(false)
            }
        }
        return result.await()
    }

    fun deleteCard(value: CardsModel) {
        val ref = FirebaseCollections.getUsersCardsCollections()
        value.id?.let { ref.document(it).delete() }
    }

    fun getPurchasedBooks(purchasedBooksResponse: MutableLiveData<NetworkResult<List<BookModel>>>,id:String) {
        val ref = FirebaseCollections.getBookPurchasesCollection().whereEqualTo("purchasedUser",id)
        setBookValue(ref, purchasedBooksResponse)
    }

    fun getMySales(purchasedBooksResponse: MutableLiveData<NetworkResult<List<BookModel>>>,id:String) {
        val ref = FirebaseCollections.getBookPurchasesCollection().whereEqualTo("authorID",id)
        setBookValue(ref, purchasedBooksResponse)
    }


    fun getPurchasedBooks(purchasedBooksResponse: MutableLiveData<NetworkResult<List<BookModel>>>) {
        val ref = FirebaseCollections.getBookPurchasesCollection()
        setBookValue(ref, purchasedBooksResponse)
    }

    fun updateBook(bookModel: BookModel) {
        FirebaseCollections.getBookPurchasesCollection().document(bookModel.uid.toString()).
        update("returned",bookModel.isReturned,"pickedUp",bookModel.isPickedUp)

        if(bookModel.isReturned==true) {
            val notification = NotificationModel(
                false, "Thank you for reviewing ${bookModel.name}",
                "Hello ${FirebaseAuth.getInstance().currentUser?.displayName}, thank you for returning " +
                        "the book: ${bookModel.name} and we hope you had the best of experiences, we hope you returned it on time" +
                        " to avoid suspension or being penalized.")
            FirebaseCollections.userNotificationCollection().document().set(notification)
        }
    }

    suspend fun reviewBook(bookModel: BookModel,reviewCommentModel: ReviewCommentModel):Boolean{
        val result = CompletableDeferred<Boolean>()
        val ref = if(bookModel.type== NEW_BOOK) FirebaseCollections.getNewBooksCollection()
        else FirebaseCollections.getUsedBooksCollection()
        ref.document(bookModel.uid.toString()).collection("reviews")
            .document(FirebaseAuth.getInstance().currentUser!!.uid).set(reviewCommentModel)
            .addOnCompleteListener {
                if(it.isSuccessful){
                    result.complete(true)
                }
                else{
                    result.complete(false)
                }
            }
        val notification = NotificationModel(false,"Thank you for reviewing: ${bookModel.name}",
            "Hello ${FirebaseAuth.getInstance().currentUser?.displayName}, we have received your review of " +
                    "the book ${bookModel.name} and we hope you had the best of experiences, rest assured our team of" +
                    " experienced developers are working tirelessly to give you a better experience"
            )
        FirebaseCollections.userNotificationCollection().document().set(notification)
        return result.await()
    }


    suspend fun getBookReviews(bookModel: BookModel,reviews: MutableLiveData<NetworkResult<List<ReviewCommentModel>>>){
        val ref = if(bookModel.type== NEW_BOOK) FirebaseCollections.getNewBooksCollection()
        else FirebaseCollections.getUsedBooksCollection()
        setReviewValue(ref.document(bookModel.uid.toString()).collection("reviews"),reviews)
    }


    private fun setReviewValue(query: Query, value: MutableLiveData<NetworkResult<List<ReviewCommentModel>>>) {
        value.value = NetworkResult.Loading()
        val list = mutableListOf<ReviewCommentModel>()
        query.get()
            .addOnCompleteListener {
                if (it.isSuccessful && !it.result.isEmpty) {
                    val documents = it.result.documents
                    for (data in documents) {
                        val item = data.toObject(ReviewCommentModel::class.java)
                        item?.apply {
                            list.add(item)
                        }
                    }
                    value.value = NetworkResult.Success(list)
                } else if (it.isSuccessful && it.result.isEmpty) {
                    value.value = NetworkResult.Success(mutableListOf())
                } else {
                    value.value = NetworkResult.Error(it.exception?.message ?: "An error occurred")
                }

            }
    }
}