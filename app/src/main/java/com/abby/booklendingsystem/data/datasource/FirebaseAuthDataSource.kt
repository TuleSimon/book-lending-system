package com.abby.booklendingsystem.data.datasource

import android.net.Uri
import androidx.lifecycle.MutableLiveData
import com.abby.booklendingsystem.enums.LoginResult
import com.abby.booklendingsystem.enums.NetworkResult
import com.abby.booklendingsystem.model.UsersModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.UserProfileChangeRequest
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.launch
import javax.inject.Inject

class FirebaseAuthDataSource @Inject constructor(

) {

    suspend fun login(email:String, password:String, value:MutableLiveData<LoginResult<UsersModel>>)
    {
        value.value = LoginResult.Loading()
        FirebaseAuth.getInstance().signInWithEmailAndPassword(email,password).addOnCompleteListener {
            if(it.isSuccessful){
                it.result.user?.uid?.apply {
                    CoroutineScope(IO).launch{
                        getUser(this@apply,value)
                    }
                }
            }
            else{
               value.value = LoginResult.Error(it.exception?.message?:"An error occured")
            }
        }
    }


     suspend fun getUser(id:String, value: MutableLiveData<LoginResult<UsersModel>>){

        FirebaseCollections.getUsersCollections().document(id).get().addOnCompleteListener {
            if(it.isSuccessful && it.result.exists()){
                val _user = it.result.toObject(UsersModel::class.java)
                if(_user!=null) {

                    if(FirebaseAuth.getInstance().currentUser?.displayName==null){
                        FirebaseAuth.getInstance().currentUser?.updateProfile(
                            UserProfileChangeRequest.Builder().apply {
                                displayName = _user.fullname
                            }.build()
                        )
                    }
                    value.value = LoginResult.Success(_user)
                }
                else
                    value.value = LoginResult.Error("User not found")
            }
            else{
                value.value = LoginResult.Error(it.exception?.message?:"An error occured")
            }
        }

    }

    suspend fun createAccount(user: UsersModel, value:MutableLiveData<LoginResult<Boolean>>) {
        value.value = LoginResult.Loading()
        FirebaseAuth.getInstance().createUserWithEmailAndPassword(user.email.toString(),
            user.password.toString()).addOnCompleteListener {
            if(it.isSuccessful){
                it.result.user?.uid?.apply {
                    user.userId = this
                    addUserValue(this, user, value)
                }
            }
            else{
                value.value = LoginResult.Error(it.exception?.message?:"An error occured")
            }
        }


    }

     fun addUserValue(uid:String,user: UsersModel, value: MutableLiveData<LoginResult<Boolean>>){
        FirebaseCollections.getUsersCollections().document(uid).set(user).addOnCompleteListener {
            if(it.isSuccessful){
                value.value = LoginResult.Success(true)

            }
            else{
                value.value = LoginResult.Error(it.exception?.message?:"An error occured")
            }
        }
    }

    fun getSignedInUser(): FirebaseUser? {
        return FirebaseAuth.getInstance().currentUser
    }


    fun updatePhoto(uri: Uri, value: MutableLiveData<NetworkResult<Boolean>>){
        FirebaseAuth.getInstance().currentUser?.updateProfile(
            UserProfileChangeRequest.Builder().apply {  photoUri = uri}.build()
        )?.addOnCompleteListener {
            if(it.isSuccessful){
                FirebaseCollections.getUsersCollections().document(FirebaseAuth.getInstance().currentUser?.uid.toString())
                    .update("profile_image",uri.toString()).addOnCompleteListener {
                        if(it.isSuccessful){
                            value.value = NetworkResult.Success(true)
                        }
                        else{
                            value.value = NetworkResult.Error(it.exception?.message?:"An error ocured")
                        }
                    }
            }
            else{
                value.value = NetworkResult.Error(it.exception?.message?:"An error ocured")
            }
        }
    }





    suspend fun getUserInfo(id:String):UsersModel?{
        val user = CompletableDeferred<UsersModel?>()
        FirebaseCollections.getUsersCollections().document(id).get().addOnCompleteListener {
            if(it.isSuccessful && it.result.exists()){
                val _user = it.result.toObject(UsersModel::class.java)
                if(_user!=null) {
                    user.complete(_user)
                }
                else
                    user.complete(null)
            }
            else{
                user.complete(null)
            }
        }
        return user.await()
    }
}