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
        val deckRef = database.getReference("UserData/${currUser!!.uid}/Decks")


        // Retrieving the decks card count to display
        deckRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                for (deckSnapshot in dataSnapshot.children) {
                    val deck = deckSnapshot.getValue(DeckClass::class.java)
                    // Retrieve the ID of the matching deck
                    if (deck != null && deck.deckName == selectedDeck!!.deckName) {
                        val selectedDeckId = deckSnapshot.key
                        if (selectedDeckId != null) {
                            // Get the card count of the deck
                            val deckCardCountRef = deckRef.child(selectedDeckId).child("deckCardCount")
                            deckCardCountRef.addListenerForSingleValueEvent(object :
                                ValueEventListener {
                                override fun onDataChange(countSnapshot: DataSnapshot) {
                                    // Display the card count in the text view
                                    val cardCount = countSnapshot.getValue(Int::class.java) ?: 0
                                    deckCountTV.text = "Card Count: $cardCount"
                                }

                                override fun onCancelled(databaseError: DatabaseError) {
                                    Log.e(
                                        "SelectedDeckFragment", "Error fetching deck card count: $databaseError"
                                    )
                                }
                            })
                        }
                    }
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.e("SelectedDeckFragment", "Error fetching deck data: $databaseError")
            }
        })

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
        getSelectedDeckCards()

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
                                val transaction =
                                    requireActivity().supportFragmentManager.beginTransaction()
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

    private fun getSelectedDeckCards() {
        selectedDeckRecyclerList.clear() // Clear recycler view before populating

        val selectedDeck: DeckClass? = arguments?.getParcelable("Selected Deck")
        val database = FirebaseDatabase.getInstance()
        val deckRef = database.getReference("UserData/${currUser!!.uid}/Decks")

        // Retrieving the deck id for the selected deck
        deckRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                for (deckSnapshot in dataSnapshot.children) {
                    val deck = deckSnapshot.getValue(DeckClass::class.java)
                    if (deck != null && deck.deckName == selectedDeck!!.deckName) {
                        val selectedDeckId = deckSnapshot.key.toString()

                        // Retrieve the cards in the selected deck
                        val deckCardRef = database.getReference("UserData").child(currUser!!.uid)
                            .child("Decks").child(selectedDeckId).child("Cards")

                        deckCardRef.addListenerForSingleValueEvent(object : ValueEventListener {
                            override fun onDataChange(cardSnapshot: DataSnapshot) {
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
                                // Handle errors while fetching card data
                                Log.e("SelectedDeck", "Error fetching deck card data: $databaseError")
                            }
                        })
                        break
                    }
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Handle errors while fetching card data
                Log.e("SelectedDeck", "Error fetching deck card data: $databaseError")
            }
        })
    }

    override fun onCardClick(position: Int) {
        TODO("Not yet implemented")
    }
}