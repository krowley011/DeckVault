package com.example.deckvault

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.LifecycleOwner
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Picasso

class CardDetailPage : Fragment() {
    private lateinit var database: FirebaseDatabase
    private var auth = FirebaseAuth.getInstance()
    private var currUser = auth.currentUser
    private lateinit var deckId: String
    private var selectedCard: CardClass? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    @SuppressLint("SetTextI18n")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val rootView = inflater.inflate(R.layout.fragment_card_detail_page, container, false)

        // Retrieve bundled data
        val bundle = arguments
        selectedCard = bundle?.getParcelable("selectedCard")


        // Assign values from bundle
        selectedCard?.let {
            val cardImage = it.cardImage
            val cardName = it.cardName
            val cardNumber = it.cardNumber
            var cardSubName = it.cardSubName
            var cardColor = it.cardColor
            var cardClasses = it.cardClasses
            val cardDamage = it.cardDamage
            val cardDefense = it.cardDefense
            var cardAction = it.cardAction
            val cardInk = it.cardInk
            val cardInkable = it.cardInkable
            val cardLore = it.cardLore
            var cardDescription = it.cardDescription
        }

        // Assign values to views
        val cardNameTopTV = rootView.findViewById<TextView>(R.id.cardPage_NameTopTV)
        cardNameTopTV.text = selectedCard!!.cardName
        val cardNameTV = rootView.findViewById<TextView>(R.id.cardPage_NameTV)
        cardNameTV.text = selectedCard!!.cardName
        val cardImageIV = rootView.findViewById<ImageView>(R.id.cardPage_Image)

        // Load image using Picasso from Firebase Storage URL into the ImageView
        val imageUrl = selectedCard!!.cardImage

        if (!imageUrl.isNullOrEmpty()) {
            Picasso.get().load(imageUrl).into(cardImageIV)
        } else {
            // Handle null or empty URL case
            Log.e("CardDetailPage", "Image URL is null or empty")
            // Show a placeholder image or handle the absence of an image in your UI
            Picasso.get().load(R.drawable.backofcard).into(cardImageIV)
        }

        // Change constraint layout and toolbar color based on card color
        val cardPageConstraintLayout = rootView.findViewById<ConstraintLayout>(R.id.cardPage_contraintLayout)
        val cardPageToolbar = rootView.findViewById<androidx.appcompat.widget.Toolbar>(R.id.cardPage_toolbar)

        // Map card colors to their respective color resources
        val colorRes = when (selectedCard?.cardColor) { // Replace this with the actual color value
            "Steel" -> R.color.steel
            "Amethyst" -> R.color.amethyst
            "Sapphire" -> R.color.sapphire
            "Ruby" -> R.color.ruby
            "Emerald" -> R.color.emerald
            else -> R.color.blue // Use default color if the selected card color is not recognized
        }

        // Get the color from resources
        val color = ContextCompat.getColor(requireContext(), colorRes)

        // Set the color to the Toolbar's background
        cardPageConstraintLayout.setBackgroundColor(color)
        cardPageToolbar.setBackgroundColor(color)


        val cardSubNameTV = rootView.findViewById<TextView>(R.id.cardPage_SubnameTV)
        cardSubNameTV.text = selectedCard!!.cardSubName

        val classList = selectedCard!!.cardClasses.toMutableList()
        val cardClassesTV = rootView.findViewById<TextView>(R.id.cardPage_ClassesTV)
        cardClassesTV.text = classList.joinToString(separator = " - ")

        val cardDamageTV = rootView.findViewById<TextView>(R.id.cardPage_DamageTV)
        cardDamageTV.text = "Damage - " + selectedCard?.cardDamage.toString()
        val cardDefenseTV =  rootView.findViewById<TextView>(R.id.cardPage_DefenseTV)
        cardDefenseTV.text = "Defense - " + selectedCard?.cardDefense.toString()
        val cardActionTV = rootView.findViewById<TextView>(R.id.cardPage_ActionTV)
        cardActionTV.text = selectedCard!!.cardAction

