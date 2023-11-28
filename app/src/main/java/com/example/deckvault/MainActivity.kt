package com.example.deckvault

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.widget.Toast
import androidx.lifecycle.LifecycleOwner
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.FirebaseDatabase

class MainActivity : AppCompatActivity() {

    // Firebase elements
    private lateinit var auth: FirebaseAuth


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        FirebaseApp.initializeApp(this)
        auth = FirebaseAuth.getInstance()
        val user = auth.currentUser

        // If user is signed in, go to home page. If not, go to sign in
        Handler().postDelayed({
            if (user != null) {
                getUserData(user)
                val i = Intent(this, NavigationActivity::class.java)
                startActivity(i)
            } else {
                val s = Intent(this, SignInActivity::class.java)
                startActivity(s)
            }
        }, 1500)
    }

    // Function to convert FirebaseUser to UserClass
    fun mapFirebaseUserToUserClass(firebaseUser: FirebaseUser?): UserClass? {
        return firebaseUser?.let {
            UserClass(
                userName = it.displayName ?: "",
                userEmail = it.email ?: "",
                userID = it.uid
            )
        }
    }

    private fun getUserData(currentUser: FirebaseUser) {
        val userDatabase = FirebaseDatabase.getInstance().getReference("UserData")
        userDatabase.child(currentUser.uid).get().addOnSuccessListener { dataSnapshot ->
            if (dataSnapshot.exists()) {
                val user: UserClass? = mapFirebaseUserToUserClass(currentUser)
                val database = FirebaseDatabase.getInstance()
                val userReference = database.getReference("Users").child(currentUser.uid)
                val cardRepo = user?.let { CardRepository(database, it) }

                userReference.child("userName").setValue((currentUser.displayName))
                userReference.child("email").setValue(currentUser.email)
                cardRepo?.addTestCards(this)
            }
        }.addOnFailureListener {
            Toast.makeText(this, "Failed to read database.", Toast.LENGTH_SHORT).show()
        }
    }
}
