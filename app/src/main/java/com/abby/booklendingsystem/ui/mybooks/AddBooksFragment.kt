package com.abby.booklendingsystem.ui.mybooks

import android.graphics.Bitmap
import android.os.Bundle
import android.provider.MediaStore
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.abby.booklendingsystem.databinding.FragmentAddBooksBinding
import com.abby.booklendingsystem.enums.NetworkResult
import com.abby.booklendingsystem.model.BookModel
import com.abby.booklendingsystem.ui.home.HomeViewModel
import com.abby.booklendingsystem.utils.HelperFunctions
import com.abby.booklendingsystem.utils.Utils
import com.abby.booklendingsystem.utils.load
import com.abby.booklendingsystem.utils.registerBackAction
import com.canhub.cropper.CropImageContract
import com.canhub.cropper.CropImageView
import com.canhub.cropper.options
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.AndroidEntryPoint
import es.dmoral.toasty.Toasty
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.*

const val NEW_BOOK = "New Books"
const val OLD_BOOK = "Old Books"
const val UPDATE = "UPDATE"
const val ADD = "ADD"

@AndroidEntryPoint
class AddBooksFragment : Fragment() {

    private var _binding: FragmentAddBooksBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    val homeViewModel by activityViewModels<HomeViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentAddBooksBinding.inflate(inflater, container, false)
        val root: View = binding.root
        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.addBookCategory.setOnItemSelectedListener { _, _, id, item ->
            category = item.toString().uppercase(Locale.getDefault())
        }
        binding.addBookType.setOnItemSelectedListener { _, _, id, item ->
            type = item.toString()
        }


        binding.addBookType.setItems(listOf(NEW_BOOK, OLD_BOOK))

