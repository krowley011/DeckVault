package com.example.deckvault

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.PopupMenu


class DeckPageWithCards : Fragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_deck_page_with_cards, container, false)
    }

    private fun setupDeckPageMenu(rootView: View) {
        val button =
            rootView.findViewById<ImageButton>(R.id.selectedDeckMenuBTN)
        button.setOnClickListener {
            val popup = PopupMenu(requireContext(), button)
            popup.menuInflater.inflate(R.menu.cardmenu, popup.menu)
            popup.setOnMenuItemClickListener { menuItem ->
                // Handle menu item click events here
                when (menuItem.itemId) {
                    R.id.CardMenu_Copy -> {
                    }
                }
                true
            }
            popup.show()
        }
    }
}