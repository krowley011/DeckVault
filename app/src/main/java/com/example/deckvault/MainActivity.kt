package com.example.deckvault

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.LifecycleOwner
import com.google.firebase.Firebase
import com.google.firebase.FirebaseApp
import com.google.firebase.appcheck.appCheck
import com.google.firebase.appcheck.playintegrity.PlayIntegrityAppCheckProviderFactory
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.initialize


class MainActivity : AppCompatActivity() {

    // Firebase elements
    private lateinit var auth: FirebaseAuth


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        FirebaseApp.initializeApp(this)
        auth = FirebaseAuth.getInstance()
        val currUser = auth.currentUser
        val owner: LifecycleOwner = this

        // If user is signed in, go to home page. If not, go to sign in
        Handler().postDelayed({
            if (currUser != null) {
                // getUserData(currUser)
                val userRepo = UserDataRepository(FirebaseDatabase.getInstance(), currUser.uid)
                userRepo.fetchUserData()
                // Initialize all cards
                val cardRepo = currUser?.let { CardRepository(FirebaseDatabase.getInstance(), it) }
                cardRepo!!.initializeCards()

                Firebase.initialize(context = this)
                Firebase.appCheck.installAppCheckProviderFactory(
                    PlayIntegrityAppCheckProviderFactory.getInstance(),
                )
                val i = Intent(this, NavigationActivity::class.java)

                userRepo.stopUserListener()
                cardRepo.stopCardListener()

                startActivity(i)
            } else {
                val s = Intent(this, SignInActivity::class.java)
                startActivity(s)
            }

        }, 1500)
    }

    // Function to convert FirebaseUser to UserClass
    private fun mapFirebaseUserToUserClass(firebaseUser: FirebaseUser?): UserClass? {
        return firebaseUser?.let {
            UserClass(
                userName = it.displayName ?: "",
                userEmail = it.email ?: "",
                userID = it.uid,
                decks = mutableListOf()
            )
        }
    }

    private fun getUserData(currUser: FirebaseUser) {
        val userDatabase = FirebaseDatabase.getInstance().getReference("UserData")
        userDatabase.child(currUser.uid).get().addOnSuccessListener { dataSnapshot ->
            if (dataSnapshot.exists()) {
                val user: UserClass? = mapFirebaseUserToUserClass(currUser)
                val database = FirebaseDatabase.getInstance()
                val userReference = database.getReference("UserData").child(currUser.uid)
                val cardRepo = user?.let { CardRepository(database, currUser) }
                val deckRepo = user?.let { DeckRepository(database, currUser) }

                userReference.child("userName").setValue((currUser.displayName))
                userReference.child("email").setValue(currUser.email)
            }
        }.addOnFailureListener {
            Toast.makeText(this, "Failed to read database.", Toast.LENGTH_SHORT).show()
        }
    }
}
