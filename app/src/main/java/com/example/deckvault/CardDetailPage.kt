package com.example.deckvault

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.squareup.picasso.Picasso

class CardDetailPage : Fragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    @SuppressLint("SetTextI18n")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val rootView = inflater.inflate(R.layout.fragment_card_detail_page, container, false)

        // Retrieve bundled data
        val bundle = arguments
        val selectedCard = bundle?.getParcelable<CardClass>("selectedCard")

        // Assign values from bundle
        selectedCard?.let {
            val cardImage = it.cardImage
            val cardName = it.cardName
            val cardNumber = it.cardNumber
            var cardSubName = it.cardSubName
            var cardColor = it.cardColor
            var cardClasses = it.cardClasses
            val cardDamage = it.cardDamage
            val cardDefense = it.cardDefense
            var cardAction = it.cardAction
            val cardInk = it.cardInk
            val cardInkable = it.cardInkable
            val cardLore = it.cardLore
            var cardDescription = it.cardDescription
        }

        // Assign values to views
        val cardNameTopTV = rootView.findViewById<TextView>(R.id.cardPage_NameTopTV)
        cardNameTopTV.text = selectedCard!!.cardName
        val cardNameTV = rootView.findViewById<TextView>(R.id.cardPage_NameTV)
        cardNameTV.text = selectedCard.cardName
        val cardImageIV = rootView.findViewById<ImageView>(R.id.cardPage_Image)

        // Load image using Picasso from Firebase Storage URL into the ImageView
        val imageUrl = selectedCard.cardImage

        if (!imageUrl.isNullOrEmpty()) {
            Picasso.get().load(imageUrl).into(cardImageIV)
        } else {
            // Handle null or empty URL case
            Log.e("CardDetailPage", "Image URL is null or empty")
            // Show a placeholder image or handle the absence of an image in your UI
            Picasso.get().load(R.drawable.backofcard).into(cardImageIV)
        }

        // Change constraint layout and toolbar color based on card color
        val cardPageConstraintLayout = rootView.findViewById<ConstraintLayout>(R.id.cardPage_contraintLayout)
        val cardPageToolbar = rootView.findViewById<androidx.appcompat.widget.Toolbar>(R.id.cardPage_toolbar)

        // Map card colors to their respective color resources
        val colorRes = when (selectedCard.cardColor) { // Replace this with the actual color value
            "Steel" -> R.color.steel
            "Amethyst" -> R.color.amethyst
            "Sapphire" -> R.color.sapphire
            "Ruby" -> R.color.ruby
            "Emerald" -> R.color.emerald
            else -> R.color.blue // Use default color if the selected card color is not recognized
        }

        // Get the color from resources
        val color = ContextCompat.getColor(requireContext(), colorRes)

        // Set the color to the Toolbar's background
        cardPageConstraintLayout.setBackgroundColor(color)
        cardPageToolbar.setBackgroundColor(color)


        val cardSubNameTV = rootView.findViewById<TextView>(R.id.cardPage_SubnameTV)
        cardSubNameTV.text = selectedCard.cardSubName

        val classList = selectedCard.cardClasses.toMutableList()
        val cardClassesTV = rootView.findViewById<TextView>(R.id.cardPage_ClassesTV)
        cardClassesTV.text = classList.joinToString { " · " }

        val cardDamageTV = rootView.findViewById<TextView>(R.id.cardPage_DamageTV)
        cardDamageTV.text = "Damage - " + selectedCard.cardDamage.toString()
        val cardDefenseTV =  rootView.findViewById<TextView>(R.id.cardPage_DefenseTV)
        cardDefenseTV.text = "Defense - " + selectedCard.cardDefense.toString()
        val cardActionTV = rootView.findViewById<TextView>(R.id.cardPage_ActionTV)
        cardActionTV.text = selectedCard.cardAction

        // Change text based on if inkable or not
        val cardInkableTV = rootView.findViewById<TextView>(R.id.cardPage_InkableTV)
        if (selectedCard.cardInkable) {
            cardInkableTV.text = "Inkable - " + selectedCard.cardInk.toString()+ " Ink Cost"
        } else {
            cardInkableTV.text = "Uninkable - " + selectedCard.cardInk.toString() + " Ink Cost"
        }

        val cardLoreTV = rootView.findViewById<TextView>(R.id.cardPage_LoreTV)
        cardLoreTV.text = selectedCard.cardLore.toString() + " Lore"
        val cardDescriptionTV = rootView.findViewById<TextView>(R.id.cardPage_DescriptionTV)
        cardDescriptionTV.text = selectedCard.cardDescription

        return rootView
    }

}