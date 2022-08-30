package com.abby.booklendingsystem.ui.auth.ui

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.abby.booklendingsystem.R
import com.abby.booklendingsystem.databinding.ActivityOnBoardBinding
import com.abby.booklendingsystem.databinding.FragmentSignUpBinding
import com.abby.booklendingsystem.enums.LoginResult
import com.abby.booklendingsystem.ui.auth.ui.AuthViewModel
import com.abby.booklendingsystem.utils.Utils
import dagger.hilt.android.AndroidEntryPoint
import es.dmoral.toasty.Toasty
import kotlinx.coroutines.launch
import java.util.*


@AndroidEntryPoint
class AuthHomeFragment : Fragment() {

    lateinit var binding:ActivityOnBoardBinding

    private val authViewModel by activityViewModels<AuthViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = ActivityOnBoardBinding.inflate(inflater,container,false)
        return binding.root
    }
    var role = ""


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.onboardActivityLoginBtn.setOnClickListener {
            findNavController().navigate(R.id.action_authHomeFragment_to_auth_login)
        }



        binding.onboardActivityCreateAccountBtn.setOnClickListener {
            findNavController().navigate(R.id.action_authHomeFragment_to_auth_signup)
        }
    }


}