package com.example.deckvault

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.PopupMenu
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class SelectedDeck : Fragment(), SelectedDeckAdapter.OnCardClickListener {

    private var auth = FirebaseAuth.getInstance()
    private var currUser = auth.currentUser
    private lateinit var sdCardListRecycler: RecyclerView
    private lateinit var selectedDeckAdapter: SelectedDeckAdapter
    private var selectedDeckRecyclerList = ArrayList<SelectedDeckRecyclerData>()
    private lateinit var selectedDeckId: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val rootView = inflater.inflate(R.layout.fragment_selected_deck, container, false)
        val deckCountTV: TextView = rootView.findViewById(R.id.selectedDeckCardCountTV)
        val selectedDeck: DeckClass? = arguments?.getParcelable("Selected Deck")
        val database = FirebaseDatabase.getInstance()
        val deckRef = database.getReference("UserData").child(currUser!!.uid).child("Decks")

        // Update deck name on top of page with selected decks name
        val selectedDeckName = selectedDeck!!.deckName
        val selectedDeckNameTV = rootView.findViewById<TextView>(R.id.selectedDeckNameTV)
        selectedDeckNameTV.text = selectedDeckName

        // Setting up back button
        val backButton = rootView.findViewById<ImageButton>(R.id.selectedDeck_BackBTN)

        // Set OnClickListener for the back button
        backButton.setOnClickListener {
            // Navigate back to the previous fragment
            activity?.onBackPressed()
        }

        val valEventListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                // var selectedDeckId: String? = null

                for (snapshot in dataSnapshot.children) {
                    val deck = snapshot.getValue(DeckClass::class.java)

                    // Check if the deck's name matches the selected deck's name
                    if (deck != null && deck.deckName == selectedDeck?.deckName) {
                        selectedDeckId = snapshot.key.toString() // Retrieve the ID of the matching deck
                        break
                    }
                }

                // Use the retrieved selectedDeckId as needed
                if (selectedDeckId != null) {
                    // Perform actions with the selectedDeckId
                    // Get the card count of the deck
                    val deckCardCountRef = deckRef.child(selectedDeckId).child("deckCardCount")
                    deckCardCountRef.addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(countSnapshot: DataSnapshot) {
                            // Display the card count in the text view
                            val cardCount = countSnapshot.getValue(Int::class.java) ?: 0
                            deckCountTV.text = "Card Count: $cardCount"
                        }

                        override fun onCancelled(databaseError: DatabaseError) {
                            // Handle errors
                            Log.e("SelectedDeck", "Error retrieving deckCardCount")
                        }
                    })

                    getDeckInformationThenFetchCards()
                } else {
                    // Handle case where the selected deck is not found
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Handle errors
                Log.e("SelectedDeck", "Error: ${databaseError.message}")
            }
        }

        // Attach the listener
        deckRef.addListenerForSingleValueEvent(valEventListener)

        return rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val fab = view.findViewById<FloatingActionButton>(R.id.addCardFAB)

        // Create and replace the fragment when the FAB is clicked
        fab.setOnClickListener {
            retrieveSelectedDeckData()
        }

        setHasOptionsMenu(true)
        setupSelectedDeckPageMenu(view)

        sdCardListRecycler = view.findViewById(R.id.selectedDeckRecylView)
        // Grid layout to display two items per row
        val layoutManager = GridLayoutManager(requireContext(), 2)
        sdCardListRecycler.layoutManager = layoutManager

        selectedDeckAdapter = SelectedDeckAdapter(selectedDeckRecyclerList, this)
        sdCardListRecycler.adapter = selectedDeckAdapter
        sdCardListRecycler.setHasFixedSize(true)

        // Populate recycler view
        getDeckInformationThenFetchCards()

    }

    private fun setupSelectedDeckPageMenu(rootView: View) {
        val button =
            rootView.findViewById<ImageButton>(R.id.selectedDeckMenuBTN)
        button.setOnClickListener {
            val popup = PopupMenu(requireContext(), button)
            popup.menuInflater.inflate(R.menu.selecteddeckmenu, popup.menu)
            popup.setOnMenuItemClickListener { menuItem ->
                // Handle menu item click events here
                when (menuItem.itemId) {
                    R.id.SelectedDeckMenu_Select -> {
                        // Allow user to select multiple cards
                    }
                    R.id.SelectedDeckMenu_SortBy -> {
                        // Allow user to sort by filters
                    }
                }
                true
            }
            popup.show()
        }
    }

    private fun retrieveSelectedDeckData() {
        val deckRepo = DeckRepository(FirebaseDatabase.getInstance(), currUser!!)
        deckRepo.fetchDeckData()

        // Retrieve the arguments sent from the previous fragment
        arguments?.let { args ->
            val selectedDeck: DeckClass? = args.getParcelable("Selected Deck")

            deckRepo.isDeckDataReady.observe(viewLifecycleOwner, Observer { isDeckDataReady ->
                if (isDeckDataReady) {
                    // Use the retrieved selectedDeck object here as needed
                    if (selectedDeck != null) {
                        for (decks in deckRepo.Decks) {
                            if (selectedDeck.deckName == decks.deckName) {
                                // Add deck information into a bundle to pass to AddCardToSelectedDeck Fragment
                                val bundle = Bundle()
                                bundle.putParcelable("Selected Deck", decks)

                                // Pass the selected deck's information to the next fragment
                                val addCardFragment = AddCardToSelectedDeck()
                                addCardFragment.arguments = bundle

                                // Perform the fragment transaction
                                val transaction = requireActivity().supportFragmentManager.beginTransaction()
                                transaction.replace(R.id.frame_layout, addCardFragment)
                                transaction.addToBackStack(null)
                                transaction.commit()

                                break
                            }
                        }
                    }
                }
            })
        }
    }

    private fun getDeckInformationThenFetchCards() {
        val selectedDeck: DeckClass? = arguments?.getParcelable("Selected Deck")
        val database = FirebaseDatabase.getInstance()
        val deckRef = database.getReference("UserData").child(currUser!!.uid)
            .child("Decks")

        if (selectedDeck != null) {
            // Retrieving the deck id for the selected deck
            deckRef.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {for (deckSnapshot in dataSnapshot.children) {
                        val deck = deckSnapshot.getValue(DeckClass::class.java)
                        if (deck != null && deck.deckName == selectedDeck!!.deckName) {
                            val selectedDeckId = deckSnapshot.key.toString()

                            fetchCardsForSelectedDeck(selectedDeckId)
                            break
                        }
                    }
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    Log.e("SelectedDeck", "Error fetching deck data: $databaseError")
                }
            })
        } else {
            Log.e("SelectedDeck", "Selected Deck is null")
        }
    }

    // Function to fetch cards for the selected deck
    private fun fetchCardsForSelectedDeck(selectedDeckId: String) {
        val database = FirebaseDatabase.getInstance()
        val deckCardRef = database.getReference("UserData").child(currUser!!.uid)
            .child("Decks").child(selectedDeckId).child("Cards")

        val valEventListener = object : ValueEventListener {
            override fun onDataChange(cardSnapshot: DataSnapshot) {
                selectedDeckRecyclerList.clear()

                for (cardDataSnapshot in cardSnapshot.children) {
                   val card = cardDataSnapshot.getValue(CardClass::class.java)
                    card?.let {
                        val recyclerData = SelectedDeckRecyclerData(
                            card.cardImage ?: "",
                            card.cardName ?: "",
                            card.cardSubName ?: "",
                            card.cardNumber ?: 0
                        )
                        selectedDeckRecyclerList.add(recyclerData)
                    }
                }
                selectedDeckAdapter.notifyDataSetChanged()
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Handle errors
                Log.e("SelectedDeck", "Error: ${databaseError.message}")
            }
        }

        // Attach the listener
        deckCardRef.addListenerForSingleValueEvent(valEventListener)
    }

    override fun onCardClick(position: Int) {
        val clickedCard = selectedDeckRecyclerList[position]
        fetchSelectedCardDetails(clickedCard)
    }

    private fun fetchSelectedCardDetails(clickedCard: SelectedDeckRecyclerData) {
        val database = FirebaseDatabase.getInstance()
        val deckCardRef = database.getReference("UserData").child(currUser!!.uid)
            .child("Decks").child(selectedDeckId).child("Cards")

        val valEventListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                var selectedCard: CardClass? = null

                for (cardSnapshot in dataSnapshot.children) {
                    val cardNumber = cardSnapshot.child("cardNumber").getValue(Int::class.java)
                    if (cardNumber == clickedCard.cardNumber) {

                        val cardName =
                            cardSnapshot.child("cardName").getValue(String::class.java) ?: ""
                        val cardImage =
                            cardSnapshot.child("cardImage").getValue(String::class.java) ?: ""
                        val cardSubName =
                            cardSnapshot.child("cardSubName").getValue(String::class.java) ?: ""
                        val cardColor =
                            cardSnapshot.child("cardColor").getValue(String::class.java) ?: ""
                        val cardClassesSnapshot = cardSnapshot.child("cardClasses")

                        val cardClasses = mutableListOf<String>()
                        for (classSnapshot in cardClassesSnapshot.children) {
                            val className = classSnapshot.getValue(String::class.java)
                            className?.let {
                                cardClasses.add(it)
                            }
                        }

                        val cardDamage = cardSnapshot.child("cardDamage").getValue(Int::class.java) ?: 0
                        val cardDefense = cardSnapshot.child("cardDefense").getValue(Int::class.java) ?: 0
                        val cardAction = cardSnapshot.child("cardAction").getValue(String::class.java) ?: ""
                        val cardInk = cardSnapshot.child("cardInk").getValue(Int::class.java) ?: 0
                        val cardInkable = cardSnapshot.child("cardInkable").getValue(Boolean::class.java) ?: true
                        val cardLore = cardSnapshot.child("cardLore").getValue(Int::class.java) ?: 0
                        val cardDescription = cardSnapshot.child("cardDescription").getValue(String::class.java) ?: ""


                        // Create a CardClass object manually
                        selectedCard = CardClass(
                            cardName,
                            cardImage,
                            cardNumber,
                            cardSubName,
                            cardColor,
                            cardClasses,
                            cardDamage,
                            cardDefense,
                            cardAction,
                            cardInk,
                            cardInkable,
                            cardLore,
                            cardDescription
                        )
                        break
                    }
                }

                selectedCard.let { card ->
                    // Bundle the card details
                    val bundle = Bundle()
                    bundle.putParcelable("selectedCard", card)

                    // Pass the bundle to the new fragment
                    val cardDetailPage = CardDetailPage()
                    cardDetailPage.arguments = bundle

                    val transaction = requireActivity().supportFragmentManager.beginTransaction()
                    transaction.replace(R.id.frame_layout, cardDetailPage)
                    transaction.addToBackStack(null)
                    transaction.commit()
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Handle errors
                Log.e("SelectedDeck:FetchCardDetails", "Error: ${databaseError.message}")
            }
        }

        // Attach the listener
        deckCardRef.addListenerForSingleValueEvent(valEventListener)
    }
}