package com.abby.booklendingsystem.ui.payments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.setFragmentResult
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.abby.booklendingsystem.R
import com.abby.booklendingsystem.adaper.recyclerview.CardsRecyclerViewAdapter
import com.abby.booklendingsystem.databinding.FragmentSelectPaymentBinding
import com.abby.booklendingsystem.enums.NetworkResult
import com.abby.booklendingsystem.interfaces.CreditCardClick
import com.abby.booklendingsystem.model.CardsModel
import com.abby.booklendingsystem.ui.viewbook.ViewBookModel
import com.abby.booklendingsystem.utils.*
import dagger.hilt.android.AndroidEntryPoint
import es.dmoral.toasty.Toasty
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


@AndroidEntryPoint
class SelectPaymentFragment : Fragment(),CreditCardClick {

     val ViewModel by activityViewModels<ViewBookModel>()


    private val adapters by lazy {
        CardsRecyclerViewAdapter(ViewModel,this,this)
    }

    lateinit var binding:FragmentSelectPaymentBinding
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentSelectPaymentBinding.inflate(inflater,container,false)
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

            lifecycleScope.launch {
                ViewModel.getCards()
            }


            binding.paypal.setOnClickListener {
                lifecycleScope.launch {
                    pay(PAYMENTMETHOD_PAYPAL)
                }
            }
            binding.applePay.setOnClickListener {
                lifecycleScope.launch {
                    pay(PAYMENTMETHOD_APPLEPAY)
                }
            }

            binding.cancel.setOnClickListener {
                findNavController().navigateUp()
            }
        }


        private fun initRecyclerView(){

            val layout = LinearLayoutManager(requireContext(),LinearLayoutManager.VERTICAL,false)
            binding.cardsRecyclerview.layoutManager = layout
            binding.cardsRecyclerview.adapter = adapters
            binding.toolbar.registerBackAction(this)
            binding.toolbar.registerBackAction(this)

        }



    override fun click(card: CardsModel) {
        lifecycleScope.launch {
            pay(PAYMENTMETHOD_CARD)
        }
    }

    private suspend fun pay(paymentMethod:String) = lifecycleScope.launch{
        Utils.showLoader(requireContext(),"Verifying Payment method..")
        delay(2000)
        Toasty.success(requireContext(),"Verified", Toasty.LENGTH_SHORT).show()
        Utils.dismissLoader()
        Utils.showLoader(requireContext(),"please wait ..")
        delay(2000)
        Utils.dismissLoader()
        setFragmentResult("PAY", bundleOf("METHOD" to paymentMethod))
        findNavController().navigateUp()
    }


}