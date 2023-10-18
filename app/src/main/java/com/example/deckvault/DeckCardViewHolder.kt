package com.example.deckvault

import androidx.recyclerview.widget.RecyclerView
import com.example.deckvault.databinding.DecksCardviewBinding


class DeckCardViewHolder(
    private val decksCardBinding: DecksCardviewBinding, // Assuming you have a DeckCardBinding class
    private val clickListener: DeckClickListener
) : RecyclerView.ViewHolder(decksCardBinding.root) {
    fun bindDeck(deck: Deck) {
        decksCardBinding.cardImageView.setImageResource(deck.deckCover.toInt())
        decksCardBinding.deckNameTV.text = deck.deckName
        decksCardBinding.cardCountTV.text = deck.deckCountCard.toString()

        decksCardBinding.root.setOnClickListener {
            clickListener.onClick(deck)
        }
    }
}
