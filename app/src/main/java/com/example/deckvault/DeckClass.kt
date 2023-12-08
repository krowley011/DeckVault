package com.example.deckvault

data class DeckClass (
        var deckImage: String = "",
        var deckName: String = "",
        var deckCardCount: Int = 0,
        var deckCardList: MutableList<CardClass> = mutableListOf()

)