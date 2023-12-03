package com.example.deckvault

import androidx.recyclerview.widget.RecyclerView
import com.example.deckvault.databinding.DecksCardviewBinding


class DeckViewHolder(
    private val decksCardBinding: DecksCardviewBinding, // Assuming you have a DeckCardBinding class
    private val clickListener: DeckClickListener
) : RecyclerView.ViewHolder(decksCardBinding.root) {
    fun bindDeck(deck: DeckClass) {
        decksCardBinding.deckImageView.setImageResource(deck.deckImage.toInt())
        decksCardBinding.deckNameTV.text = deck.deckName
        decksCardBinding.deckCardCountTV.text = deck.deckCardCount.toString()

        decksCardBinding.root.setOnClickListener {
            clickListener.onClick(deck)
        }
    }
}
