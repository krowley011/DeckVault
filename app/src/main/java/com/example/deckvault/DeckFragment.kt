package com.example.deckvault

import android.app.Dialog
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.PopupMenu
import android.widget.TextView
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener


class DeckFragment : Fragment() {
    private lateinit var deckListRecycler: RecyclerView
    private lateinit var deckAdapter: DeckAdapter
    private var auth = FirebaseAuth.getInstance()
    private var currUser = auth.currentUser

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val rootView = inflater.inflate(R.layout.fragment_deck, container, false)
        val deckCountTV: TextView = rootView.findViewById(R.id.deckCountTV)

        val database = FirebaseDatabase.getInstance()
        val deckCountRef = database.getReference("UserData/${currUser!!.uid}/deckCount")

        deckCountRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val deckCount = dataSnapshot.getValue(Int::class.java) ?: 0
                deckCountTV.text = "You have $deckCount decks"
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Handle cancellation or errors in fetching deck count
                Log.e("DeckFragment", "Error fetching deck count: $databaseError")
            }
        })

        return rootView
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        deckListRecycler = view.findViewById(R.id.decksRecylView)        // Grid layout to display two items per row
        val layoutManager = GridLayoutManager(requireContext(), 2)
        deckListRecycler.layoutManager = layoutManager


        val deckRecyclerList = ArrayList<DeckRecyclerData>()
        deckAdapter = DeckAdapter(deckRecyclerList)
        deckListRecycler.adapter = deckAdapter

        setHasOptionsMenu(true)
        setupDeckMenu(view)

        // Populate recycler view
        getDeckCount()
    }

    private fun setupDeckMenu(rootView: View) {
        val button =
            rootView.findViewById<ImageButton>(R.id.deckMenuBTN) // Replace with your button ID
        button.setOnClickListener {
            val popup = PopupMenu(requireContext(), button)
            popup.menuInflater.inflate(R.menu.deckmenu, popup.menu)
            popup.setOnMenuItemClickListener { menuItem ->
                // Handle menu item click events here
                when (menuItem.itemId) {
                    R.id.DeckMenu_AddDeck -> {
                        showAddDeckDialog()
                    }
                }
                true
            }
            popup.show()
        }
    }

    private fun showAddDeckDialog() {
        val dialog = Dialog(requireContext())
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(true)
        dialog.setContentView(R.layout.adddeck_dialog)
        dialog.window?.setBackgroundDrawableResource(R.drawable.rounded_dialog)

        val addDeckButton = dialog.findViewById<Button>(R.id.adddeckBTN)
        val deckNameInput = dialog.findViewById<EditText>(R.id.deckNameET)

        // Example of Firebase initialization in an activity or fragment
        val database = FirebaseDatabase.getInstance()
        val user = FirebaseAuth.getInstance().currentUser

        val deckRepo = user?.let { DeckRepository(database, it) }

        addDeckButton.setOnClickListener {
            val deckName = deckNameInput.text.toString()
            if (deckName.isNotEmpty()) {
                // Create a new deck with the entered name
                val newDeck = DeckClass(deckImage = "", deckName = deckName, deckCardCount = 0)

                deckRepo!!.addDeck("gs://deckvault-a0189.appspot.com/backofcard.png", deckName, 0)

            }

            dialog.dismiss()
        }

        dialog.show()
    }


    private fun getDeckCount() {
        val database = FirebaseDatabase.getInstance()
        val deckCountRef = database.getReference("UserData/${currUser!!.uid}/deckCount")

        deckCountRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val deckCount = dataSnapshot.getValue(Int::class.java) ?: 0

                if (deckCount > 0) {
                    getDecks(deckCount)
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Handle cancellation or errors in fetching deck count
            }
        })
    }

    private fun getDecks(deckCount: Int) {
        val database = FirebaseDatabase.getInstance()
        val deckListRef = database.getReference("UserData/${currUser!!.uid}/Decks")

        deckListRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val deckRecyclerList = ArrayList<DeckRecyclerData>()

                for (deckSnapshot in dataSnapshot.children) {
                    val deckImage = deckSnapshot.child("deckImage").getValue(String::class.java)
                    val deckName = deckSnapshot.child("deckName").getValue(String::class.java)
                    val deckCardCount =
                        deckSnapshot.child("deckCardCount").getValue(Int::class.java)

                    // Create DeckRecyclerData and add to the list
                    if (deckImage != null && deckName != null && deckCardCount != null) {
                        val deckData = DeckRecyclerData(deckImage, deckName, deckCardCount)
                        deckRecyclerList.add(deckData)
                    }
                }

                // Update existing adapter's data
                deckAdapter.updateData(deckRecyclerList)
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Handle cancellation or errors in fetching deck list
            }
        })
    }
}