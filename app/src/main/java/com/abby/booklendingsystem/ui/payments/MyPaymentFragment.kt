package com.abby.booklendingsystem.ui.payments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.abby.booklendingsystem.R
import com.abby.booklendingsystem.adaper.recyclerview.CardsRecyclerViewAdapter
import com.abby.booklendingsystem.databinding.FragmentMyPaymentBinding
import com.abby.booklendingsystem.enums.NetworkResult
import com.abby.booklendingsystem.ui.viewbook.ViewBookModel
import com.abby.booklendingsystem.utils.hide
import com.abby.booklendingsystem.utils.registerBackAction
import com.abby.booklendingsystem.utils.show
import dagger.hilt.android.AndroidEntryPoint
import es.dmoral.toasty.Toasty
import kotlinx.coroutines.launch



@AndroidEntryPoint
class MyPaymentFragment : Fragment() {

    val ViewModel by activityViewModels<ViewBookModel>()
    private val adapters by lazy {
        CardsRecyclerViewAdapter(ViewModel,this)
    }




    lateinit var binding:FragmentMyPaymentBinding
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentMyPaymentBinding.inflate(inflater,container,false)
        initRecyclerView()
        return binding.root
    }

        override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
            super.onViewCreated(view, savedInstanceState)

            ViewModel.paymentMethods.observe(viewLifecycleOwner){
                binding.cardsRecyclerview.hideShimmer()
                binding.errorlayout.root.hide()
                when(it){
                    is NetworkResult.Loading ->{
                        binding.cardsRecyclerview.showShimmer()
                    }
                    is NetworkResult.Error -> {
                        binding.errorlayout.root.show()
                        binding.errorlayout.retryButton.setOnClickListener {
                            getCards()
                        }
                    }
                    is NetworkResult.Success -> {
                        val data = it.data
                        data?.apply {
                            if(this.isEmpty()){
                                binding.errorlayout.root.show()
                                binding.errorlayout.imageView.setImageResource(R.drawable.ic_credit_card_solid)
                                binding.errorlayout.textView.text = "No payment methods added yet"
                                binding.errorlayout.retryButton.setOnClickListener {
                                    getCards()
                                }
                            }
                            adapters.setItems(this)
                        }
                    }
                }
            }
            getCards()
        }

        private fun getCards(){
            binding.delete.setOnClickListener {
                if(ViewModel.selectedPaymentMethod.value==null){
                    Toasty.error(requireContext(),"No Card not selected", Toasty.LENGTH_LONG).show()
                }
                else{
                    adapters.removeItem(ViewModel.selectedPaymentMethod.value!!)
                    ViewModel.deleteCard()
                }
            }
            lifecycleScope.launch {
                ViewModel.getCards()
            }
        }


        private fun initRecyclerView(){

            val layout = LinearLayoutManager(requireContext(),LinearLayoutManager.VERTICAL,false)
            binding.cardsRecyclerview.layoutManager = layout
            binding.cardsRecyclerview.adapter = adapters
            addCard()
            binding.toolbar.registerBackAction(this)
        }


    private fun addCard(){
        binding.addCard.setOnClickListener {
            startAddingCards()
        }
    }

    private fun startAddingCards(){

        findNavController().navigate(R.id.action_myPaymentFragment_to_addCardFragment)
    }



}