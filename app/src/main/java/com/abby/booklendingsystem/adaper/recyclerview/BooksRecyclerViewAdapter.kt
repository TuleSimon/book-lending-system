package com.abby.booklendingsystem.adaper.recyclerview

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import androidx.viewbinding.ViewBindings
import com.abby.booklendingsystem.R
import com.abby.booklendingsystem.databinding.BookRowBinding
import com.abby.booklendingsystem.databinding.RecommendedBookRowBinding
import com.abby.booklendingsystem.interfaces.CartClicks
import com.abby.booklendingsystem.interfaces.ViewBookClick
import com.abby.booklendingsystem.model.BookModel
import com.abby.booklendingsystem.utils.load
import com.abby.booklendingsystem.utils.setIfNotNull
import com.abby.booklendingsystem.utils.show

class BooksRecyclerViewAdapter(
    var recommended:Boolean=false, var listen:ViewBookClick,
    var cart:Boolean=false, var cartlisten:CartClicks?=null
) : RecyclerView.Adapter<BooksRecyclerViewAdapter.BookViewHolder>() {

    inner class BookViewHolder(val binding: ViewBinding):RecyclerView.ViewHolder(binding.root) {

        fun bind(bookModel: BookModel,recommended: Boolean){
            binding.root.setOnClickListener {
                listen.click(bookModel)
            }
            if(recommended) {
                val bindings = binding as RecommendedBookRowBinding
                bindings.bookAuthor.setIfNotNull(bookModel.author)
                bindings.bookTitle.setIfNotNull(bookModel.name)
                bookModel.thumbb?.let { bindings.bookImage.load(it) }
                bindings.bookPrice.text =
                    bindings.bookPrice.context.getString(R.string.money_template, bookModel.price)
            }
            else{
                val bindings = binding as BookRowBinding
                bindings.bookAuthor.setIfNotNull(bookModel.author)
                bindings.bookTitle.setIfNotNull(bookModel.name)
                if(cart)
                    bindings.removeCart.apply {
                        show()
                        setOnClickListener {
                            cartlisten?.removeFromCart(bookModel)
                        }
                    }
                bookModel.thumbb?.let { bindings.bookImage.load(it) }
                bindings.bookPrice.text =
                    bindings.bookPrice.context.getString(R.string.money_template, bookModel.price)
            }

        }
    }
    var books = mutableListOf<BookModel>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BookViewHolder {
        return from(parent)
    }

    override fun onBindViewHolder(holder: BookViewHolder, position: Int) {
        val book = books[position]
        holder.bind(book,recommended)
    }

    fun from(parent: ViewGroup):BookViewHolder{
        val inflater = LayoutInflater.from(parent.context)
        val view =if(recommended)RecommendedBookRowBinding.inflate(inflater,parent,false)
        else BookRowBinding.inflate(inflater,parent,false)
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

