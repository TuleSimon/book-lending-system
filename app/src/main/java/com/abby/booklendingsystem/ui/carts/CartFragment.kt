package com.abby.booklendingsystem.ui.carts

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.setFragmentResultListener
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.abby.booklendingsystem.R
import com.abby.booklendingsystem.adaper.recyclerview.BooksRecyclerViewAdapter
import com.abby.booklendingsystem.databinding.FragmentBooks2Binding
import com.abby.booklendingsystem.databinding.FragmentCartBinding
import com.abby.booklendingsystem.enums.NetworkResult
import com.abby.booklendingsystem.interfaces.CartClicks
import com.abby.booklendingsystem.interfaces.ViewBookClick
import com.abby.booklendingsystem.model.BookModel
import com.abby.booklendingsystem.ui.books.BooksViewModel
import com.abby.booklendingsystem.ui.viewbook.ViewBookModel
import com.abby.booklendingsystem.utils.*
import dagger.hilt.android.AndroidEntryPoint
import es.dmoral.toasty.Toasty
import kotlinx.coroutines.launch

@AndroidEntryPoint
class CartFragment() : Fragment(), ViewBookClick,CartClicks {


    lateinit var binding:FragmentCartBinding
    private val adapters by lazy {
        BooksRecyclerViewAdapter(listen = this, cart = true, cartlisten = this)
    }
    private val viewModel by activityViewModels<ViewBookModel>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        if(view==null) {
            binding = FragmentCartBinding.inflate(inflater, container, false)
            initRecyclerView()
            getNewBooks()
        }
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.cartItems.observe(viewLifecycleOwner){
            binding.booksRecyclerview.hideShimmer()
            binding.errorlayout.root.hide()
            when(it){
                is NetworkResult.Loading ->{
                    binding.booksRecyclerview.showShimmer()
                    binding.checkOut.text = ""
                }
                is NetworkResult.Error -> {
                    binding.errorlayout.root.show()
                    binding.errorlayout.retryButton.setOnClickListener {
                        getNewBooks()
                    }
                    binding.checkOut.text = ""

                }
                is NetworkResult.Success -> {
                    val data = it.data
                    data?.apply {
                        if(this.isEmpty()){
                            binding.errorlayout.root.show()
                            binding.errorlayout.imageView.setImageResource(R.drawable.ic_baseline_add_shopping_cart_24)
                            binding.errorlayout.textView.text = "No items added to cart"
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
                            binding.checkOut.text = "Checkout £${price}"
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
            viewModel.getCartItems()
        }
        makePayment()
    }


    private fun initRecyclerView(){
        val layout = GridLayoutManager(requireContext(),2)
        binding.booksRecyclerview.layoutManager = layout
        binding.booksRecyclerview.adapter = adapters
    }

    override fun click(bookModel: BookModel) {
        val directions = CartFragmentDirections.actionCartFragmentToViewFragment(bookModel)
        findNavController().navigate(directions)
    }

    override fun removeFromCart(bookModel: BookModel) {
        lifecycleScope.launch {
            viewModel.removeFromCart(bookModel)
        }
        adapters.removeItem(bookModel)
    }

    private fun makePayment(){

        binding.checkOut.setOnClickListener {
            if(adapters.books.isNotEmpty()) {
                viewModel.purchaseItems = adapters.books
                val directions = CartFragmentDirections.actionCartFragmentToSelectPaymentFragment()
                findNavController().navigate(directions)
            }
            else{
                Toasty.error(requireContext(),"cart is empty", Toasty.LENGTH_LONG).show()
            }
        }

        setFragmentResultListener("PAY"){_,bundle ->
            val method = bundle.getString("METHOD")
            if(method==null){
                Toasty.error(requireContext(),"An error occurred while making payment", Toasty.LENGTH_LONG).show()
            }
            else {
                lifecycleScope.launch {
                    Utils.showLoader(requireContext(), "making payment..")
                    finalizePurchase(method)
                    Utils.dismissLoader()
                    Toasty.success(requireContext(), "Purchase completed", Toasty.LENGTH_LONG)
                        .show()
                    getNewBooks()
                    viewModel.purchaseItems.clear()

                }
            }
        }
    }


    private suspend fun finalizePurchase(method:String){
            Toasty.success(requireContext(),"finalizing", Toasty.LENGTH_SHORT).show()
            val deliveryMethod = HelperFunctions.showSelectDeliveryMethodDialog(this@CartFragment)
            viewModel.makePurchases(method, deliveryMethod)
    }


}