package com.example.deckvault

import android.app.Dialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
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
                        card.cardSubName ?: ""
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
        val selectedCard = addCardRecyclerList[position]

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
        cardName.text = selectedCard.cardName
        cardSubName.text = selectedCard.cardSubName
        Picasso.get().load(selectedCard.cardImage).into(cardImage)

        // Show the dialog
        dialog.show()
    }

}
