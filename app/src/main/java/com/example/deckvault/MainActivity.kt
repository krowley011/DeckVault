package com.example.deckvault

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import com.google.firebase.auth.FirebaseAuth

class MainActivity : AppCompatActivity() {
    private lateinit var buttonLogin: Button

    // Firebase elements
    private lateinit var auth: FirebaseAuth
    //private lateinit var database: FirebaseDatabase


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        buttonLogin = findViewById(R.id.login_btn)
        buttonLogin.setOnClickListener {
            val i = Intent(this, NavigationActivity::class.java)
            startActivity(i)
        }
    }
}