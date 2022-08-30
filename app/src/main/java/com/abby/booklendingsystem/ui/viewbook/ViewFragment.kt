package com.abby.booklendingsystem.ui.viewbook

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.navArgs
import com.abby.booklendingsystem.R
import com.abby.booklendingsystem.databinding.FragmentViewBinding
import com.abby.booklendingsystem.enums.NetworkResult
import com.abby.booklendingsystem.model.BookModel
import com.abby.booklendingsystem.ui.books.BooksViewModel
import com.abby.booklendingsystem.utils.load
import com.abby.booklendingsystem.utils.observeOnce
import com.abby.booklendingsystem.utils.registerBackAction
import com.abby.booklendingsystem.utils.setIfNotNull
import dagger.hilt.android.AndroidEntryPoint
import es.dmoral.toasty.Toasty
import kotlinx.coroutines.launch


@AndroidEntryPoint
class ViewFragment : Fragment() {

    lateinit var binding:FragmentViewBinding
    val viewModel by activityViewModels<ViewBookModel>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentViewBinding.inflate(inflater,container,false)
        return binding.root
    }

    val args:ViewFragmentArgs by navArgs()
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val book = args.book
        updateValues(book)

        binding.addtoCart.setOnClickListener {
            Toasty.success(requireContext(), "Adding to cart", Toasty.LENGTH_LONG).show()
            lifecycleScope.launch {
                viewModel.addToCart(book)
            }
        }
        binding.addtoWishlist.setOnClickListener {
            lifecycleScope.launch {
                val status = viewModel.addToWishlist(book)
                if (status){
                    Toasty.success(
                        requireContext(),
                        "Added to wishlist",
                        Toasty.LENGTH_LONG
                    ).show()
                }
                else{
                    Toasty.error(
                        requireContext(),
                        "An error occured",
                        Toasty.LENGTH_LONG
                    ).show()
                }
            }
        }
        listen()
            binding.toolbar.registerBackAction(this)
    }

    private fun listen(){
        viewModel.addToCartResponse.observeOnce(viewLifecycleOwner){
            it?.apply {
                when (it) {
                    is NetworkResult.Success -> {

                    }
                    else -> {
                        Toasty.error(
                            requireContext(),
                            it.message ?: "An error occured",
                            Toasty.LENGTH_LONG
                        ).show()
                    }
                }
                viewModel.addToCartResponse.removeObservers(viewLifecycleOwner)
                viewModel.addToCartResponse.value = null
            }
        }
    }

    private fun updateValues(book:BookModel){
        binding.addBookDescription.setIfNotNull(book.description)
        binding.bookName.setIfNotNull(book.name)
        binding.bookAuthor.setIfNotNull(book.author)
        book.rating?.apply {
            binding.bookRating.rating = this
        }
        binding.toolbar.title = book.name
        binding.bookCategory.setIfNotNull(book.category)
        book.thumbb?.let { binding.addBookImage.load(it) }
        binding.addtoCart.text = getString(R.string.cart_template,book.price)
    }

}