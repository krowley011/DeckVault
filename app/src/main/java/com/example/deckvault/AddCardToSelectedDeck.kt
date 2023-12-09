package com.example.deckvault

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase


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

        val selectedDeck: DeckClass? = arguments?.getParcelable("Selected Deck")

        if (selectedDeck != null) {
            val deckName = selectedDeck.deckName
            val deckImage = selectedDeck.deckImage
            val deckCardCount = selectedDeck.deckCardCount
            val deckCardList = selectedDeck.deckCardList
        } else {
            Log.e("AddCardToSelectedDeckFragment", "Selected Deck is null.")
        }

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

        val searchView = view.findViewById<SearchView>(R.id.cardSearchView)

        // Set an OnQueryTextListener to handle search actions
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                // Perform actions when search is submitted (e.g., perform search)
                if (!query.isNullOrEmpty()) {
                    performSearch(query)
                }
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                // Perform actions while typing in the search bar
                // Update search results dynamically as the text changes
                if (!newText.isNullOrEmpty()) {
                    updateSearchResults(newText)
                } else {
                    // Handle case when search query is cleared
                    // For example, show default list or clear search results
                    // clearSearchResults()
                }
                return true
            }
        })
    }

    // Function to perform search based on the query submitted
    private fun performSearch(query: String) {
        val matchingCards = cardRepo.Cards.filter { card ->
            card.cardName?.contains(query, ignoreCase = true) == true ||
                    card.cardSubName?.contains(query, ignoreCase = true) == true
        }
    }
    // Function to update search results dynamically as the text changes
    private fun updateSearchResults(newText: String) {
        performSearch(newText)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_add_card_to_selected_deck, container, false)
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
                            card.cardSubName
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
        TODO("Not yet implemented")
    }


}