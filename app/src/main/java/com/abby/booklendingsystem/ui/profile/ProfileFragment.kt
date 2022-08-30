package com.abby.booklendingsystem.ui.profile

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.abby.booklendingsystem.R
import com.abby.booklendingsystem.databinding.FragmentProfilesBinding
import com.abby.booklendingsystem.enums.NetworkResult
import com.abby.booklendingsystem.ui.home.HomeViewModel
import com.abby.booklendingsystem.ui.purchases.PurchasedBooksViewModel
import com.abby.booklendingsystem.utils.HelperFunctions
import com.abby.booklendingsystem.utils.hide
import com.abby.booklendingsystem.utils.load
import com.abby.booklendingsystem.utils.setIfNotNull
import com.canhub.cropper.CropImageContract
import com.canhub.cropper.CropImageView
import com.canhub.cropper.options
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.AndroidEntryPoint
import es.dmoral.toasty.Toasty
import kotlinx.coroutines.launch


@AndroidEntryPoint
class ProfileFragment : Fragment() {


    lateinit var binding: FragmentProfilesBinding
    private val homeViewModel by activityViewModels<HomeViewModel>()
    private val ViewModel by activityViewModels<PurchasedBooksViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentProfilesBinding.inflate(inflater, container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        updateInfo()
        binding.changeProfilepic.setOnClickListener {
            startCrop()
        }
        setCLicks()
    }

    private fun setCLicks(){
        binding.mybooks.setOnClickListener {
            findNavController().navigate(R.id.action_navigation_dashboard_to_myBooksFragment)
        }
        binding.cart.setOnClickListener {
            findNavController().navigate(R.id.action_navigation_dashboard_to_cartFragment)
        }
        binding.payments.setOnClickListener {
            findNavController().navigate(R.id.action_navigation_dashboard_to_myPaymentFragment)
        }

        binding.purchases.setOnClickListener {
            findNavController().navigate(R.id.action_navigation_dashboard_to_myPurchasesFragment)
        }
        binding.mysales.setOnClickListener {
            findNavController().navigate(R.id.action_navigation_dashboard_to_mySalesFragment)
        }

        binding.editProfile.setOnClickListener {
            findNavController().navigate(R.id.action_navigation_dashboard_to_myProfileFragment)
        }
        binding.logout.setOnClickListener {
            FirebaseAuth.getInstance().signOut()
        }
        binding.mywishlist.setOnClickListener {
            findNavController().navigate(R.id.action_navigation_dashboard_to_wishlistFragment)
        }
    }

    private fun updateInfo(){
        val user = FirebaseAuth.getInstance().currentUser
        binding.email.setIfNotNull(user?.email)
        binding.fullname.setIfNotNull(user?.displayName)
        user?.photoUrl?.apply {
            binding.userImage.load(this.toString())
        }
        lifecycleScope.launch {
            val author = ViewModel.getUserInfo(FirebaseAuth.getInstance().currentUser?.uid.toString())
            author?.apply {
                if(this.role.equals( "Reader",true)){
                    binding.mysales.hide()
                    binding.mybooks.hide()
                }
            }
        }
    }



    private var dialog:AlertDialog?=null
    private fun uploadImage(uri: Uri){
        val profilePicBitmap = MediaStore.Images.Media.getBitmap(requireActivity().contentResolver, uri)
        lifecycleScope.launch {
            profilePicBitmap?.apply {
                homeViewModel.uploadImage(profilePicBitmap, HelperFunctions.UserStorage(),true)
                listenToValue()
                dialog = HelperFunctions.showUploadDialog("Uploading Image",this@ProfileFragment,
                    homeViewModel.progress,homeViewModel.totalProgress)
            }
        }

    }

    private val cropImage = registerForActivityResult(CropImageContract()) { result ->
        if (result.isSuccessful) {
            // use the returned uri
            val uriContent = result.uriContent
            val uriFilePath = result.getUriFilePath(requireContext()) // optional usage
            uriContent?.apply {
                uploadImage(this)
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



    private fun listenToValue(){
        homeViewModel.uploadUri.observe(viewLifecycleOwner){
            when(it){
                is NetworkResult.Error -> {
                    dialog?.dismiss()
                    Toasty.error(requireContext(),it.message?:"An error occured",Toasty.LENGTH_LONG).show()
                }
                is NetworkResult.Success -> {
                    dialog?.dismiss()
                    lifecycleScope.launch {
                        it.data?.let { it1 -> homeViewModel.updateUserPhoto(it1)
                            binding.userImage.load(it1.toString())
                        }
                    }

                    Toasty.success(requireContext(),"Image uploaded",Toasty.LENGTH_LONG).show()
                }
            }
        }


    }


}