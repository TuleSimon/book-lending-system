package com.abby.booklendingsystem.data.datasource

import android.graphics.Bitmap
import android.net.Uri
import androidx.lifecycle.MutableLiveData
import com.abby.booklendingsystem.enums.NetworkResult
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.StorageTask
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream
import javax.inject.Inject

class FirebaseUploader @Inject constructor() {

    suspend fun uploadProductImage(bitmap: Bitmap, ref:StorageReference,uri:MutableLiveData<NetworkResult<Uri>>,
                                   progress: MutableStateFlow<Long>?=null, totalProgress: MutableStateFlow<Long>?=null,
    ) {
        val baos = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 70, baos)
        val data = baos.toByteArray()
        val fileRef = ref.child("${System.currentTimeMillis()}.jpg")
        val uploadTask = fileRef.putBytes(data)
        uploadTask.addOnProgressListener {
            CoroutineScope(Main).launch {
                progress?.emit(it.bytesTransferred)
                totalProgress?.emit(it.totalByteCount)
            }
        }.addOnCompleteListener{
            if(it.isSuccessful){
                 it.result.metadata?.reference?.downloadUrl?.addOnCompleteListener { task->
                    if(task.isSuccessful){
                        uri.value =  NetworkResult.Success(task.result)
                    }
                     else{
                         uri.value = NetworkResult.Error(task.exception?.message?:"An error occured")
                     }
                }
            }
            else{
                uri.value = NetworkResult.Error(it.exception?.message?:"An error occured")
            }
        }
    }
}