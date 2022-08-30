package com.abby.booklendingsystem.ui.notifications

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.abby.booklendingsystem.data.datasource.*
import com.abby.booklendingsystem.enums.NetworkResult
import com.abby.booklendingsystem.model.NotificationModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NotificationsViewModel @Inject constructor(
    private val firebaseFirestoreDataSource: FirebaseFirestoreDataSource,
    private val firebaseAuthDataSource: FirebaseAuthDataSource,
    private val firebaseFireStoreApiSources: FirebasePurchasesDataSource
): ViewModel() {

    val notifications = MutableLiveData<NetworkResult<List<NotificationModel>>>()



    suspend fun getNotifications() = viewModelScope.launch{
        firebaseFirestoreDataSource.getNotifications(notifications)
    }

    fun updateRead() = viewModelScope.launch{
        if(notifications.value?.data!=null) {
            val value = notifications.value!!.data!!
            for (noti in value.iterator()) {
                if(noti.read==false) {
                    firebaseFirestoreDataSource.updateToRead(noti)
                    noti.read=true
                }
            }
        }
    }

}