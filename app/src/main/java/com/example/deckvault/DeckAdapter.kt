import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.deckvault.DeckClickListener
import com.example.deckvault.DeckRecyclerData
import com.example.deckvault.R
import com.google.firebase.storage.FirebaseStorage
import com.squareup.picasso.Picasso

class DeckAdapter(
    private var deckRecyclerList: List<DeckRecyclerData>,
    private val itemClickListener: DeckClickListener
) : RecyclerView.Adapter<DeckAdapter.ItemViewHolder>() {

    inner class ItemViewHolder(view: View) : RecyclerView.ViewHolder(view), View.OnClickListener {
        var deckImage: ImageView = view.findViewById(R.id.deckImageView)
        var deckName: TextView = view.findViewById(R.id.deckNameTV)
        var deckCount: TextView = view.findViewById(R.id.deckCardCountTV)

        init {
            itemView.setOnClickListener(this)
        }

        // Need to fix onClick event
        override fun onClick(view: View?) {
            val position = adapterPosition
            if (position != RecyclerView.NO_POSITION) {
                val deck = deckRecyclerList[position]
                itemClickListener.onClick(deck)
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
    }
    override fun getItemCount(): Int {
        return deckRecyclerList.size
    }

    fun updateData(newDeckRecyclerList: List<DeckRecyclerData>) {
        deckRecyclerList = newDeckRecyclerList
        notifyDataSetChanged()
    }
}
