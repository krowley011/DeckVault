package com.example.deckvault

data class CardClass (
    var cardName : String? = null,
    var cardImage : String? = null,
    val cardNumber : Int? = null,
    var cardSubName: String? = null,
    var cardColor: String? = null,
    var cardClasses: MutableList<String>,
    val cardDamage: Int? = null,
    val cardDefense: Int? = null,
    var cardAction: String? = null,
    val cardInk: Int? = null,
    val cardInkable: Boolean,
    val cardLore: Int? = null,
    var cardDescription: String? = null
)