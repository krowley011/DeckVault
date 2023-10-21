package com.example.deckvault

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView

class NavigationActivity : AppCompatActivity() {
    private lateinit var bottomNavigationView: BottomNavigationView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home_page)

        //Setting on click listener for bottom navigation bar
        bottomNavigationView = findViewById(R.id.bottomNavigationView)

        bottomNavigationView.setOnItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.home_bn -> {
                    replaceFragment(HomeFragment())
                    true
                }

                R.id.decks_bn -> {
                    replaceFragment(DeckFragment())
                    true
                }

                R.id.camera_bn -> {
                    replaceFragment(CameraFragment())
                    true
                }

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