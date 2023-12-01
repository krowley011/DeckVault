package com.example.deckvault

import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.Exclude

data class UserClass(
    var userName: String? = null,
    var userEmail: String? = null,
    var cardCount: Int = 0,
    var deckCount: Int = 0,
    @get:Exclude
    var userID: String = "",
    var decks: MutableList<DeckClass> = mutableListOf(),
)
