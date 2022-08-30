package com.abby.booklendingsystem.ui.purchases

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewModelScope
import androidx.navigation.fragment.navArgs
import com.abby.booklendingsystem.R
import com.abby.booklendingsystem.databinding.FragmentViewBinding
import com.abby.booklendingsystem.databinding.FragmentViewPurchaseitemsBinding
import com.abby.booklendingsystem.enums.NetworkResult
import com.abby.booklendingsystem.model.BookModel
import com.abby.booklendingsystem.model.UsersModel
import com.abby.booklendingsystem.ui.books.BooksViewModel
import com.abby.booklendingsystem.ui.mybooks.OLD_BOOK
import com.abby.booklendingsystem.utils.*
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.AndroidEntryPoint
import es.dmoral.toasty.Toasty
import kotlinx.coroutines.launch
import org.joda.time.DateTime
import org.joda.time.DateTimeUtils
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit


@AndroidEntryPoint
class ViewPurchaseFragment : Fragment() {

    lateinit var binding:FragmentViewPurchaseitemsBinding
    val viewModel by activityViewModels<PurchasedBooksViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentViewPurchaseitemsBinding.inflate(inflater,container,false)
        return binding.root
    }

    val args:ViewPurchaseFragmentArgs by navArgs()
    val book by lazy { args.book }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        updateValues(book)

        binding.toolbar.registerBackAction(this)
    }




    fun convertDate(date: Date?):String?{
        return date?.let { SimpleDateFormat("MMMM d,y", Locale.ENGLISH).format(it) }
    }

    fun daysLeft(date:Date):Int{
        val days= TimeUnit.MILLISECONDS.toDays(date.time - Calendar.getInstance() .timeInMillis  ).toInt()
        return days
    }




    private fun updateValues(book:BookModel) = lifecycleScope.launch{
        binding.bookName.setIfNotNull(book.name)
        binding.bookAuthor.setIfNotNull(book.author)
        book.rating?.apply {
            binding.bookRating.rating = this
        }
        binding.toolbar.title = book.name
        binding.bookCategory.setIfNotNull(book.category)
        book.thumbb?.let { binding.addBookImage.load(it) }

        binding.bookPurchaseType.setIfNotNull(book.type)

        if(book.type== OLD_BOOK) {
            binding.latebookreturned.show()
            binding.bookreturned.show()
            binding.returnStatus.root.show()
        }
        else{
            binding.returnStatus.root.hide()
        }

        binding.bookPaymentMethod.setIfNotNull(book.purchasedMethod)
        binding.bookDateofpurchased.setIfNotNull(convertDate(book.date))
        book.date?.apply {
            val time = Calendar.getInstance()
            time.time  = this
            time.add(Calendar.DAY_OF_YEAR, 7)
            binding.bookDateofreturn.setIfNotNull(convertDate(time.time))
        }
        binding.bookPrice.text = getString(R.string.money_template,book.price)

        Utils.showLoader(requireContext(),"please wait..")
        val author = getUser(book.authorID)
        val author2 = getUser(book.purchasedUser)
        Utils.dismissLoader()

        if(FirebaseAuth.getInstance().currentUser?.uid==author?.userId){
            binding.authorLayout.show()
           binding.reviewBook.hide()
            binding.updateDetails.setOnClickListener {
                updateBook()
            }
        }

            binding.reviewBook.setOnClickListener {
                    HelperFunctions.showReviewDialog(this@ViewPurchaseFragment, viewModel, book)

            }



        binding.paymentStatus.apply {
            done.show()
            notdone.hide3()
            body.text = "Payment of ${book.price} pounds completed successfully, using " +
                    "${book.purchasedMethod?.lowercase()} as payment method"
        }

        if(book.isReturned==true){
            binding.returnStatus.apply {
                done.show()
                notdone.hide3()
                header.text = "Book Returned"
                body.text = "Book ${book.name} has been returned to the seller bookshop address via the POST OFFICE."
            }
        }
        else{
            binding.returnStatus.apply {
                notdone.show()
                done.hide3()
                header.text = "Book Not Returned Yet"
                body.text = "Book ${book.name} has not been returned to the POST OFFICE & seller bookshop address, " +
                        "please return it within the return time frame."
            }
        }

        if(book.isPickedUp == true){
            binding.deliveryStatus.done.show()
            binding.deliveryStatus.notdone.hide3()
            when(book.deliveryMethod){
                HelperFunctions.DELIVERY_PICKUP -> {
                    binding.deliveryStatus.header.text = binding.root.context.getString(R.string.book_del_picked_up)
                    binding.pickedup.text = "Picked Up"
                    viewModel.viewModelScope.launch {

                        binding.deliveryStatus.body.text = "Book picked up from ${author?.address}"
                    }

                }
                else -> {
                    binding.deliveryStatus.header.text = binding.root.context.getString(R.string.book_del_delivered)
                    viewModel.viewModelScope.launch {
                        binding.pickedup.text = "Delivered to reader"
                        binding.deliveryStatus.body.text = "Book was delivered to your address: ${author2?.address}" +
                                " via the nearest POST OFFICE"
                    }
                }
            }
        }
        else{
            binding.deliveryStatus.done.hide3()
            binding.deliveryStatus.notdone.show()
            when(book.deliveryMethod){
                HelperFunctions.DELIVERY_PICKUP -> {
                    binding.deliveryStatus.header.text = binding.root.context.getString(R.string.book_del_awaitingpick)
                    binding.pickedup.text = "Picked Up"
                    viewModel.viewModelScope.launch {
                        binding.deliveryStatus.body.text = "Waiting for you to picked up the book from author" +
                                " book store at ${author?.address}."
                    }
                }
                else -> {
                    binding.deliveryStatus.header.text = binding.root.context.getString(R.string.book_del_prog)
                    binding.pickedup.text = "Delivered to reader"
                    viewModel.viewModelScope.launch {
                        binding.deliveryStatus.body.text = "Book is being delivered to your address: ${author2?.address}, " +
                                "via the nearest POST OFFICE, this might take 1-4 days."
                    }
                }
            }
        }
    }


    suspend fun getUser(id:String?):UsersModel?{
        if(id!=null)
            return viewModel.getUserInfo(id)
        return null
    }

    private fun updateBook(){
        val isReturned = binding.bookreturned.isChecked
        val ispickedup = binding.pickedup.isChecked
        book.isReturned = isReturned
        book.isPickedUp = ispickedup
        viewModel.updateBook(book)
        updateValues(book)
        Toasty.success(requireContext(),"updated",Toasty.LENGTH_LONG).show()
    }
}