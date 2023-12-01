package com.example.deckvault

import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.Toast
import androidx.core.content.ContextCompat.startActivity
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class UserDataRepository(private val database: FirebaseDatabase, private val userId: String) {
    private val _userData = MutableLiveData<UserClass>()
    val userData: LiveData<UserClass>
        get() = _userData

    private var listener: ValueEventListener? = null

    init {
        fetchUserData()
    }

    private fun fetchUserData() {
        // Firebase Path References
        val userDataRef = database.getReference("UserData").child(userId).child("User")
        listener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val userName = dataSnapshot.child("Username").getValue(String::class.java) ?: ""
                val userEmail = dataSnapshot.child("Email").getValue(String::class.java) ?: ""
                val userId = dataSnapshot.child("UserId").getValue(String::class.java) ?: ""
                val cardCount =
                    dataSnapshot.child("Card Count").getValue(Long::class.java)?.toInt() ?: 0
                val deckCount =
                    dataSnapshot.child("Deck Count").getValue(Long::class.java)?.toInt() ?: 0

                val user = UserClass(userName, userEmail, cardCount, deckCount, userId)
                _userData.postValue(user) // Update the LiveData object with the new data
            }

            override fun onCancelled(error: DatabaseError) {}
        }
        userDataRef.addListenerForSingleValueEvent(listener!!)
    }

    fun updateUserData(
        userName: String?,
        userEmail: String?,
        cardCount: Int,
        deckCount: Int,
        userId: String
    ) {
        val userDataRef = database.getReference("UserData").child(userId).child("User")
        val userData = HashMap<String, Any>()
        userData["Username"] = userName ?: ""
        userData["Email"] = userEmail ?: ""
        userData["UserId"] = userId
        userData["Card Count"] = cardCount
        userData["Deck Count"] = deckCount

        userDataRef.updateChildren(userData)
    }

    fun addUser(user: UserClass) {
       val userDataRef =
            database.getReference("UserData").child(user.userID.toString()).child("User")
        userDataRef.child("Name").setValue(user.userName)
        userDataRef.child("Email").setValue(user.userEmail)
        userDataRef.child("Card Count").setValue(user.cardCount)
       userDataRef.child("Deck Count").setValue(user.deckCount)
        userDataRef.child("UserId").setValue(user.userID)
    }

    //To be used if allowing user to delete account
