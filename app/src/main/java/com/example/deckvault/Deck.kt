package com.example.deckvault

var deckList = mutableListOf<Deck>()
data class Deck (
    var deckCover: String,
    var deckName: String,
    var deckCountCard: Int,
    val id: Int? = deckList.size
)