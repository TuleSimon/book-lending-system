package com.abby.booklendingsystem.ui.auth

import android.os.Bundle
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController

import com.abby.booklendingsystem.R
import com.abby.booklendingsystem.databinding.ActivityAuthBinding
import dagger.hilt.android.AndroidEntryPoint
import es.dmoral.toasty.Toasty

@AndroidEntryPoint
class AuthActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAuthBinding
    lateinit var  navController:NavController
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme(R.style.Theme_BookLendingSystem2)
        binding = ActivityAuthBinding.inflate(layoutInflater)
        setContentView(binding.root)


         navController = findNavController(R.id.nav_host_fragment_activity_auth)
        val appBarConfiguration = AppBarConfiguration(
            setOf(
               R.id.authHomeFragment, R.id.auth_login, R.id.auth_signup
            )
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.

    }

    override fun onBackPressed() {
        if(navController.currentDestination?.id==R.id.authHomeFragment)
        Toasty.info(this,"Please login",Toasty.LENGTH_LONG).show()
        else
            super.onBackPressed()
    }


}