//    fun removeUser(username: String)
//    {
//        val curUser = FirebaseAuth.getInstance().currentUser // get the current user
//        if (curUser != null && userId == curUser.uid)
//        {
//            // if the user isn't null
//
//            curUser.delete() // delete them from firebase
//            val database = FirebaseDatabase.getInstance()
//            val userRef = database.reference.child("UserData").child(curUser.uid) // get the path to their user data location in the database
//            val usernameRef = database.reference.child("ServerData").child("Usernames").child(username) // get the path to their username in the taken username list
//
//            usernameRef.removeValue() // clear their information from the database
//            userRef.removeValue() // clear the username from the taken username list
//
//        }
//    }

    fun stopProfileListener() {
        listener?.let {
            val userDataRef = database.getReference("UserData").child(userId)
                .child("User")
            userDataRef.removeEventListener(it)
            listener = null
        }
    }
}

    class CardRepository(private val database: FirebaseDatabase, private val user: UserClass)
    {
        public var Decks = mutableListOf<CardClass>()
        private val _isCardDataReady = MutableLiveData<Boolean>()
        val isCardDataReady: LiveData<Boolean>
            get() = _isCardDataReady

        private var listener: ValueEventListener? = null

        init
        {
            fetchCardData()
        }

        fun removeCard(card: CardClass, owner: LifecycleOwner) {
            val database = FirebaseDatabase.getInstance()
            val cardRef = database.reference.child("UserData").child(user.userID).child("Cards").child(card.cardNumber.toString())
            cardRef.removeValue()

            // update user's card count
            val userDataRepo = UserDataRepository(database, user.userID)
            userDataRepo.userData.observe(owner) { user ->
                var cardCount = user.cardCount
                cardCount--
                userDataRepo.updateUserData(user.userName, user.userEmail, user.cardCount, user.deckCount, user.userID)
                userDataRepo.stopProfileListener()
            }
        }

        fun fetchCardData()
        {
            val profileDataRef = database.getReference("UserData").child(user.userID).child("Cards")
            Decks.clear()
            listener = object : ValueEventListener
            {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    _isCardDataReady.postValue(false) // inform the caller the list is not ready
                    for (cardSnapshot in dataSnapshot.children) {
                        var cardName = cardSnapshot.child("Card Name").value as? String ?: ""
                        var cardImage = cardSnapshot.child("Card Image").value as? String ?: ""
                        val cardNumber = (cardSnapshot.child("Card Number").value as? Long)?.toInt() ?: 0 // Cast to Int or provide a default value if null
                        var cardSubName = cardSnapshot.child("Card SubName").value as? String ?: ""
                        var cardColor = cardSnapshot.child("Card Color").value as? String ?: ""
                        var cardClasses = cardSnapshot.child("Card Classes").value as? MutableList<String> ?: mutableListOf()
                        val cardDamage = (cardSnapshot.child("Card Damage").value as? Long)?.toInt() ?: 0 // Cast to Int or provide a default value if null
                        val cardDefense = (cardSnapshot.child("Card Defense").value as? Long)?.toInt() ?: 0 // Cast to Int or provide a default value if null
                        var cardAction = cardSnapshot.child("Card Action").value as? String ?: ""
                        val cardInk = (cardSnapshot.child("Card Ink").value as? Long)?.toInt() ?: 0 // Cast to Int or provide a default value if null
                        val cardInkable = cardSnapshot.child("Card Inkable").value as? Boolean ?: false // Provide a default value if null
                        val cardLore = (cardSnapshot.child("Card Lore").value as? Long)?.toInt() ?: 0 // Cast to Int or provide a default value if null
                        var cardDescription = cardSnapshot.child("Card Description").value as? String ?: ""

                        var card = CardClass(
                            cardName,
                            cardImage,
                            cardNumber,
                            cardSubName,
                            cardColor,
                            cardClasses,
                            cardDamage,
                            cardDefense,
                            cardAction,
                            cardInk,
                            cardInkable,
                            cardLore,
                            cardDescription
                        )
                        Decks.add(card)
                    }
                    _isCardDataReady.postValue(true) // inform the caller we have filled the list with each book
                }

                override fun onCancelled(error: DatabaseError) { }
            }
            profileDataRef.addListenerForSingleValueEvent(listener!!)
        }

        fun updateCardData(card: CardClass)
        {
            val cardDataRef = database.getReference("UserData").child(user.userID).child("Cards").child(card.cardNumber.toString())

            // Will need to add functionality here if I allow the user to change the card data
        }

        fun addCard(card: CardClass, owner: LifecycleOwner)
        {
            val cardDataRef = database.getReference("UserData").child(user.userID).child("Cards")
            cardDataRef.child(card.cardNumber.toString()).child("Card Name").setValue(card.cardName)
            cardDataRef.child(card.cardNumber.toString()).child("Card Image").setValue(card.cardImage)
            cardDataRef.child(card.cardNumber.toString()).child("Card Number").setValue(card.cardNumber)
            cardDataRef.child(card.cardNumber.toString()).child("Card SubName").setValue(card.cardSubName)
            cardDataRef.child(card.cardNumber.toString()).child("Card Color").setValue(card.cardColor)
            cardDataRef.child(card.cardNumber.toString()).child("Card Classes").setValue(card.cardClasses)
            cardDataRef.child(card.cardNumber.toString()).child("Card Damage").setValue(card.cardDamage)
            cardDataRef.child(card.cardNumber.toString()).child("Card Defense").setValue(card.cardDefense)
            cardDataRef.child(card.cardNumber.toString()).child("Card Action").setValue(card.cardAction)
            cardDataRef.child(card.cardNumber.toString()).child("Card Ink").setValue(card.cardInk)
            cardDataRef.child(card.cardNumber.toString()).child("Card Inkable").setValue(card.cardInkable)
            cardDataRef.child(card.cardNumber.toString()).child("Card Lore").setValue(card.cardLore)
            cardDataRef.child(card.cardNumber.toString()).child("Card Description").setValue(card.cardDescription)

            // update user's book count
            val userDataRepo = UserDataRepository(database, user.userID)
            userDataRepo.userData.observe(owner) { userProfile ->
                var cardCount = user.cardCount
                cardCount++
                userDataRepo.updateUserData(user.userID, user.userEmail, user.cardCount, user.deckCount, user.userID)
                userDataRepo.stopProfileListener()
            }

        }

        //For adding favorite functionality to cards
