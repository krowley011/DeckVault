package com.example.deckvault

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView

class NavigationActiviy : AppCompatActivity() {
    private lateinit var bottomNavigationView: BottomNavigationView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home_page)

        //Setting on click listener for bottom navigation bar
        bottomNavigationView = findViewById(R.id.bottomNavigationView)

        bottomNavigationView.setOnClickListener {menuItem ->
            //Selected home button
            when(menuItem.id) {
                R.id.home_bn -> {
                    replaceFragment(HomeFragment())
                    true
                }
            }
            //Selected decks button
            when(menuItem.id){
                R.id.decks_bn -> {
                    replaceFragment(DeckFragment())
                    true
                }
            }
            //Selected camera button
            when(menuItem.id){
                R.id.camera_bn -> {
                    replaceFragment(CameraFragment())
                    true
                }
            }
            //Selected settings button
            when(menuItem.id){
                R.id.settings_bn -> {
                    replaceFragment(SettingsFragment())
                    true
                }
                else -> false
            }
        }
        replaceFragment(HomeFragment())
    }

    private fun replaceFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction().replace(R.id.frame_layout, fragment).commit()
    }
}