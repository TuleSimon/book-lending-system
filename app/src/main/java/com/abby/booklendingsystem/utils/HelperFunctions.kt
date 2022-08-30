package com.abby.booklendingsystem.utils

import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.lifecycleScope
import com.abby.booklendingsystem.R
import com.abby.booklendingsystem.databinding.DialogPickupOptionsBinding
import com.abby.booklendingsystem.databinding.DialogReviewBookBinding
import com.abby.booklendingsystem.databinding.ProgressLayoutBinding
import com.abby.booklendingsystem.enums.LoginResult
import com.abby.booklendingsystem.model.BookModel
import com.abby.booklendingsystem.model.ReviewCommentModel
import com.abby.booklendingsystem.ui.purchases.PurchasedBooksViewModel
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import es.dmoral.toasty.Toasty
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

const val AUTHOR_BOOKS ="AUTHOR"
const val ALL_BOOKS = "ALL"


object HelperFunctions {

    fun<T> setValue(result:LoginResult<T>, data:MutableLiveData<T>){
        when(result){
            is LoginResult.Error -> { }
        }
    }


    fun BooksStorage():StorageReference{
        return FirebaseStorage.getInstance().reference.child("books_images")
    }

    fun UserStorage():StorageReference{
        return FirebaseStorage.getInstance().reference.child("users")
    }



    suspend fun showUploadDialog( title:String,fragment:Fragment,
                         progress: MutableStateFlow<Long>, totalProgress: MutableStateFlow<Long>,): AlertDialog {
        val binding = ProgressLayoutBinding.inflate(fragment.layoutInflater)
        val dialog = MaterialAlertDialogBuilder(fragment.requireContext())
        dialog.setView(binding.root)
        binding.title.text = title
        val alert = dialog.show()
        fragment.lifecycleScope.launch {
            progress.collect{
                binding.progress.progress = it.toInt()
            }
        }
        fragment.lifecycleScope.launch {
            totalProgress.collect{
                binding.progress.max = it.toInt()
            }
        }
        return alert
    }

    const val DELIVERY_PICKUP = "PICKUP"
    const val DELIVERY_HOME_DELIVERY = "HOME DELIVERY"

    suspend fun showSelectDeliveryMethodDialog(fragment: Fragment ): String {
        val binding = DialogPickupOptionsBinding.inflate(fragment.layoutInflater)
        val dialog = MaterialAlertDialogBuilder(fragment.requireContext())
        dialog.setView(binding.root)
        val alert = dialog.show()
        val delivery_method = CompletableDeferred<String>()

        binding.contiue.setOnClickListener {
            val checkId = binding.radiogroup.checkedRadioButtonId
            when(checkId){
                R.id.home_delivery -> delivery_method.complete( DELIVERY_HOME_DELIVERY)
                R.id.pickup_station -> delivery_method.complete( DELIVERY_PICKUP)
            }
            alert.dismiss()
        }

        return delivery_method.await()
    }





    fun showReviewDialog(fragment: Fragment ,viewModel: PurchasedBooksViewModel,bookModel: BookModel) {
        val binding = DialogReviewBookBinding.inflate(fragment.layoutInflater)
        val dialog = MaterialAlertDialogBuilder(fragment.requireContext())
        dialog.setView(binding.root)
        val alert = dialog.show()

        binding.review.setOnClickListener {
            val rating = binding.rating.rating
            if(rating==0f){
                Toasty.error(fragment.requireContext(),"Invalid rating",Toasty.LENGTH_LONG).show()
                return@setOnClickListener
            }
            val comment = ReviewCommentModel(rating)
            binding.commentOptional.text?.apply {
                comment.comment = this.toString()
            }

            fragment.lifecycleScope.launch {
                binding.progress.show()
                binding.review.isEnabled = false
                val review = viewModel.addReview(bookModel,comment)
                binding.progress.hide3()
                binding.review.isEnabled = true
                if(review){
                    Toasty.success(fragment.requireContext(),"Book Reviewed",Toasty.LENGTH_LONG).show()
                    alert.dismiss()
                }
                else{
                    Toasty.error(fragment.requireContext(),"Book not Reviewed",Toasty.LENGTH_LONG).show()
                }
            }

        }

    }
}