
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.deckvault.DeckRecyclerData
import com.example.deckvault.R
import com.google.firebase.storage.FirebaseStorage
import com.squareup.picasso.Picasso

class DeckAdapter(
    private var deckRecyclerList: List<DeckRecyclerData>,
    private val deckClickListener: OnDeckClickListener
) : RecyclerView.Adapter<DeckAdapter.ItemViewHolder>() {

    interface OnDeckClickListener {
        fun onDeckClick(position: Int)
    }

    inner class ItemViewHolder(view: View) : RecyclerView.ViewHolder(view), View.OnClickListener {
        var deckImage: ImageView = view.findViewById(R.id.deckImageView)
        var deckName: TextView = view.findViewById(R.id.deckNameTV)
        var deckCount: TextView = view.findViewById(R.id.deckCardCountTV)

        init {
            view.setOnClickListener(this)
        }

        override fun onClick(p0: View?) {
            val position = adapterPosition
            if(position != RecyclerView.NO_POSITION) {
                Log.d("DeckAdapter", "Clicked on item at position: $position")
                deckClickListener.onDeckClick(position)
            }
        }

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

        // Load image using Picasso from Firebase Storage URL into the ImageView
        val storageReference = FirebaseStorage.getInstance().getReferenceFromUrl(deck.deckImage)

        storageReference.downloadUrl.addOnSuccessListener { uri ->
            Picasso.get().load(uri).into(holder.deckImage)
        }.addOnFailureListener {
            // Handle any errors while fetching the download URL
        }

        holder.itemView.setOnClickListener {
            // Call the interface method to handle click events
            deckClickListener.onDeckClick(position)
        }
    }

    override fun getItemCount(): Int {
        return deckRecyclerList.size
    }

    fun updateData(newDeckRecyclerList: List<DeckRecyclerData>) {
        deckRecyclerList = newDeckRecyclerList
        notifyDataSetChanged()
    }
}
