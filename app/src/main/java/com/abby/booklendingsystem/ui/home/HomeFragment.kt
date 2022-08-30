package com.abby.booklendingsystem.ui.home

import android.content.res.Resources
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.abby.booklendingsystem.R
import com.abby.booklendingsystem.adaper.ImageSliderAdapter
import com.abby.booklendingsystem.adaper.recyclerview.BooksRecyclerViewAdapter
import com.abby.booklendingsystem.adaper.viewpager.BooksViewPagerAdapter
import com.abby.booklendingsystem.databinding.FragmentHomeBinding
import com.abby.booklendingsystem.enums.NetworkResult
import com.abby.booklendingsystem.interfaces.ViewBookClick
import com.abby.booklendingsystem.model.BookModel
import com.abby.booklendingsystem.ui.books.BooksViewModel
import com.abby.booklendingsystem.utils.*
import com.abby.booklendingsystem.utils.Utils.getUser
import com.example.digitalbooks.model.ImageListModel
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.google.firebase.auth.FirebaseAuth
import com.smarteist.autoimageslider.SliderAnimations
import dagger.hilt.android.AndroidEntryPoint
import es.dmoral.toasty.Toasty
import kotlinx.coroutines.launch

@AndroidEntryPoint
class HomeFragment : Fragment(), ViewBookClick {

    private var _binding: FragmentHomeBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    val homeViewModel by activityViewModels<HomeViewModel>()
    val bookViewModel by activityViewModels<BooksViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root
        binding.shimmer.show()
        setUpViewPager()
        initRecyclerView()
        getRecoomendedBooks()
        getImageLists()
        return root
    }




    private fun getImageLists() {
        homeViewModel.sliderImages.observe(viewLifecycleOwner){
            binding.shimmer.hide()
            when(it){

                is NetworkResult.Loading -> {
                    binding.shimmer.showShimmer()
                    binding.shimmer.show()
                }
                is NetworkResult.Error -> {
                    Toasty.error(requireContext(),it.message?:"An error occured",Toasty.LENGTH_LONG).show()
                }
                is NetworkResult.Success -> {
                    binding.homeFragmentImageSlider.show()
                    val list = it.data
                    list?.apply {
                        startImageSlider(this)
                    }

                }

            }
        }

        lifecycleScope.launch{
            if(homeViewModel.sliderImages.value?.data==null || homeViewModel.sliderImages.value?.data?.isEmpty()==true)
            homeViewModel.getImages()
        }

    updateInfo()
    }


    private fun updateInfo(){
        val user = FirebaseAuth.getInstance().currentUser
        if (user?.displayName!=null)
        binding.name.setIfNotNull("${user.displayName}")
        else
            binding.name.setIfNotNull(user?.email)

        user?.photoUrl?.apply {
            binding.profileImage.load(this.toString())
        }

        binding.cart.setOnClickListener {
            findNavController().navigate(R.id.action_navigation_home_to_cartFragment)
        }
    }



    private fun startImageSlider(list:List<ImageListModel>){
        binding.homeFragmentImageSlider.show()
        val adapter = ImageSliderAdapter(requireContext(),list)
        binding.homeFragmentImageSlider.setSliderAdapter(adapter)
        binding.homeFragmentImageSlider.setSliderTransformAnimation(
            SliderAnimations .SIMPLETRANSFORMATION)
        binding.homeFragmentImageSlider.startAutoCycle()
    }


        private fun setUpViewPager(){
            val viewpager = binding.viewpager
            viewpager.adapter = BooksViewPagerAdapter(this, ALL_BOOKS)
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

    val adapters by lazy {
        BooksRecyclerViewAdapter(true,this)
    }
    private fun getRecoomendedBooks(){
        if(adapters.itemCount==0)
        homeViewModel.getRecommendedBooks()
        homeViewModel.recommendedBooksResult.observe(viewLifecycleOwner){
            binding.recommendedBooks.hideShimmer()
            when(it){
                is NetworkResult.Loading ->{
                    binding.recommendedBooks.showShimmer()
                }
                is NetworkResult.Error -> {
                    binding.recommendedBooks.hide()

                }
                is NetworkResult.Success -> {
                    val data = it.data
                    data?.apply {
                        adapters.setItems(this)
                    }
                }
            }
        }

    }

    private fun initRecyclerView(){
        val layout = GridLayoutManager (requireContext(),1,GridLayoutManager.HORIZONTAL,false)
        binding.recommendedBooks.layoutManager = layout
        binding.recommendedBooks.adapter = adapters
    }

    override fun click(bookModel: BookModel) {
        val directions = HomeFragmentDirections.actionNavigationHomeToViewFragment(bookModel)
        findNavController().navigate(directions)
    }


}