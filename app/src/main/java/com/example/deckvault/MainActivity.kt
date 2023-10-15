package com.example.deckvault

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button

class MainActivity : AppCompatActivity() {
    private lateinit var buttonLogin: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        buttonLogin = findViewById(R.id.login_btn)
        buttonLogin.setOnClickListener {
            val i = Intent(this, NavigationActiviy::class.java)
            startActivity(i)
        }
    }
}