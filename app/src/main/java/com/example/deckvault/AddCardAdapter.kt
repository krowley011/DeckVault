package com.example.deckvault

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso

class AddCardAdapter (
    private var addCardRecyclerList: List<AddCardRecyclerData>,
    private val cardClickListener: OnCardClickListener
    ) : RecyclerView.Adapter<AddCardAdapter.ItemViewHolder>() {

        interface OnCardClickListener {
            fun onCardClick(position: Int)
        }

        inner class ItemViewHolder(view: View) : RecyclerView.ViewHolder(view), View.OnClickListener {
            var cardImage: ImageView = view.findViewById(R.id.addCardImageView)
            var cardName: TextView = view.findViewById(R.id.addCardNameTV)
            var cardSubname: TextView = view.findViewById(R.id.addCardSubName)

            init {
                view.setOnClickListener(this)
            }

            override fun onClick(view: View?) {
                val position = adapterPosition
                if(position != RecyclerView.NO_POSITION) {
                    Log.d("AddCardAdapter", "Clicked on item at position: $position")
                    cardClickListener.onCardClick(position)
                }
            }
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
            val inflatedView: View = LayoutInflater.from(parent.context)
                .inflate(R.layout.card_cardview, parent, false)
            return ItemViewHolder(inflatedView)
        }

        override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
            val card: AddCardRecyclerData = addCardRecyclerList[position]

            holder.cardName.text = card.cardName
            holder.cardSubname.text = card.cardSubName

            // Attempt to load card image from Firebase Storage
            val imageUrl = card.cardImage

            if (!imageUrl.isNullOrEmpty()) {
                Picasso.get().load(imageUrl).into(holder.cardImage)
            } else {
                // Handle null or empty URL case
                Log.e("AddCardAdapter", "Image URL is null or empty")
                // Show a placeholder image or handle the absence of an image in your UI
                Picasso.get().load(R.drawable.backofcard).into(holder.cardImage)
            }
        }

        override fun getItemCount(): Int {
            return addCardRecyclerList.size
        }

        fun updateData(newAddCardRecyclerList: List<AddCardRecyclerData>) {
            addCardRecyclerList = newAddCardRecyclerList
            notifyDataSetChanged()
        }

}