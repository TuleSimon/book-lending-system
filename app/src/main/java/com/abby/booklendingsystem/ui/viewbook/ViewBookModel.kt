package com.abby.booklendingsystem.ui.viewbook

import android.graphics.Bitmap
import android.net.Uri
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.abby.booklendingsystem.data.datasource.*
import com.abby.booklendingsystem.enums.NetworkResult
import com.abby.booklendingsystem.model.BookModel
import com.abby.booklendingsystem.model.CardsModel
import com.abby.booklendingsystem.model.CategoryModel
import com.example.digitalbooks.model.ImageListModel
import com.google.firebase.storage.StorageReference
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ViewBookModel @Inject constructor(
    private val firebasePurchasesDataSource: FirebasePurchasesDataSource,
) : ViewModel() {


    val cartItems = MutableLiveData<NetworkResult<List<BookModel>>>()

    val wishListItems = MutableLiveData<NetworkResult<List<BookModel>>>()

    val addToCartResponse = MutableLiveData<NetworkResult<Boolean>>()




    suspend fun addToCart(bookModel: BookModel)= viewModelScope.launch{
        firebasePurchasesDataSource.addToCart(bookModel,addToCartResponse)
    }

    suspend fun addToWishlist(bookModel: BookModel):Boolean{
        return firebasePurchasesDataSource.addToWishlist(bookModel)
    }

    suspend fun getCartItems() = viewModelScope.launch {
        firebasePurchasesDataSource.getCartItems(cartItems)
    }


    suspend fun getWishListCollection() = viewModelScope.launch {
        firebasePurchasesDataSource.getWishLists(wishListItems)
    }


    suspend fun removeFromCart(bookModel: BookModel) = viewModelScope.launch {
        firebasePurchasesDataSource.removeFromCart(bookModel)
    }



    val paymentMethods = MutableLiveData<NetworkResult<List<CardsModel>>>()
    val selectedPaymentMethod = MutableLiveData<CardsModel?>()

    suspend fun getCards() = viewModelScope.launch {
        firebasePurchasesDataSource.getUsersCards(paymentMethods)
    }

    suspend fun selectCard(cardsModel: CardsModel) = viewModelScope.launch {
        selectedPaymentMethod.value = cardsModel
    }

    suspend fun saveCard(cardsModel: CardsModel):Boolean{
        return firebasePurchasesDataSource.saveUserCard(cardsModel)
    }

    fun deleteCard() =viewModelScope.launch{
        selectedPaymentMethod.value?.let { firebasePurchasesDataSource.deleteCard(it) }
        selectedPaymentMethod.value=null
    }

    val purchaseStatus = MutableLiveData<NetworkResult<Boolean>>()
    var purchaseItems = mutableListOf<BookModel>()
    suspend fun makePurchases(method:String, deliveryMethod:String):Boolean{
        val task = firebasePurchasesDataSource.checkOut(purchaseItems,method,deliveryMethod)
        return task

    }
}