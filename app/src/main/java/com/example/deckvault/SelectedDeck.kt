package com.example.deckvault

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.PopupMenu
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class SelectedDeck : Fragment() {

    private var auth = FirebaseAuth.getInstance()
    private var currUser = auth.currentUser

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_selected_deck, container, false)
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
}