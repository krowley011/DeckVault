package com.example.deckvault

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class HomeFragment : Fragment() {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val currUser = FirebaseAuth.getInstance().currentUser
        val welcomeUserTextView: TextView = view.findViewById(R.id.wel_user)
        val hpOverviewTV: TextView = view.findViewById(R.id.hpOverviewTV)

        if (currUser != null) {
            // Update welcome message
            val displayName = currUser.displayName ?: "User"
            val firstName = displayName.split(" ").firstOrNull() ?: "User"
            welcomeUserTextView.text = "Welcome $firstName!"

            // Retrieve user-related information from Firebase Database
            val userId = currUser.uid
            val database = FirebaseDatabase.getInstance()
            val deckCountRef = database.getReference("UserData/$userId/deckCount")
            val cardCountRef = database.getReference("UserData/$userId/cardCount")

            // Retrieve and update textview displaying card and deck count
            deckCountRef.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(deckDataSnapshot: DataSnapshot) {
                    val deckCount = deckDataSnapshot.getValue(Int::class.java) ?: 0

                    cardCountRef.addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(cardDataSnapshot: DataSnapshot) {
                            val cardCount = cardDataSnapshot.getValue(Int::class.java) ?: 0
                            hpOverviewTV.text = "You have $cardCount cards and $deckCount decks"
                        }

                        override fun onCancelled(cardDatabaseError: DatabaseError) {
                            // Handle error
                        }
                    })
                }

                override fun onCancelled(deckDatabaseError: DatabaseError) {
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

