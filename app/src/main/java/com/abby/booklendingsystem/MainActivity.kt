package com.abby.booklendingsystem

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.abby.booklendingsystem.data.datasource.FirebaseAuthDataSource
import com.abby.booklendingsystem.data.datasource.FirebaseCollections
import com.abby.booklendingsystem.data.datasource.FirebaseFirestoreDataSource
import com.abby.booklendingsystem.databinding.ActivityMainBinding
import com.abby.booklendingsystem.enums.NetworkResult
import com.abby.booklendingsystem.model.NotificationModel
import com.abby.booklendingsystem.ui.auth.AuthActivity
import com.abby.booklendingsystem.ui.home.HomeViewModel
import com.abby.booklendingsystem.ui.notifications.NotificationsViewModel
import com.abby.booklendingsystem.utils.hide
import com.abby.booklendingsystem.utils.show
import com.google.firebase.auth.FirebaseAuth
import com.gun0912.tedpermission.PermissionListener
import com.gun0912.tedpermission.TedPermission
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity(), PermissionListener {

    private lateinit var binding: ActivityMainBinding

    @Inject
    lateinit var firebaseAuthDataSource:FirebaseAuthDataSource

    private val viewModel by viewModels<NotificationsViewModel>()

    private val resultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                init()
            }
            else{
                LaunchAuth()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme(R.style.Theme_BookLendingSystem)
        binding = ActivityMainBinding.inflate(layoutInflater)
        checkIfLoggedIn()
    }





    private fun init(){
        setContentView(binding.root)

        val navView: BottomNavigationView = binding.navView

        val navController = findNavController(R.id.nav_host_fragment_activity_main)
        // Passing each menu ID as a set of Ids because each
        navView.setupWithNavController(navController)
        lifecycleScope.launch {
            viewModel.getNotifications()
        }
        viewModel.notifications.observe(this){
            when(it){
                is NetworkResult.Success -> {
                    val notifications = it.data
                    if(notifications!=null && notifications.isNotEmpty()){
                        val unread = notifications.filter { not -> not.read==false }
                        if(unread.isNotEmpty()){
                            binding.navView.getOrCreateBadge(R.id.navigation_notifications)
                                .number = unread.size
                        }
                    }
                }
            }
        }
        FirebaseAuth.getInstance().addAuthStateListener {
            if(it.currentUser==null){
                LaunchAuth()
            }
        }

        navController.addOnDestinationChangedListener{_,destination,_ ->
            if(destination.id==R.id.navigation_home || destination.id==R.id.navigation_notifications
                || destination.id==R.id.navigation_dashboard){
                binding.navView.show()
            }
            else{
                binding.navView.hide()
            }
        }
        FirebaseCollections.userNotificationCollection().addSnapshotListener(this) { value, error ->
            lifecycleScope.launch {
                viewModel.getNotifications()
            }
        }

    }

    fun hideBadge(){
        binding.navView.removeBadge(R.id.navigation_notifications)
    }



    private fun checkIfLoggedIn(){
        val user = firebaseAuthDataSource.getSignedInUser()
        if(user==null){
            LaunchAuth()
        }
        else{
            init()
        }
    }


    override fun onStart() {
        super.onStart()
        checkStoragePermission()
//        requestPermission()
    }

    override fun onResume() {
        super.onResume()
    }

    private fun checkStoragePermission() {
        TedPermission.with(this)
            .setPermissionListener(this)
            .setPermissions(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.CALL_PHONE)
            .check()
    }

    override fun onPermissionGranted() {
    }

    override fun onPermissionDenied(deniedPermissions: MutableList<String>?) {

    }




    private fun LaunchAuth(){
        val intent = Intent(this, AuthActivity::class.java)
        resultLauncher.launch(intent)
    }
}