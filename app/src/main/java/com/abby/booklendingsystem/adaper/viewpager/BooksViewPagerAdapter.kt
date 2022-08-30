package com.abby.booklendingsystem.adaper.viewpager

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.abby.booklendingsystem.ui.books.NewBooksFragment
import com.abby.booklendingsystem.ui.books.UsedBooksFragment
import kotlin.reflect.typeOf

class BooksViewPagerAdapter(

    fragment:Fragment,val type:String):FragmentStateAdapter(fragment) {

    override fun getItemCount(): Int {
        return 2
    }

    override fun createFragment(position: Int): Fragment {
        return if(position ==0)NewBooksFragment(type) else UsedBooksFragment(type)
    }
}