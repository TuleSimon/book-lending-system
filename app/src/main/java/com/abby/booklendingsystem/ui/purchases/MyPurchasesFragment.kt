package com.abby.booklendingsystem.ui.purchases

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
import com.abby.booklendingsystem.adaper.recyclerview.PurchasedBooksRecyclerViewAdapter
import com.abby.booklendingsystem.databinding.FragmentBooks2Binding
import com.abby.booklendingsystem.databinding.FragmentMyPurchasesBinding
import com.abby.booklendingsystem.enums.NetworkResult
import com.abby.booklendingsystem.interfaces.ViewBookClick
import com.abby.booklendingsystem.model.BookModel
import com.abby.booklendingsystem.ui.books.BooksViewModel
import com.abby.booklendingsystem.ui.home.HomeFragmentDirections
import com.abby.booklendingsystem.utils.AUTHOR_BOOKS
import com.abby.booklendingsystem.utils.hide
import com.abby.booklendingsystem.utils.registerBackAction
import com.abby.booklendingsystem.utils.show
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MyPurchasesFragment() : Fragment(),ViewBookClick{


    lateinit var binding:FragmentMyPurchasesBinding
    private val pbViewModel by activityViewModels<PurchasedBooksViewModel>()
    private val adapters by lazy {
        PurchasedBooksRecyclerViewAdapter(this,pbViewModel)
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentMyPurchasesBinding.inflate(inflater,container,false)
        initRecyclerView()
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        pbViewModel.purchasedBooksResponse.observe(viewLifecycleOwner){
            binding.booksRecyclerview.hideShimmer()
            binding.tollbar.registerBackAction(this)
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
                            binding.errorlayout.imageView.setImageResource(R.drawable.ic_book_solid)
                            binding.errorlayout.textView.text = "No data Yet"
                            binding.errorlayout.retryButton.setOnClickListener {
                                getNewBooks()
                            }
                        }
                        adapters.setItems(this.sortedBy { it.datePurchased })
                    }
                }
            }
        }
        if(adapters.itemCount==0)
            getNewBooks()
    }

    private fun getNewBooks(){
        lifecycleScope.launch {
            FirebaseAuth.getInstance().currentUser?.uid?.let { pbViewModel.getPurchasedBooks(it) }
        }
    }


    private fun initRecyclerView(){
        val layout = LinearLayoutManager(requireContext(),LinearLayoutManager.VERTICAL,false)
        binding.booksRecyclerview.layoutManager = layout
        binding.booksRecyclerview.adapter = adapters
    }

    override fun click(bookModel: BookModel) {
        val direction = MyPurchasesFragmentDirections.actionMyPurchasesFragmentToViewPurchaseFragment(bookModel)
        findNavController().navigate(direction)
    }


}