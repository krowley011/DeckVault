package com.example.deckvault

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso

class SelectedDeckAdapter (
    private var cardRecyclerList: List<SelectedDeckRecyclerData>,
    private val cardClickListener: OnCardClickListener
) : RecyclerView.Adapter<SelectedDeckAdapter.ItemViewHolder>() {

    interface OnCardClickListener {
        fun onCardClick(position: Int)
    }

    inner class ItemViewHolder(view: View) : RecyclerView.ViewHolder(view), View.OnClickListener {
        var cardImage: ImageView = view.findViewById(R.id.selectedDeckCardImageView)
        var cardName: TextView = view.findViewById(R.id.selectedDeckCardNameTV)
        var cardSubname: TextView = view.findViewById(R.id.selectedDeckCardSubName)

        init {
            view.setOnClickListener(this)
        }

        override fun onClick(view: View?) {
            val position = adapterPosition
            if(position != RecyclerView.NO_POSITION) {
                Log.d("SelectedDeckAdapter", "Clicked on item at position: $position")
                cardClickListener.onCardClick(position)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val inflatedView: View = LayoutInflater.from(parent.context)
            .inflate(R.layout.selecteddeck_cardview, parent, false)
        return ItemViewHolder(inflatedView)
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        val card: SelectedDeckRecyclerData = cardRecyclerList[position]

        holder.cardName.text = card.cardName
        holder.cardSubname.text = card.cardSubName

        // Attempt to load card image from Firebase Storage
        val imageUrl = card.cardImage

        if (!imageUrl.isNullOrEmpty()) {
            Picasso.get().load(imageUrl).into(holder.cardImage)
        } else {
            // Handle null or empty URL case
            Log.e("SelectedDeckAdapter", "Image URL is null or empty")
            // Show a placeholder image or handle the absence of an image in your UI
            Picasso.get().load(R.drawable.backofcard).into(holder.cardImage)
        }
    }

    override fun getItemCount(): Int {
        return cardRecyclerList.size
    }

    fun updateData(newCardRecyclerList: List<SelectedDeckRecyclerData>) {
        cardRecyclerList = newCardRecyclerList
        notifyDataSetChanged()
    }

}