        binding.addBookSelectImage.setOnClickListener {
            startCrop()
        }
        binding.addBookPublishBtn.setOnClickListener {
            publishBook()
        }
        binding.toolbar.registerBackAction(this)
    }

    override fun onPause() {
        super.onPause()
        binding.addBookCategory.setItems(mutableListOf<String>())
        homeViewModel.categoriesResult.value=null
    }

    override fun onResume() {
        super.onResume()
        homeViewModel.categoriesResult.value?.data?.apply {
            binding.addBookCategory.setItems(this.map { it.name }.toMutableList())
        }?:initializeNetworkDependentValues()
        binding.addBookCategory.isEnabled=true
        binding.addBookCategory.setOnClickListener {
            binding.addBookCategory.expand()
            binding.addBookCategory.setOnClickListener(null)
        }
    }


    private fun initializeNetworkDependentValues() = lifecycleScope.launch{
        homeViewModel.getCategories()
        homeViewModel.categoriesResult.observe(viewLifecycleOwner){
            when(it){
            is NetworkResult.Error -> {
                Utils.dismissLoader()
            Toasty.error(
                requireContext(),
                it.message ?: "An error occured",
                Toasty.LENGTH_LONG
            ).show() }

             is NetworkResult.Loading -> {
                 Utils.showLoader(requireContext(),"loading..")
            }

            is NetworkResult.Success -> {
                it.data?.let { it1 ->
                    if(it1.isNotEmpty()){
                        binding.addBookCategory.isEnabled=true
                        binding.addBookCategory.setItems(it1.map { it.name }.toMutableList())
                        Utils.dismissLoader()
                    }
                }
        }
        }
    }
    }




    private var bitmap: Bitmap? = null
    private val cropImage = registerForActivityResult(CropImageContract()) { result ->
        if (result.isSuccessful) {
            // use the returned uri
            val uriContent = result.uriContent

//            val uriFilePath = result.getUriFilePath(requireContext()) // optional usage
            uriContent?.apply {
                bitmap = MediaStore.Images.Media.getBitmap(requireActivity().contentResolver, this)
                binding.addBookImage.load(this)
            }
        } else {
            // an error occurred
            val exception = result.error
        }
    }





    private fun startCrop() {
        // start picker to get image for cropping and then use the image in cropping activity
        cropImage.launch(
            options {
                setGuidelines(CropImageView.Guidelines.ON)
            }
        )
    }




    private var category = ""
    private var type = NEW_BOOK

    private fun publishBook(mode:String = ADD) {
        val title = binding.addBookTitle.text.toString()
        val author = FirebaseAuth.getInstance().currentUser?.displayName.toString()
        val authorid = FirebaseAuth.getInstance().currentUser?.uid.toString()
        val description = binding.addBookDescription.text.toString()
        val price = binding.addBookPrice.text.toString()
        val rating = binding.addBookRating.rating
        val categorys = category
        val type = type
        val availble = binding.available.isChecked
        val recommend = binding.recommend.isChecked
        val context = requireContext()
        when {
            TextUtils.isEmpty(title) -> Toasty.info(
                context,
                "title is required",
                Toasty.LENGTH_LONG
            ).show()
            TextUtils.isEmpty(description) -> Toasty.info(
                context,
                "description is required",
                Toasty.LENGTH_LONG
            ).show()
            TextUtils.isEmpty(price) -> Toasty.info(
                context,
                "price is required",
                Toasty.LENGTH_LONG
            ).show()
            TextUtils.isEmpty(categorys) -> Toasty.info(
                context,
                "Category is required",
                Toasty.LENGTH_LONG
            ).show()
            TextUtils.isEmpty(type) -> Toasty.info(context, "Type is required", Toasty.LENGTH_LONG)
                .show()
            (bitmap == null && mode== ADD) -> Toasty.info(context, "select an image for book", Toasty.LENGTH_LONG)
                .show()
            (bitmap==null && mode== UPDATE) -> {

            }
            else -> {
//                homeViewModel.uploadBookImage(title,author,authorid,description,price,rating,categorys,type,availble,recommend,requireContext(),bitmap)
                homeViewModel.uploadBookImage(bitmap!!)
                uploadImage()
                if(mode == ADD) {
                    listenToValue(
                        BookModel(
                            title.uppercase(),
                            author,
                            authorid,
                            description,
                            price,
                            rating,
                            categorys,
                            recommend,
                            availble,Calendar.getInstance().time,type
                        )
                    )
                }
            }
        }

    }

    private fun uploadImage() {
        lifecycleScope.launch {
            dialog = HelperFunctions.showUploadDialog(
                "Publishing Book", this@AddBooksFragment,
                homeViewModel.progress, homeViewModel.totalProgress
            )
        }
    }

    private var dialog: AlertDialog? = null
    private fun listenToValue(bookModel: BookModel, mode:String = ADD) {
        homeViewModel.bookuploadUri.observe(viewLifecycleOwner) {
            when (it) {
                is NetworkResult.Error -> {
                    dialog?.dismiss()
                    Toasty.error(
                        requireContext(),
                        it.message ?: "An error occured",
                        Toasty.LENGTH_LONG
                    ).show()
                }
                is NetworkResult.Success -> {

                    lifecycleScope.launch {
                        it.data?.let { it1 ->
                            bookModel.thumbb = it1.toString()
                            observeBook()
                            if(mode==ADD)
                                homeViewModel.publishBook(bookModel)
                            else
                                homeViewModel.updateBook(bookModel)
                            homeViewModel.bookuploadUri.removeObservers(viewLifecycleOwner)
                            homeViewModel.bookuploadUri.value=null
                        }
                    }
                }
            }
        }

    }


    private fun observeBook() {
        homeViewModel.publishBOokResult.observe(viewLifecycleOwner){
            when (it) {
                is NetworkResult.Error -> {
                    dialog?.dismiss()
                    Toasty.error(
                        requireContext(),
                        it.message ?: "An error occured",
                        Toasty.LENGTH_LONG
                    ).show()
                }
                is NetworkResult.Success -> {

                    lifecycleScope.launch {
                        it.data?.let { it1 ->
                            dialog?.dismiss()
                            Toasty.success(requireContext(),"Book Published", Toasty.LENGTH_LONG).show()
                            findNavController().navigateUp()
                        }
                    }
                }
            }
        }
    }

}