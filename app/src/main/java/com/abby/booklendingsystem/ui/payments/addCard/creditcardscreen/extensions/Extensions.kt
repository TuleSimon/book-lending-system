package com.abby.booklendingsystem.ui.payments.addCard.creditcardscreen.extensions

import com.abby.booklendingsystem.ui.payments.addCard.creditcardscreen.utils.CreditCardExpiryInputFilter
import com.abby.booklendingsystem.ui.payments.addCard.creditcardscreen.utils.CreditCardFormatTextWatcher
import com.google.android.material.textfield.TextInputEditText

object Extensions {

    fun TextInputEditText.setCreditCardTextWatcher() {
        val tv =
            CreditCardFormatTextWatcher(this)
        this.addTextChangedListener(tv)
    }

    fun TextInputEditText.setExpiryDateFilter() {
        this.filters = arrayOf(CreditCardExpiryInputFilter())
    }

}