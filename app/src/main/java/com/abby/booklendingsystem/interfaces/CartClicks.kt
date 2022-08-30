package com.abby.booklendingsystem.interfaces

import com.abby.booklendingsystem.model.BookModel

interface CartClicks {
    fun removeFromCart(bookModel: BookModel)
}