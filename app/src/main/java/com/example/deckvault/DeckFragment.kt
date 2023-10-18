package com.example.deckvault

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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

        return rootView
    }

    private fun populateDecks() {
        val deckList = ArrayList<Deck>()
    }
}