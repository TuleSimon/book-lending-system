package com.abby.booklendingsystem.ui.auth.ui.login

import android.app.Activity.RESULT_OK
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.abby.booklendingsystem.R
import com.abby.booklendingsystem.databinding.FragmentLoginBinding
import com.abby.booklendingsystem.enums.LoginResult
import com.abby.booklendingsystem.ui.auth.ui.AuthViewModel
import com.abby.booklendingsystem.utils.Utils
import dagger.hilt.android.AndroidEntryPoint
import es.dmoral.toasty.Toasty
import kotlinx.coroutines.launch


@AndroidEntryPoint
class LoginFragment : Fragment() {

    lateinit var binding:FragmentLoginBinding
    private val authViewModel by activityViewModels<AuthViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentLoginBinding.inflate(inflater,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.loginActivityLoginBtn.setOnClickListener {
            login()
        }

        binding.loginActivityDontHaveAnAccount.setOnClickListener {
            findNavController().navigate(R.id.action_auth_login_to_auth_signup)
        }


        authViewModel.loginStatus.observe(viewLifecycleOwner){
            when(it){
                is LoginResult.Error -> {
                    Utils.dismissLoader()
                    Toasty.error(requireContext(),
                    it.message?:"An error occurred", Toasty.LENGTH_LONG).show()}
                is LoginResult.Loading -> {
                    Utils.showLoader(requireContext(),"Signing in,Please wait...")
                }
                is LoginResult.Success -> {
                    Utils.dismissLoader()
                    Toasty.success(requireContext(),"Welcome ${it.data?.fullname}", Toasty.LENGTH_LONG).show()
                    requireActivity().setResult(RESULT_OK)
                    requireActivity().finish()
                }
                else ->{
                    Utils.dismissLoader()
                }
            }
        }
    }

    private fun login() {
        val email = binding.loginActivityEmail.text.toString()
        val password = binding.loginActivityPassword.text.toString()
        lifecycleScope.launch {
            authViewModel.login(email, password,requireContext())
        }

    }


}