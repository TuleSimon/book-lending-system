package com.abby.booklendingsystem.data.datasource

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference

object FirebaseCollections {


    fun getUsersCollections():CollectionReference{
        val db =   Firebase.firestore
        return db.collection("userss")
    }

    fun getSliderImagesCollections():CollectionReference{
        val db = Firebase.firestore
        return db.collection("home_slider_images")
    }

    fun getNewBooksCollection():CollectionReference{
        val db = Firebase.firestore
        return db.collection("new_books")
    }

    fun BookImageStorage():StorageReference{
        return FirebaseStorage.getInstance().reference.child("books_image")
    }

    fun CategoriesCollection():CollectionReference{
        val db = Firebase.firestore
        return db.collection("categories")
    }

    fun getUsedBooksCollection(): CollectionReference {
        val db = Firebase.firestore
        return db.collection("used_books")
    }


    fun getUserCart(): CollectionReference {
        return getUsersCollections().document(FirebaseAuth.getInstance().currentUser!!.uid)
            .collection("cart")
    }


    fun getWishList(): CollectionReference {
        return getUsersCollections().document(FirebaseAuth.getInstance().currentUser!!.uid)
            .collection("wishlist")
    }

    fun getBookPurchasesCollection(): CollectionReference {
        val db = Firebase.firestore
        return db.collection("book_purchases")
    }



    fun getUsersCardsCollections():CollectionReference{
        return getUsersCollections().document(FirebaseAuth.getInstance().currentUser!!.uid).
        collection("cards")
    }

    fun userNotificationCollection():CollectionReference{
        return getUsedBooksCollection().document(FirebaseAuth.getInstance().currentUser!!.uid)
            .collection("notifications")
    }
}