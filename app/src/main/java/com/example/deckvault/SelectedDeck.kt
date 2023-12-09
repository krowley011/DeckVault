package com.example.deckvault

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.PopupMenu
import androidx.fragment.app.Fragment

class SelectedDeck : Fragment() {
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
                        // Allow user to select multiple decks
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
}