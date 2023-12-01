package com.example.deckvault

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class SignInActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var googleSignInClient: GoogleSignInClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signin)

        auth = FirebaseAuth.getInstance()

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(this, gso)

        findViewById<Button>(R.id.signin_btn).setOnClickListener {
            signInGoogle()
        }
    }

    private fun signInGoogle() {
        val signInIntent = googleSignInClient.signInIntent
        launcher.launch(signInIntent)
    }

    private val launcher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        result -> if (result.resultCode == Activity.RESULT_OK) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            handleResults(task)
        }
    }

    private fun handleResults(task: Task<GoogleSignInAccount>) {
        if (task.isSuccessful) {
            val account: GoogleSignInAccount? = task.result
            if (account != null) {
                updateUI(account)
            }
        } else {
            Toast.makeText(this, task.exception.toString(), Toast.LENGTH_SHORT).show()
        }
    }

    private fun updateUI(account: GoogleSignInAccount?) {
        if (account != null) {
            val credential = GoogleAuthProvider.getCredential(account.idToken, null)
            auth.signInWithCredential(credential).addOnCompleteListener { signInTask ->
                if (signInTask.isSuccessful) {
                    addUserToFirebase()
                } else {
                    Toast.makeText(this@SignInActivity, signInTask.exception.toString(), Toast.LENGTH_SHORT).show()
                }
            }
        } else {
            Toast.makeText(this@SignInActivity, "Google Sign-In failed", Toast.LENGTH_SHORT).show()
        }
    }

    private fun addUserToFirebase() {
        val curUser = auth.currentUser
        if (curUser != null) {
            val userDatabase = FirebaseDatabase.getInstance().getReference("UserData")
            val userQuery = userDatabase.orderByChild("userEmail").equalTo(curUser.email)

            userQuery.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    if (dataSnapshot.exists()) {
                        // User already exists in the database
                        val intent = Intent(this@SignInActivity, NavigationActivity::class.java)
                        startActivity(intent)
                    } else {
                        val user = UserClass()
                        user.userID = curUser.uid
                        user.userName = curUser.displayName.toString()
                        user.userEmail = curUser.email
                        user.cardCount = 0
                        user.deckCount = 0

                        // User doesn't exist in the database, add user data
                        userDatabase.child(curUser.uid).setValue(user)
                            .addOnSuccessListener {
                                // Data added successfully, proceed to NavigationActivity
                                val intent = Intent(this@SignInActivity, NavigationActivity::class.java)
                                startActivity(intent)
                            }
                            .addOnFailureListener { e ->
                                // Handle failure to add data
                                Toast.makeText(this@SignInActivity, "Failed to add user data: ${e.message}", Toast.LENGTH_SHORT).show()
                            }
                    }
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    // Handle database error
                    Toast.makeText(this@SignInActivity, "Database error: ${databaseError.message}", Toast.LENGTH_SHORT).show()
                }
            })
        }
    }
}
