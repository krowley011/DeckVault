package com.example.deckvault

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class HomeFragment : Fragment() {

    // ... (Other code remains the same)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val currentUser = FirebaseAuth.getInstance().currentUser
        val welcomeUserTextView: TextView = view.findViewById(R.id.wel_user)
        val hpOverviewTextView: TextView = view.findViewById(R.id.hpOverviewTV)

        if (currentUser != null) {
            // Update welcome message
            val displayName = currentUser.displayName ?: "User"
            val firstName = displayName.split(" ").firstOrNull() ?: "User"
            welcomeUserTextView.text = "Welcome $firstName!"

            // Retrieve user-related information from Firebase Database
            val userId = currentUser.uid
            val userRef = FirebaseDatabase.getInstance().getReference("users").child(userId)

            userRef.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    // Retrieve user-specific data
                    val userCardCount = dataSnapshot.child("cardCount").getValue(Int::class.java) ?: 0
                    val userDeckCount = dataSnapshot.child("deckCount").getValue(Int::class.java) ?: 0

                    // Update TextView with user-related information
                    hpOverviewTextView.text = "You have $userCardCount cards and $userDeckCount decks"
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    // Handle error
                }
            })
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

}
