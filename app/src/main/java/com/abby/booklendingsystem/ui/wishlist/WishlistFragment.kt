package com.abby.booklendingsystem.ui.wishlist

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.abby.booklendingsystem.R
import com.abby.booklendingsystem.adaper.recyclerview.BooksRecyclerViewAdapter
import com.abby.booklendingsystem.databinding.FragmentCartBinding
import com.abby.booklendingsystem.databinding.FragmentWishlistBinding
import com.abby.booklendingsystem.enums.NetworkResult
import com.abby.booklendingsystem.interfaces.ViewBookClick
import com.abby.booklendingsystem.model.BookModel
import com.abby.booklendingsystem.ui.home.HomeFragmentDirections
import com.abby.booklendingsystem.ui.viewbook.ViewBookModel
import com.abby.booklendingsystem.utils.*
import dagger.hilt.android.AndroidEntryPoint
import es.dmoral.toasty.Toasty
import kotlinx.coroutines.launch

@AndroidEntryPoint
class WishlistFragment() : Fragment(), ViewBookClick {


    lateinit var binding:FragmentWishlistBinding
    private val adapters by lazy {
        BooksRecyclerViewAdapter(listen = this)
    }
    private val viewModel by activityViewModels<ViewBookModel>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        if(view==null) {
            binding = FragmentWishlistBinding.inflate(inflater, container, false)
            initRecyclerView()
            getNewBooks()
        }
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.wishListItems.observe(viewLifecycleOwner){
            binding.booksRecyclerview.hideShimmer()
            binding.errorlayout.root.hide()
            when(it){
                is NetworkResult.Loading ->{
                    binding.booksRecyclerview.showShimmer()
                }
                is NetworkResult.Error -> {
                    binding.errorlayout.root.show()
                    binding.errorlayout.retryButton.setOnClickListener {
                        getNewBooks()
                    }

                }
                is NetworkResult.Success -> {
                    val data = it.data
                    data?.apply {
                        if(this.isEmpty()){
                            binding.errorlayout.root.show()
                            binding.errorlayout.imageView.setImageResource(R.drawable.ic_baseline_favorite_24)
                            binding.errorlayout.textView.text = "No items added to wishlist"
                            binding.errorlayout.retryButton.setOnClickListener {
                                getNewBooks()
                            }
                        }
                        else{
                            var price = 0
                            for(i in iterator()){
                                i.price?.apply {
                                    price += this.toInt()
                                }
                            }
                        }
                        adapters.setItems(this)
                    }
                }
            }
        }
        binding.tollbar.registerBackAction(this)

    }

    private fun getNewBooks(){
        lifecycleScope.launch {
            viewModel.getWishListCollection()
        }

    }


    private fun initRecyclerView(){
        val layout = GridLayoutManager(requireContext(),2)
        binding.booksRecyclerview.layoutManager = layout
        binding.booksRecyclerview.adapter = adapters
    }

    override fun click(bookModel: BookModel) {
        val directions = WishlistFragmentDirections.actionWishlistFragmentToViewFragment(bookModel)
        findNavController().navigate(directions)
    }


}