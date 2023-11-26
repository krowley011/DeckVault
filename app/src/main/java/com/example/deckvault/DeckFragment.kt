package com.example.deckvault

import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.Button
import android.widget.ImageButton
import android.widget.PopupMenu
import androidx.compose.ui.graphics.Color
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.deckvault.databinding.ActivityMainBinding


class DeckFragment : Fragment() {
    private lateinit var deckListRecycler: RecyclerView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val rootView = inflater.inflate(R.layout.fragment_deck, container, false)
        deckListRecycler = rootView.findViewById(R.id.decksRecylView)
        populateDecks()
        setHasOptionsMenu(true)
        setupDeckMenu(rootView)
        return rootView
    }

    private fun setupDeckMenu(rootView: View) {
        val button = rootView.findViewById<ImageButton>(R.id.deckMenuBTN) // Replace with your button ID
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

        dialog.show()
    }

    private fun populateDecks() {
        val deckList = ArrayList<Deck>()
        // Populate your deck list
    }
}
