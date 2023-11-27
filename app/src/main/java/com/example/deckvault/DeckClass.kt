package com.example.deckvault

data class Deck (
    var deckCover: String,
    var deckName: String,
    var deckCountCard: Int,
    val id: Int? = deckList.size
)

var deckList = mutableListOf<Deck>()