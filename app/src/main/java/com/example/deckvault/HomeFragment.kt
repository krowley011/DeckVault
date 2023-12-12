package com.example.deckvault

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class HomeFragment : Fragment(), RecentCardAdapter.OnCardClickListener {

    private var auth = FirebaseAuth.getInstance()
    private var currUser = auth.currentUser
    private var recentCardRecyclerList = ArrayList<RecentCardRecyclerData>()
    private lateinit var recentCardRepo: RecentRepository
    private lateinit var recentCardAdapter: RecentCardAdapter
    private lateinit var recentCardListRecycler: RecyclerView

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val welcomeUserTextView: TextView = view.findViewById(R.id.wel_user)
        val hpOverviewTV: TextView = view.findViewById(R.id.hpOverviewTV)


        if (currUser != null) {
            // Update welcome message
            val displayName = currUser!!.displayName ?: "User"
            val firstName = displayName.split(" ").firstOrNull() ?: "User"
            welcomeUserTextView.text = "Welcome $firstName!"

            // Retrieve user-related information from Firebase Database
            val userId = currUser!!.uid
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

            // Attach to recycler view
            recentCardListRecycler = view.findViewById(R.id.recentCardRecyclView)
            // Grid layout to display two items per row
            val layoutManager = GridLayoutManager(requireContext(), 2)
            recentCardListRecycler.layoutManager = layoutManager

            recentCardAdapter = RecentCardAdapter(recentCardRecyclerList, this)
            recentCardListRecycler.adapter = recentCardAdapter
            recentCardListRecycler.setHasFixedSize(true)

            recentCardRepo = RecentRepository(database, currUser!!)

            fetchRecentCards()

        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    private fun fetchRecentCards() {
        recentCardRecyclerList.clear() // Clear recycler view before populating

        recentCardRepo.isRecentDataReady.observe(viewLifecycleOwner, Observer { isRecentDataReady ->
            if (isRecentDataReady) {
                val recentCardCount = recentCardRepo.recentCardFeed.size

                if (recentCardCount > 0) {
                    for (recentCards in recentCardRepo.recentCardFeed) {
                        recentCardRecyclerList.add(
                            RecentCardRecyclerData(
                                recentCards.cardImage,
                                recentCards.cardName,
                                recentCards.cardSubName
                            )
                        )
                    }
                    recentCardAdapter.notifyDataSetChanged()
                }
            }
            recentCardRepo.stopRecentListener()
        })
    }

    override fun onCardClick(position: Int) {
            val clickedCard = recentCardRecyclerList[position]
            fetchSelectedCardDetails(clickedCard)
    }

    private fun fetchSelectedCardDetails(clickedCard: RecentCardRecyclerData) {
        val database = FirebaseDatabase.getInstance()
        val recentCardRef = database.getReference("UserData").child(currUser!!.uid).child("Recent Cards")

        recentCardRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                var selectedCard: CardClass? = null

                for (cardSnapshot in dataSnapshot.children) {
                    val cardNumber = cardSnapshot.child("Card Number").value as? Int ?: 0
                    if (cardNumber == clickedCard.cardNumber) {
                        selectedCard = cardSnapshot.getValue(CardClass::class.java)
                        break
                    }
                }

                selectedCard?.let { card ->
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
                Log.e("FetchCardDetails", "Error: ${databaseError.message}")
            }
        })
    }


}

