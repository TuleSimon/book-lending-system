package com.abby.booklendingsystem.ui.payments.addCard.creditcardscreen.ui

import android.animation.AnimatorInflater
import android.animation.AnimatorSet
import android.graphics.drawable.AnimatedVectorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.abby.booklendingsystem.R
import com.abby.booklendingsystem.databinding.FragmentAddCardBinding
import com.abby.booklendingsystem.model.CardsModel
import com.abby.booklendingsystem.ui.payments.addCard.creditcardscreen.extensions.Extensions.setCreditCardTextWatcher
import com.abby.booklendingsystem.ui.payments.addCard.creditcardscreen.extensions.Extensions.setExpiryDateFilter
import com.abby.booklendingsystem.ui.payments.addCard.creditcardscreen.utils.CardType
import com.abby.booklendingsystem.ui.viewbook.ViewBookModel
import com.abby.booklendingsystem.utils.Utils
import com.abby.booklendingsystem.utils.registerBackAction
import dagger.hilt.android.AndroidEntryPoint
import es.dmoral.toasty.Toasty
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import render.animations.*

@AndroidEntryPoint
class AddCardFragment : Fragment() {

    private lateinit var binding: FragmentAddCardBinding
    private val viewModel by viewModels<MainViewModel>()
    private val viewModel2 by viewModels<ViewBookModel>()

    lateinit var flipLeftIn: AnimatorSet
    lateinit var flipLeftOut: AnimatorSet
    lateinit var flipRightOut: AnimatorSet
    lateinit var flipRightIn: AnimatorSet

    lateinit var animatedVectorDrawable: AnimatedVectorDrawable

    private var isFront = true

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = FragmentAddCardBinding.inflate(layoutInflater,container,false)
        binding.apply {
            lifecycleOwner = this@AddCardFragment
            viewModel = this@AddCardFragment.viewModel
            frontSide.viewModel = this@AddCardFragment.viewModel
            frontSide.lifecycleOwner = this@AddCardFragment
            backSide.viewModel = this@AddCardFragment.viewModel
            backSide.lifecycleOwner = this@AddCardFragment
        }

        setupAnimation()
        setupViews()
        binding.saveBtn.setOnClickListener {
            onClick(it)
        }

        binding.btn.setOnClickListener {
            onClick(it)
        }
        binding.toolbar.registerBackAction(this)
        return binding.root
    }

    private fun setupAnimation() {
        val scale = requireContext().resources.displayMetrics.density
        binding.frontSide.frontSide.cameraDistance = 16000 * scale
        binding.backSide.backSide.cameraDistance = 16000 * scale

        flipLeftIn =
            AnimatorInflater.loadAnimator(requireContext(), R.animator.card_flip_left_in) as AnimatorSet
        flipLeftOut =
            AnimatorInflater.loadAnimator(requireContext(), R.animator.card_flip_left_out) as AnimatorSet
        flipRightIn =
            AnimatorInflater.loadAnimator(requireContext(), R.animator.card_flip_right_in) as AnimatorSet
        flipRightOut =
            AnimatorInflater.loadAnimator(requireContext(), R.animator.card_flip_right_out) as AnimatorSet
    }

    private fun flipCard() {
        if (isFront) {
            flipRightIn.setTarget(binding.backSide.backSide)
            flipRightOut.setTarget(binding.frontSide.frontSide)
            flipRightIn.start()
            flipRightOut.start()
        } else {
            flipLeftIn.setTarget(binding.frontSide.frontSide)
            flipLeftOut.setTarget(binding.backSide.backSide)
            flipLeftIn.start()
            flipLeftOut.start()
        }
        isFront = !isFront
    }

    private fun setupViews() {
        binding.etCardNumber.setCreditCardTextWatcher()
        binding.etExpiryDate.setExpiryDateFilter()

        viewModel.cardNumber.observe(viewLifecycleOwner
        ) { value ->
            replacePlaceholder(value)
            checkCardType(value)
        }

        binding.etCardCvv.setOnFocusChangeListener { view, b ->
            if (b) {
                flipCard()
            } else {
                MainScope().launch {
                    delay(400)
                    flipCard()
                }
            }
        }
    }

    private fun replacePlaceholder(value: String) {
        var placeholderString = "**** **** **** ****"

        value.forEach {
            val sb = StringBuilder(placeholderString)
            val firstIndex = placeholderString.indexOf("*")
            sb.setCharAt(firstIndex, it)
            placeholderString = sb.toString()
        }
        viewModel.cardNumberPlaceholder.postValue(placeholderString)
    }

    private fun checkCardType(value: String) {
        if (value.length < 4) {
            viewModel.cardType.value = CardType.NO_TYPE
            return
        }

        if (value.startsWith("4")) {
            viewModel.cardType.value = CardType.VISA_CARD
        } else {
            viewModel.cardType.value = CardType.MASTER_CARD
        }
    }

    private fun saveCard() {
        Utils.showLoader(requireContext(),"Saving..")


        MainScope().launch {
            val saved = viewModel2.saveCard(CardsModel(null,viewModel.cardNumber.value,
            viewModel.cardHolderName.value,viewModel.cardCVV.value,viewModel.cardExpiry.value,
                viewModel.cardType.value?.toString()
            ))
            Utils.dismissLoader()
            if (saved) {
                viewModel.cardSaved.value = true
                var render = Render(requireContext()).also { it.setAnimation(Slide.InUp(binding.tempImg)) }
                render.start()

                MainScope().launch {
                    binding.tvCardSaved.visibility = View.VISIBLE
                    var textRender =
                        Render(requireContext()).also { it.setAnimation(Fade.InUp(binding.tvCardSaved)) }
                    textRender.start()
                }
                val drawable = binding.done.drawable
                animatedVectorDrawable = drawable as AnimatedVectorDrawable
                animatedVectorDrawable.start()
                delay(1000)
                findNavController().navigateUp()
            }

            else{
                Toasty.error(requireContext(),"Card not saved",Toasty.LENGTH_LONG).show()
            }

        }

    }

    fun onClick(view: View) {
        when (view.id) {
            R.id.save_btn -> saveCard()
            R.id.btn -> flipCard()
        }
    }
}