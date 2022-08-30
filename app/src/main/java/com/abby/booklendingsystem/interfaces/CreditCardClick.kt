package com.abby.booklendingsystem.interfaces

import com.abby.booklendingsystem.model.BookModel
import com.abby.booklendingsystem.model.CardsModel

interface CreditCardClick {
    fun click(card: CardsModel)
}