        // Change text based on if inkable or not
        val cardInkableTV = rootView.findViewById<TextView>(R.id.cardPage_InkableTV)
        if (selectedCard!!.cardInkable) {
            cardInkableTV.text = "Inkable - " + selectedCard?.cardInk.toString()+ " Ink Cost"
        } else {
            cardInkableTV.text = "Uninkable - " + selectedCard?.cardInk.toString() + " Ink Cost"
        }

        val cardLoreTV = rootView.findViewById<TextView>(R.id.cardPage_LoreTV)
        cardLoreTV.text = selectedCard!!.cardLore.toString() + " Lore"
        val cardDescriptionTV = rootView.findViewById<TextView>(R.id.cardPage_DescriptionTV)
        cardDescriptionTV.text = selectedCard!!.cardDescription

        // Setting up back button
        val backButton = rootView.findViewById<ImageButton>(R.id.cardPage_BackBTN)

        // Set OnClickListener for the back button
        backButton.setOnClickListener {
            // Navigate back to the previous fragment
            activity?.onBackPressed()
        }

        // Set up delete functions
        val cardId = selectedCard!!.cardNumber
        val cardRef = FirebaseDatabase.getInstance().getReference("UserData").child(currUser!!.uid).child("Decks")

        cardRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                deckId = null.toString() // Initialize deckId as nullable

                // Retrieve the deckId for the deck the card is in
                for (childSnapshot in snapshot.children) {
                    val cardsSnapshot = childSnapshot.child("Cards")
                    for (cardSnapshot in cardsSnapshot.children) {
                        val cardKey = cardSnapshot.key
                        if (cardKey == cardId.toString()) {
                            deckId =
                                childSnapshot.key.toString() // Store the deckId where the card is found
                            break
                        }
                    }
                    if (deckId != null) break // Break the outer loop once deckId is found
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle error
                Log.e("CardDetailPage", "Cannot retrieve deckId")
            }
        })

        // Setting up delete button
        val deleteButton = rootView.findViewById<Button>(R.id.cardPage_deleteBTN)

        deleteButton.setOnClickListener {
            selectedCard?.let { card ->
                removeCard(card, deckId, viewLifecycleOwner) { success ->
                    if (success) {
                        // Return to previous page
                        requireFragmentManager().popBackStackImmediate();
                    } else {
                        Log.e("CardDetailPage", "Failed to delete card")
                    }
                }
            } ?: run {
                Log.e("CardDetailPage", "Selected card is null")
            }
        }

        return rootView
    }

    private fun removeCard(card: CardClass, deckId: String, owner: LifecycleOwner, callback: (Boolean) -> Unit) {
        val database = FirebaseDatabase.getInstance()
        val userDataRef = database.getReference("UserData").child(currUser!!.uid)
        val cardRef = database.reference.child("UserData").child(currUser!!.uid).child("Decks")
            .child(deckId).child("Cards")
            .child(card.cardNumber.toString())

        // update user's card count
        userDataRef.child("cardCount").addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                var cardCount = (snapshot.value as? Long ?: 0).toInt()
                if (cardCount > 0) {
                    cardCount--
                }

                userDataRef.child("cardCount").setValue(cardCount)
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle error
                Log.e("CardDetailPage", "Failed to update card count")
            }
        })

        // Reference to the user's recent card list and remove the card
        val recentCardsRef = userDataRef.child("Recent Cards")
        recentCardsRef.child(card.cardNumber.toString()).removeValue()

        // Update deck's card count
        val deckRef = userDataRef.child("Decks").child(deckId)
        deckRef.child("deckCardCount").addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                var deckCardCount = snapshot.getValue(Int::class.java) ?: 0 // Get current deck's card count
                if (deckCardCount > 0) {
                    deckCardCount-- // Decrement deck's card count
                }

                // Update deck's card count
                deckRef.child("deckCardCount").setValue(deckCardCount)
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle error
                Log.e("CardDetailPage", "Failed to retrieve deck's card count")
            }
        })

        // Remove the card
        cardRef.removeValue().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                callback(true) // Deletion successful
            } else {
                Log.e("CardDetailPage", "Failed to delete card")
                callback(false) // Deletion failed
            }
        }
    }
}