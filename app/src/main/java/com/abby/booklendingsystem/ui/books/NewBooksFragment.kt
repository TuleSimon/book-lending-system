package com.abby.booklendingsystem.ui.books

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.abby.booklendingsystem.R
import com.abby.booklendingsystem.adaper.recyclerview.BooksRecyclerViewAdapter
import com.abby.booklendingsystem.databinding.FragmentBooks2Binding
import com.abby.booklendingsystem.enums.NetworkResult
import com.abby.booklendingsystem.interfaces.ViewBookClick
import com.abby.booklendingsystem.model.BookModel
import com.abby.booklendingsystem.ui.home.HomeFragmentDirections
import com.abby.booklendingsystem.ui.home.HomeViewModel
import com.abby.booklendingsystem.utils.AUTHOR_BOOKS
import com.abby.booklendingsystem.utils.Utils
import com.abby.booklendingsystem.utils.hide
import com.abby.booklendingsystem.utils.show
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.AndroidEntryPoint
import es.dmoral.toasty.Toasty
import jp.wasabeef.recyclerview.animators.SlideInLeftAnimator
import kotlinx.coroutines.launch

@AndroidEntryPoint
class NewBooksFragment(var type:String) : Fragment(), ViewBookClick{


    lateinit var binding:FragmentBooks2Binding
    private val adapters by lazy {
        BooksRecyclerViewAdapter(listen = this)
    }
    private val booksViewModel by activityViewModels<BooksViewModel>()
    private val homeViewModel by activityViewModels<HomeViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentBooks2Binding.inflate(inflater,container,false)
        initRecyclerView()
        initializeNetworkDependentValues()
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        booksViewModel.newBooks.observe(viewLifecycleOwner){
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
                            binding.errorlayout.imageView.setImageResource(R.drawable.ic_book_solid)
                            binding.errorlayout.textView.text = "No data Yet"
                            binding.errorlayout.retryButton.setOnClickListener {
                                getNewBooks()
                            }
                        }
                        adapters.setItems(this)
                    }
                }
            }
        }
        if(adapters.itemCount==0)
        getNewBooks()
    }

    private fun getNewBooks(){
        lifecycleScope.launch {
            if(type == AUTHOR_BOOKS)
            {
                FirebaseAuth.getInstance().currentUser?.let { booksViewModel.getNewBooks(it.uid) }
            }
            else
                booksViewModel.getNewBooks()
        }
    }


    private fun initRecyclerView(){
        val layout = GridLayoutManager(requireContext(),2)
        binding.booksRecyclerview.layoutManager = layout
        binding.booksRecyclerview.adapter = adapters
        binding.booksRecyclerview.itemAnimator = SlideInLeftAnimator()
    }

    override fun click(bookModel: BookModel) {
        val directions = HomeFragmentDirections.actionNavigationHomeToViewFragment(bookModel)
        findNavController().navigate(directions)
    }


    private fun reloadAllBooks(){
            getNewBooks()
    }
    private fun setFilter(){
        val list = booksViewModel.newBooks.value?.data

        binding.filterCategory.setOnItemSelectedListener { _, _, id, item ->
            val category =  item.toString()
            if(category.equals("All categories",true)){
                reloadAllBooks()
            }
            else{
                list?.apply {
                    val lists = this.filter { book ->
                        book.category.toString().equals(category,true) }
                    adapters.setItems(lists)
                }

            }
        }

        var inputted = false

        binding.searchGo.setOnClickListener {
            val searchText = binding.searchBooksText.text.toString()
            if(searchText.isEmpty() && !inputted) {
                Toasty.error(requireContext(), "search query must not be empty", Toasty.LENGTH_LONG)
                    .show()
                return@setOnClickListener
            }
            else if(inputted && searchText.isEmpty()){
                reloadAllBooks()
            }
            else{
                inputted = true
                list?.apply {
                    val lists = this.filter { book -> book.name.toString().contains(searchText,true) }
                    adapters.setItems(lists)
                }

            }
        }
    }


    private fun initializeNetworkDependentValues() = lifecycleScope.launch{
        binding.filterCategory.setItems("All categories")
        homeViewModel.getCategories()
        homeViewModel.categoriesResult.observe(viewLifecycleOwner){
            when(it){
                is NetworkResult.Error -> {
                    Utils.dismissLoader()
                    binding.filterCategory.isEnabled = false
                    Toasty.error(
                        requireContext(),
                        it.message ?: "An error occured",
                        Toasty.LENGTH_LONG
                    ).show() }

                is NetworkResult.Loading -> {
//                    Utils.showLoader(requireContext(),"loading..")
                    binding.filterCategory.isEnabled = false

                }

                is NetworkResult.Success -> {
                    setFilter()
                    it.data?.let { it1 ->
                        if(it1.isNotEmpty()){
                            binding.filterCategory.isEnabled=true
                            val list = mutableListOf("All categories")
                            list.addAll(it1.map { it.name.toString() }.toMutableList())
                            binding.filterCategory.setItems(list)
                            Utils.dismissLoader()
                        }
                    }

                }
            }
        }
    }



}