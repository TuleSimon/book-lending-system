package com.abby.booklendingsystem.ui.mybooks

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.abby.booklendingsystem.R
import com.abby.booklendingsystem.adaper.viewpager.BooksViewPagerAdapter
import com.abby.booklendingsystem.databinding.FragmentMyBooksBinding
import com.abby.booklendingsystem.ui.books.BooksViewModel
import com.abby.booklendingsystem.utils.AUTHOR_BOOKS
import com.abby.booklendingsystem.utils.registerBackAction
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MyBooksFragment : Fragment() {

    private var _binding: FragmentMyBooksBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    val ViewModel by activityViewModels<BooksViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {


        _binding = FragmentMyBooksBinding.inflate(inflater, container, false)
        val root: View = binding.root
        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

    binding.addBook.setOnClickListener {
            findNavController().navigate(R.id.action_myBooksFragment_to_addBooksFragment)
        }
        binding.tollbar.registerBackAction(this)
        setUpViewPager()
    }

    private fun setUpViewPager(){
        val viewpager = binding.viewpager
        viewpager.adapter = BooksViewPagerAdapter(this, AUTHOR_BOOKS)
        TabLayoutMediator(binding.tabLayout,viewpager){
                tab, position -> setTabText(position,tab)
        }.attach()
    }

    private fun setTabText(position:Int, tab: TabLayout.Tab){
        when(position){
            0 -> {tab.text = "New Books"
                tab.icon = ResourcesCompat.getDrawable(resources,R.drawable.ic_book_solid,
                    requireActivity().theme)
            }
            1 -> {tab.text = "Used Books"
                tab.icon = ResourcesCompat.getDrawable(resources,R.drawable.ic_book_open_solid
                    ,requireActivity().theme)
            }
            else -> tab.text = "Books"
        }
    }

}