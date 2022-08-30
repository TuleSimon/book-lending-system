package com.abby.booklendingsystem.ui.profile

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.abby.booklendingsystem.databinding.FragmentProfileBinding
import com.abby.booklendingsystem.ui.purchases.PurchasedBooksViewModel
import com.abby.booklendingsystem.utils.load
import com.abby.booklendingsystem.utils.setIfNotNull
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch


@AndroidEntryPoint
class MyProfileFragment : Fragment() {


    lateinit var binding: FragmentProfileBinding
    private val ViewModel by activityViewModels<PurchasedBooksViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentProfileBinding.inflate(inflater, container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        updateInfo()

    }


    private fun updateInfo(){
        val user = FirebaseAuth.getInstance().currentUser
        binding.profileEmail.setIfNotNull(user?.email)
        binding.profileName.setIfNotNull(user?.displayName)
        user?.photoUrl?.apply {
            binding.profileImage.load(this.toString())
        }
        lifecycleScope.launch {
            val author = ViewModel.getUserInfo(FirebaseAuth.getInstance().currentUser?.uid.toString())
            binding.profileAddress.setIfNotNull(author?.address)
            binding.profileAccountType.setIfNotNull(author?.role)
            binding.profileNumber.setIfNotNull(author?.number)
        }
    }

}