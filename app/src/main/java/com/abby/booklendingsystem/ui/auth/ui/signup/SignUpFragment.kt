package com.abby.booklendingsystem.ui.auth.ui.signup

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.abby.booklendingsystem.R
import com.abby.booklendingsystem.databinding.FragmentSignUpBinding
import com.abby.booklendingsystem.enums.LoginResult
import com.abby.booklendingsystem.ui.auth.ui.AuthViewModel
import com.abby.booklendingsystem.utils.Utils
import dagger.hilt.android.AndroidEntryPoint
import es.dmoral.toasty.Toasty
import kotlinx.coroutines.launch
import java.util.*


@AndroidEntryPoint
class SignUpFragment : Fragment() {

    lateinit var binding:FragmentSignUpBinding

    private val authViewModel by activityViewModels<AuthViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentSignUpBinding.inflate(inflater,container,false)
        return binding.root
    }
    var role = ""


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.signupActivitySelectRole.setOnItemSelectedListener { _, _, id, item ->
            role = item.toString().lowercase(Locale.getDefault())
        }
        binding.signupActivitySelectRole.setItems("Reader","Author")

        binding.signupActivityCreateAccountBtn.setOnClickListener {
            createAccount()
        }

        authViewModel.createAccountStatus.observe(viewLifecycleOwner){
            when(it){
                is LoginResult.Error -> { Toasty.error(requireContext(),
                    it.message?:"An erro occurred",Toasty.LENGTH_LONG).show()}
                is LoginResult.Loading -> {
                    Utils.showLoader(requireContext(),"Creating your account...")
                }
                is LoginResult.Success -> {
                    Utils.dismissLoader()
                    Toasty.success(requireContext(),"account created, please sign in", Toasty.LENGTH_LONG).show()
                    findNavController().navigateUp()
                }
            }
        }


        binding.signupActivityAlreadyHaveAccount.setOnClickListener {
            findNavController().navigate(R.id.action_auth_signup_to_auth_login)
        }
    }

    private fun createAccount(){
        val fullname = binding.signupActivityFullname.text.toString()
        val email = binding.signupActivityEmail.text.toString()
        val number = binding.signupActivityNumber.text.toString()
        val address = binding.signupActivityAddress.text.toString()
        val password = binding.signupActivityPassword.text.toString()
        val confirmPassword = binding.signupActivityConfirmpassword.text.toString()

        lifecycleScope.launch {
            authViewModel.createAccount(fullname,email,number,password,confirmPassword,address,role,requireContext())
        }
    }


}