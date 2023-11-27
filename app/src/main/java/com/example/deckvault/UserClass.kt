package com.example.deckvault

import com.google.firebase.database.Exclude

data class UserClass(
    var userName : String? = null,
    var userEmail : String? = null,
    val cardCount : Int = 0,
    val deckCount : Int = 0,
    @get:Exclude
    var userID : String = ""
)
