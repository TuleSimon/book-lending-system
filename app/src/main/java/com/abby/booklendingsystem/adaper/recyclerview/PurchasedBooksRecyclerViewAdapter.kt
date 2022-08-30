package com.abby.booklendingsystem.adaper.recyclerview

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewModelScope
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import androidx.viewbinding.ViewBindings
import com.abby.booklendingsystem.R
import com.abby.booklendingsystem.data.datasource.FirebasePurchasesDataSource
import com.abby.booklendingsystem.databinding.BookRowBinding
import com.abby.booklendingsystem.databinding.PurchasedBookRowBinding
import com.abby.booklendingsystem.databinding.RecommendedBookRowBinding
import com.abby.booklendingsystem.interfaces.CartClicks
import com.abby.booklendingsystem.interfaces.ViewBookClick
import com.abby.booklendingsystem.model.BookModel
import com.abby.booklendingsystem.model.UsersModel
import com.abby.booklendingsystem.ui.purchases.PurchasedBooksViewModel
import com.abby.booklendingsystem.utils.HelperFunctions
import com.abby.booklendingsystem.utils.load
import com.abby.booklendingsystem.utils.setIfNotNull
import com.abby.booklendingsystem.utils.show
import kotlinx.coroutines.launch

class PurchasedBooksRecyclerViewAdapter(
    var listen:ViewBookClick,
     val viewModel: PurchasedBooksViewModel
) : RecyclerView.Adapter<PurchasedBooksRecyclerViewAdapter.BookViewHolder>() {

    inner class BookViewHolder(val binding: PurchasedBookRowBinding):RecyclerView.ViewHolder(binding.root) {

        fun bind(bookModel: BookModel){
            binding.card.setOnClickListener {
                listen.click(bookModel)
            }
            binding.imageCard.setOnClickListener {
                listen.click(bookModel)
            }
            binding.bookAuthor.setIfNotNull(bookModel.author)
            binding.bookTitle.setIfNotNull(bookModel.name)
            bookModel.thumbb?.let { binding.bookImage.load(it) }

            var author:UsersModel?

            if(bookModel.isPickedUp == true){
                binding.bookStatus.setBackgroundColor(ContextCompat.getColor(binding.root.context,R.color.teal_200))
                when(bookModel.deliveryMethod){
                    HelperFunctions.DELIVERY_PICKUP -> {
                        binding.bookStatus.text = binding.root.context.getString(R.string.book_del_picked_up)

                        viewModel.viewModelScope.launch {
                           author = getUser(bookModel.authorID)
                            binding.deliveryStatus.text = "Book picked up from ${author?.address}"
                        }

                    }
                    else -> {
                        binding.bookStatus.text = binding.root.context.getString(R.string.book_del_delivered)
                        viewModel.viewModelScope.launch {
                            author = getUser(bookModel.purchasedUser)
                            binding.deliveryStatus.text = "Book was delivered to your address: ${author?.address}"
                        }
                    }
                }
            }
            else{
                when(bookModel.deliveryMethod){
                    HelperFunctions.DELIVERY_PICKUP -> {
                        binding.bookStatus.text = binding.root.context.getString(R.string.book_del_awaitingpick)
                        viewModel.viewModelScope.launch {
                            author = getUser(bookModel.authorID)
                            binding.deliveryStatus.text = "Waiting for you to picked up the book from author" +
                                    " book store at ${author?.address}."
                        }
                    }
                    else -> {
                        binding.bookStatus.text = binding.root.context.getString(R.string.book_del_prog)
                        viewModel.viewModelScope.launch {
                            author = getUser(bookModel.purchasedUser)
                            binding.deliveryStatus.text = "Book is being delivered to your address: ${author?.address}, " +
                                    "this might take 1-4 days."
                        }
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
    var books = mutableListOf<BookModel>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BookViewHolder {
        return from(parent)
    }

    override fun onBindViewHolder(holder: BookViewHolder, position: Int) {
        val book = books[position]
        holder.bind(book)
    }

    fun from(parent: ViewGroup):BookViewHolder{
        val inflater = LayoutInflater.from(parent.context)
        val view = PurchasedBookRowBinding.inflate(inflater,parent,false)
        return BookViewHolder(view)
    }

    override fun getItemCount(): Int {
        return books.size
    }

    fun setItems(booklist: List<BookModel>){
        books = booklist.toMutableList()
        notifyDataSetChanged()
    }

    fun removeItem(bookModel: BookModel) {
        val index = books.indexOf(bookModel)
        books.removeAt(index)
        notifyItemRemoved(index)
    }

}

