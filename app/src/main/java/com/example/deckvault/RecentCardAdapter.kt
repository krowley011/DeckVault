package com.example.deckvault

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso

class RecentCardAdapter(
    private var recentCardRecyclerList: List<RecentCardRecyclerData>,
    private val recentCardClickListener: OnCardClickListener
) : RecyclerView.Adapter<RecentCardAdapter.ItemViewHolder>() {

    interface OnCardClickListener {
        fun onCardClick(position: Int)
    }

    inner class ItemViewHolder(view: View) : RecyclerView.ViewHolder(view), View.OnClickListener {
        var cardImage: ImageView = view.findViewById(R.id.recentCardImageView)
        var cardName: TextView = view.findViewById(R.id.recentCardNameTV)
        var cardSubname: TextView = view.findViewById(R.id.recentCardSubName)

        init {
            view.setOnClickListener(this)
        }

        override fun onClick(view: View?) {
            val position = adapterPosition
            if (position != RecyclerView.NO_POSITION) {
                Log.d("SelectedDeckAdapter", "Clicked on item at position: $position")
                recentCardClickListener.onCardClick(position)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val inflatedView: View = LayoutInflater.from(parent.context)
            .inflate(R.layout.recentcard_cardview, parent, false)
        return ItemViewHolder(inflatedView)
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        val card: RecentCardRecyclerData = recentCardRecyclerList[position]

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
        return recentCardRecyclerList.size
    }

    fun updateData(newCardRecyclerList: List<RecentCardRecyclerData>) {
        recentCardRecyclerList = newCardRecyclerList
        notifyDataSetChanged()
    }

}
