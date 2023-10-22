package com.example.deckvault

import com.google.firebase.database.Exclude

data class UserClass(
    var userName : String? = null,
    var userEmail : String? = null,
    @get:Exclude
    var userID : String? = null
)
