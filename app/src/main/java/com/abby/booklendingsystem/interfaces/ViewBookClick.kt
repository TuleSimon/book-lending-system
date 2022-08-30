package com.abby.booklendingsystem.interfaces

import com.abby.booklendingsystem.model.BookModel

interface ViewBookClick {
    fun click(bookModel: BookModel)
}