package com.example.deckvault

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso

class DeckAdapter(
    private var deckRecyclerList: List<DeckRecyclerData>
) : RecyclerView.Adapter<DeckAdapter.ItemViewHolder>() {
    inner class ItemViewHolder(view: View): RecyclerView.ViewHolder(view) {
        var deckImage: ImageView = view.findViewById(R.id.deckImageView)
        var deckName: TextView = view.findViewById(R.id.deckNameTV)
        var deckCount: TextView = view.findViewById(R.id.deckCardCountTV)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val inflatedView: View = LayoutInflater.from(parent.context)
            .inflate(R.layout.decks_cardview, parent, false)
        return ItemViewHolder(inflatedView)
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        val deck: DeckRecyclerData = deckRecyclerList[position]

        holder.deckName.text = deck.deckName
        holder.deckCount.text = deck.deckCardCount.toString()
        Picasso.get().load(deck.deckImage).into(holder.deckImage)
    }
    override fun getItemCount(): Int {
        return deckRecyclerList.size
    }

    fun updateData(newDeckRecyclerList: List<DeckRecyclerData>) {
        deckRecyclerList = newDeckRecyclerList
        notifyDataSetChanged()
    }
}