//        fun updateFavoriteStatus(card: CardClass)
//       {
//            val userDataRef = database.getReference("UserData").child(user.userID).child("Cards")
//           userDataRef.child(card.cardNumber.toString()).child("IsFavorite").setValue(card.isFav)
//        }

        //Adding test cards to test Firebase Database
        //Will remove after later
        fun addTestCards(owner: LifecycleOwner) {
            val cardDataRef = database.getReference("UserData").child(user.userID).child("Cards")

            // Add test cards here
            val testCards = mutableListOf<CardClass>(
                createCard(
                    "Donald Duck",
                    "PlaceholderURL",
                    13,
                    "Musketeer",
                    "Steel",
                    "Dreamborn, Hero, Musketeer",
                    2,
                    5,
                    "Stay Alert! During your turn, your Musketeer characters " +
                            "gain Evasive. (They can challenge characters with Evasive.)",
                    4,
                    true,
                    1,
                    "Bodyguard (This character may enter play exerted. An opposing " +
                        "character who challenges one of your characters must choose one with " +
                            "Bodyguard if able.)" ),
                createCard(
                    "Merlin",
                    "PlaceholderURL",
                    52,
                    "Rabbit",
                    "Amethyst",
                    "Storyborn, Mentor, Sorceror",
                    2,
                    3,
                    "Hoppity Hip! When you play this character and when he leaves play, " +
                            "you may draw a card.",
                    4,
                    false,
                    1,
                    "It was turning out to be a bad hare day.")
            )

            for (card in testCards) {
                cardDataRef.child(card.cardNumber.toString()).setValue(card)
            }

            // Update user's card count after adding test cards
            val userDataRepo = UserDataRepository(database, user.userID)
            userDataRepo.userData.observe(owner) { userProfile ->
                var cardCount = user.cardCount + testCards.size
                userDataRepo.updateUserData(user.userID, user.userEmail, cardCount, user.deckCount, user.userID)
                userDataRepo.stopProfileListener()
            }
        }

        fun createCard(name: String, imageUrl: String, number: Int, subName: String, color: String,
            types: String, damage: Int, defense: Int, action: String, ink: Int, inkable: Boolean,
            lore: Int, description: String): CardClass {

            val cardTypes = types.split(", ").toMutableList()

            return CardClass(name, imageUrl, number, subName, color, cardTypes, damage, defense,
                action, ink, inkable, lore, description)
        }

        fun stopCardListener()
        {
            listener?.let {
                val userDataRef = database.getReference("UserData").child(user.userID)
                    .child("Cards")
                userDataRef.removeEventListener(it)
                listener = null
            }
        }

        fun clearUserDecks(owner: LifecycleOwner) {
            val currentUser = FirebaseAuth.getInstance().currentUser
            if (currentUser != null) {
                for (card in Decks) {
                    removeCard(card, owner)
                }
                // Signal that the user library has been cleared
                _isCardDataReady.postValue(true)
            } else {
                Log.e("CardRepository", "User ID is null. Cannot clear decks.")
                _isCardDataReady.postValue(false)
            }
        }

    }

class RecentRepository(private val database: FirebaseDatabase, private val username: String, private val friendList: MutableList<CardClass>)
{
    public var recentCardFeed = mutableListOf<CardClass>()
    private val _isRecentCardDataReady = MutableLiveData<Boolean>()
    val isRecentDataReady: LiveData<Boolean>
        get() = _isRecentCardDataReady

    private var listener: ValueEventListener? = null

    private val currentUser: FirebaseUser? = FirebaseAuth.getInstance().currentUser

    init
    {
        //fetchRecentData()
    }

    fun fetchRecentData()
    {
        // Firebase Path References
        val userDataRef = database.getReference("UserData").child(currentUser!!.uid).child("Recent Cards")
        recentCardFeed.clear()
        listener = object : ValueEventListener
        {
            override fun onDataChange(dataSnapshot: DataSnapshot)
            {
                _isRecentCardDataReady.postValue(false) // inform the caller the list is not ready
                for (cardSnapshot in dataSnapshot.children)
                {
                    var cardName = cardSnapshot.child("Card Name").value as? String ?: ""
                    var cardImage = cardSnapshot.child("Card Image").value as? String ?: ""
                    val cardNumber = (cardSnapshot.child("Card Number").value as? Long)?.toInt() ?: 0 // Cast to Int or provide a default value if null
                    var cardSubName = cardSnapshot.child("Card SubName").value as? String ?: ""
                    var cardColor = cardSnapshot.child("Card Color").value as? String ?: ""
                    var cardClasses = cardSnapshot.child("Card Classes").value as? MutableList<String> ?: mutableListOf()
                    val cardDamage = (cardSnapshot.child("Card Damage").value as? Long)?.toInt() ?: 0 // Cast to Int or provide a default value if null
                    val cardDefense = (cardSnapshot.child("Card Defense").value as? Long)?.toInt() ?: 0 // Cast to Int or provide a default value if null
                    var cardAction = cardSnapshot.child("Card Action").value as? String ?: ""
                    val cardInk = (cardSnapshot.child("Card Ink").value as? Long)?.toInt() ?: 0 // Cast to Int or provide a default value if null
                    val cardInkable = cardSnapshot.child("Card Inkable").value as? Boolean ?: false // Provide a default value if null
                    val cardLore = (cardSnapshot.child("Card Lore").value as? Long)?.toInt() ?: 0 // Cast to Int or provide a default value if null
                    var cardDescription = cardSnapshot.child("Card Description").value as? String ?: ""

                    var card: CardClass = CardClass(
                        cardName,
                        cardImage,
                        cardNumber,
                        cardSubName,
                        cardColor,
                        cardClasses,
                        cardDamage,
                        cardDefense,
                        cardAction,
                        cardInk,
                        cardInkable,
                        cardLore,
                        cardDescription
                    )

                    recentCardFeed.add(card)
                }
                _isRecentCardDataReady.postValue(true) // inform the caller we have filled the list with each recent book
            }
            override fun onCancelled(error: DatabaseError) { }
        }
        userDataRef.addListenerForSingleValueEvent(listener!!)
    }

    fun stopRecentListener()
    {
        listener?.let {
            val profileDataRef = database.getReference("UserData").child(currentUser!!.uid)
                .child("Recents")
            profileDataRef.removeEventListener(it)
            listener = null
        }
    }

}
