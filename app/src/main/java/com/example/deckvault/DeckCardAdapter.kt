package com.example.deckvault

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.deckvault.databinding.DecksCardviewBinding

class DeckCardAdapter(private val deckList: List<Deck>, private val clickListener: DeckClickListener) :
    RecyclerView.Adapter<DeckCardViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DeckCardViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val decksCardBinding = DecksCardviewBinding.inflate(inflater, parent, false)
        return DeckCardViewHolder(decksCardBinding, clickListener)
    }

    override fun onBindViewHolder(holder: DeckCardViewHolder, position: Int) {
        val deck = deckList[position]
        holder.bindDeck(deck)
    }

    override fun getItemCount(): Int {
        return deckList.size
    }
}
