package com.example.deckvault

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {
    private lateinit var buttonLogin: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        buttonLogin = findViewById(R.id.login_btn)
        buttonLogin.setOnClickListener {
            val i = Intent(this, HomePage::class.java)
            startActivity(i)
        }
    }
}