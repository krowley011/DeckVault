package com.example.deckvault

import android.app.Dialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Picasso


class AddCardToSelectedDeck : Fragment(), AddCardAdapter.OnCardClickListener {
    private var auth = FirebaseAuth.getInstance()
    private var currUser = auth.currentUser
    private var addCardRecyclerList = ArrayList<AddCardRecyclerData>()
    private lateinit var cardRepo: CardRepository
    private lateinit var cardAdapter: AddCardAdapter
    private lateinit var cardListRecycler: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        cardListRecycler = view.findViewById(R.id.returnedCardSearchRecylView)
        // Grid layout to display two items per row
        val layoutManager = GridLayoutManager(requireContext(), 2)
        cardListRecycler.layoutManager = layoutManager

        cardAdapter = AddCardAdapter(addCardRecyclerList, this)
        cardListRecycler.adapter = cardAdapter
        cardListRecycler.setHasFixedSize(true)

        cardRepo = CardRepository(FirebaseDatabase.getInstance(), currUser!!)

        // Populate recycler view
        getInitializedCards()

        setUpSearchListener()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_add_card_to_selected_deck, container, false)
    }

    // Search listener for search bar, updates as the user types in a card name or subname
    private fun setUpSearchListener() {
        val searchView = requireView().findViewById<SearchView>(R.id.cardSearchView)

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                if (!query.isNullOrEmpty()) {
                    performSearch(query)
                } else {
                    getInitializedCards()
                }
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                // Optional: Perform search while the user types
                newText?.let { searchText ->
                    if (searchText.isNotEmpty()) {
                        performSearch(searchText)
                    } else {
                        // If the text is empty, reload all cards
                        getInitializedCards()
                    }
                }
                return true
            }
        })
    }

    private fun performSearch(query: String) {
        cardRepo.retrieveInitializedCards(object : CardRepository.CardDataCallback {
            override fun onSuccess(initializedCards: List<CardClass>) {
                // Compare user input to cards in the initialized cards
                val matchingCards = initializedCards.filter { card ->
                    card.cardName?.startsWith(query, ignoreCase = true) == true ||
                            card.cardSubName?.startsWith(query, ignoreCase = true) == true
                }

                // Convert from cardClass to AddCardRecyclerData to display cards
                val convertedCards = matchingCards.map { card ->
                    AddCardRecyclerData(
                        card.cardImage ?: "",
                        card.cardName ?: "",
                        card.cardSubName ?: "",
                        card.cardNumber ?: 0
                    )
                }

                addCardRecyclerList.clear()
                addCardRecyclerList.addAll(convertedCards)
                cardAdapter.notifyDataSetChanged()
            }

            override fun onError(databaseError: DatabaseError) {
                // Handle error case
                Log.e("GetInitializedCards", "Error fetching cards: $databaseError")
            }
        })
    }

    // Retrieve initialized cards to display in recycler view
    private fun getInitializedCards() {
        cardRepo.retrieveInitializedCards(object : CardRepository.CardDataCallback {
            override fun onSuccess(initializedCards: List<CardClass>) {
                addCardRecyclerList.clear()

                for (card in initializedCards) {
                    addCardRecyclerList.add(
                        AddCardRecyclerData(
                            card.cardImage,
                            card.cardName,
                            card.cardSubName,
                            card.cardNumber
                        )
                    )
                }
                cardAdapter.notifyDataSetChanged()
            }

            override fun onError(databaseError: DatabaseError) {
                // Handle error case
                Log.e("GetInitializedCards", "Error fetching cards: $databaseError")
            }
        })
    }

    override fun onCardClick(position: Int) {
        val selectedCard = addCardRecyclerList[position]

        val cardRef = FirebaseDatabase.getInstance().getReference("CardData").child(selectedCard.cardNumber.toString())

        cardRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                // Retrieve the detailed information of the card
                val detailedCard = dataSnapshot.getValue(CardClass::class.java)

                detailedCard?.let { card ->
                    val dialog = createDialogWithCardDetails(card)
                    dialog.show()
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Handle errors while retrieving detailed card info
                Log.e("CardDetailFetch", "Error fetching card info: $databaseError")
            }
        })
    }

    private fun createDialogWithCardDetails(card: CardClass): Dialog {
        // Initialize dialog box
        val dialog = Dialog(requireContext())
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(true)
        dialog.setContentView(R.layout.addcard_detailpopup)
        dialog.window?.setBackgroundDrawableResource(R.drawable.rounded_dialog)

        // Find views in the dialog layout
        val cardImage: ImageView = dialog.findViewById(R.id.addCardPU_Image)
        val cardName: TextView = dialog.findViewById(R.id.addCardPU_Name)
        val cardSubName: TextView = dialog.findViewById(R.id.addCardPU_Subname)

        // Set data to the views
        cardName.text = card.cardName
        cardSubName.text = card.cardSubName
        Picasso.get().load(card.cardImage).into(cardImage)

        val addBTN: Button = dialog.findViewById(R.id.addcardPU_BTN)

        // Set onClickListener for the add button
        addBTN.setOnClickListener {
            val selectedDeck: DeckClass? = arguments?.getParcelable("Selected Deck")
            if (selectedDeck != null) {
                addCard(card, viewLifecycleOwner)
            }
            dialog.dismiss() // Dismiss the dialog after adding the card
        }

        return dialog
    }

    private fun addCard(card: CardClass, owner: LifecycleOwner) {
        val selectedDeck: DeckClass? = arguments?.getParcelable("Selected Deck")
        val database = FirebaseDatabase.getInstance()
        val deckRef = database.getReference("UserData").child(currUser!!.uid).child("Decks")
        val userRef = database.getReference("UserData").child(currUser!!.uid)

        // Retrieve the list of decks for the user
        deckRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                for (deckSnapshot in dataSnapshot.children) {
                    val deck = deckSnapshot.getValue(DeckClass::class.java)
                    if (deck != null && deck.deckName == selectedDeck!!.deckName) {
                        val selectedDeckId = deckSnapshot.key // Retrieve the ID of the matching deck
                        if (selectedDeckId != null) {
                            // Add card to the deck
                            val cardDataRef = deckRef.child(selectedDeckId).child("Cards")
                            cardDataRef.push().setValue(card) // Add the card to the selected deck's cards

                            // Retrieve the decks card count and increment
                            val deckCardCountRef = deckRef.child(selectedDeckId).child("deckCardCount")
                            val userCardCountRef = userRef.child("cardCount")
                            deckCardCountRef.addListenerForSingleValueEvent(object : ValueEventListener {
                                override fun onDataChange(countSnapshot: DataSnapshot) {
                                    val currentCount = countSnapshot.getValue(Int::class.java) ?: 0
                                    deckCardCountRef.setValue(currentCount + 1)
                                }

                                override fun onCancelled(databaseError: DatabaseError) {
                                    // Handle errors while fetching the card count
                                    Log.e("AddCard", "Error fetching deck card count: $databaseError")
                                }
                            })

                            userCardCountRef.addListenerForSingleValueEvent(object : ValueEventListener {
                                override fun onDataChange(countSnapshot: DataSnapshot) {
                                    val currentCount = countSnapshot.getValue(Int::class.java) ?: 0
                                    userCardCountRef.setValue(currentCount + 1)
                                }

                                override fun onCancelled(databaseError: DatabaseError) {
                                    // Handle errors while fetching the card count
                                    Log.e("AddCard", "Error fetching user card count: $databaseError")
                                }
                            })
                            break
                        }
                    }
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Handle errors while fetching deck data
                Log.e("AddCard", "Error fetching deck data: $databaseError")
            }
        })
    }

}
