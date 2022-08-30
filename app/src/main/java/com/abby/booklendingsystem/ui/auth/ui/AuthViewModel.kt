package com.abby.booklendingsystem.ui.auth.ui

import android.content.Context
import android.text.TextUtils
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.abby.booklendingsystem.data.datasource.FirebaseAuthDataSource
import com.abby.booklendingsystem.enums.LoginResult
import com.abby.booklendingsystem.model.UsersModel
import com.abby.booklendingsystem.utils.Utils
import dagger.hilt.android.lifecycle.HiltViewModel
import es.dmoral.toasty.Toasty
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    var firebaseAuthDataSource: FirebaseAuthDataSource
):ViewModel() {

    val createAccountStatus = MutableLiveData<LoginResult<Boolean>>()
    val loginStatus = MutableLiveData<LoginResult<UsersModel>>()

    suspend fun login(email: String,password: String,context: Context) = viewModelScope.launch {
        if(TextUtils.isEmpty(email) || TextUtils.isEmpty(password))
            Toasty.error(context,"Username and password must not be empty", Toasty.LENGTH_LONG).show()
        else {
            firebaseAuthDataSource.login(email, password,loginStatus)
        }
    }



    suspend fun createAccount(fullname: String, email: String, number: String, password: String,
                              confirmPassword: String, address: String,role:String,context:Context)
    = viewModelScope.launch{

        when{
            TextUtils.isEmpty(fullname) -> Toasty.info(context,"Fullname is required",Toasty.LENGTH_LONG).show()
            TextUtils.isEmpty(email) -> Toasty.info(context,"email is required",Toasty.LENGTH_LONG).show()
            TextUtils.isEmpty(number) -> Toasty.info(context,"number is required",Toasty.LENGTH_LONG).show()
            TextUtils.isEmpty(password) -> Toasty.info(context,"password is required",Toasty.LENGTH_LONG).show()
            password.length < 6 -> Toasty.info(context,"Password must be more than 6 characters",Toasty.LENGTH_LONG).show()
            password != confirmPassword -> Toasty.info(context,"Password did not match",Toasty.LENGTH_LONG).show()
            role == "" -> Toasty.info(context,"Please select role",Toasty.LENGTH_LONG).show()
            else ->{
                val user = UsersModel(email,fullname,number ,role,address,password)
                firebaseAuthDataSource.createAccount(user,createAccountStatus)

            }
        }
    }

}