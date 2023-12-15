package com.example.deckvault

import android.net.Uri
import android.util.Log
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import java.io.File


class UserDataRepository(private val database: FirebaseDatabase, private val userId: String) {
    private val _userData = MutableLiveData<UserClass>()
    val userData: LiveData<UserClass>
        get() = _userData

    private var listener: ValueEventListener? = null

    init {
        fetchUserData()
    }

    fun fetchUserData() {
        // Firebase Path References
        val deckList = mutableListOf<DeckClass>()
        val userDataRef = FirebaseDatabase.getInstance().getReference("UserData").child(userId).child("User")
        listener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val userName = dataSnapshot.child("Username").getValue(String::class.java) ?: ""
                val userEmail = dataSnapshot.child("Email").getValue(String::class.java) ?: ""
                val userId = dataSnapshot.child("UserId").getValue(String::class.java) ?: ""
                val cardCount = dataSnapshot.child("Card Count").getValue(Long::class.java)?.toInt() ?: 0
                val deckCount = dataSnapshot.child("Deck Count").getValue(Long::class.java)?.toInt() ?: 0
                val deckListSnapshot = dataSnapshot.child("Deck List")

                val deckList = mutableListOf<DeckClass>()

                for (deckSnapshot in deckListSnapshot.children) {
                    // Assuming each child is a DeckClass object
                    val deck = deckSnapshot.getValue(DeckClass::class.java)
                    deck?.let { deckList.add(it) }
                }

                val user = UserClass(userName, userEmail, cardCount, deckCount, userId, deckList)
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
        userId: String,
        deckList: MutableList<DeckClass>
    ) {
        val userDataRef = database.getReference("UserData").child(userId).child("User")
        val userData = HashMap<String, Any>()
        userData["Username"] = userName ?: ""
        userData["Email"] = userEmail ?: ""
        userData["UserId"] = userId
        userData["Card Count"] = cardCount
        userData["Deck Count"] = deckCount
        userData["Deck List"] = deckList

        userDataRef.updateChildren(userData)
    }

    fun addUser(user: UserClass) {
       val userDataRef = database.getReference("UserData").child(user.userID).child("User")
       userDataRef.child("Name").setValue(user.userName)
       userDataRef.child("Email").setValue(user.userEmail)
       userDataRef.child("Card Count").setValue(user.cardCount)
       userDataRef.child("Deck Count").setValue(user.deckCount)
       userDataRef.child("UserId").setValue(user.userID)
       userDataRef.child("Deck List").setValue(user.decks)
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

    fun stopUserListener() {
        listener?.let {
            val userDataRef = database.getReference("UserData").child(userId)
                .child("User")
            userDataRef.removeEventListener(it)
            listener = null
        }
    }
}

class CardRepository(private val database: FirebaseDatabase, private val user: FirebaseUser) {
        public var Cards = mutableListOf<CardClass>()
        private val deckId : String = ""
        private val _isCardDataReady = MutableLiveData<Boolean>()
        val isCardDataReady: LiveData<Boolean>
            get() = _isCardDataReady

        private var listener: ValueEventListener? = null

        init {
            fetchCardData(deckId)
        }

        fun removeCard(card: CardClass, deckId: String, owner: LifecycleOwner) {
            val database = FirebaseDatabase.getInstance()
            val cardRef = database.reference.child("UserData").child(user.uid).child("Decks").child(deckId).child("Cards")
                .child(card.cardNumber.toString())
            cardRef.removeValue()

            // update user's card count
            val userDataRepo = UserDataRepository(database, user.uid)
            userDataRepo.userData.observe(owner) { user ->
                var cardCount = user.cardCount
                cardCount--
                // Update card count
                val userDataRef = database.getReference("UserData").child("userID")
                userDataRef.child("Card Count").setValue(cardCount)
                userDataRepo.stopUserListener()
            }
        }

        fun fetchCardData(deckId: String) {
            val profileDataRef = database.getReference("UserData").child(user.uid).child("Decks").child(deckId).child("Cards")
            Cards.clear()
            listener = object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    _isCardDataReady.postValue(false) // inform the caller the list is not ready
                    for (cardSnapshot in dataSnapshot.children) {
                        var cardName = cardSnapshot.child("Card Name").value as? String ?: ""
                        var cardImage = cardSnapshot.child("Card Image").value as? String ?: ""
                        val cardNumber = (cardSnapshot.child("Card Number").value as? Long)?.toInt() ?: 0
                        var cardSubName = cardSnapshot.child("Card SubName").value as? String ?: ""
                        var cardColor = cardSnapshot.child("Card Color").value as? String ?: ""
                        var cardClasses = cardSnapshot.child("Card Classes").value as? MutableList<String> ?: mutableListOf()
                        val cardDamage = (cardSnapshot.child("Card Damage").value as? Long)?.toInt() ?: 0
                        val cardDefense = (cardSnapshot.child("Card Defense").value as? Long)?.toInt() ?: 0
                        var cardAction = cardSnapshot.child("Card Action").value as? String ?: ""
                        val cardInk = (cardSnapshot.child("Card Ink").value as? Long)?.toInt() ?: 0
                        val cardInkable = cardSnapshot.child("Card Inkable").value as? Boolean ?: false
                        val cardLore = (cardSnapshot.child("Card Lore").value as? Long)?.toInt() ?: 0
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
                        //Need to specify which deck to add card to
                        Cards.add(card)
                    }
                    _isCardDataReady.postValue(true) // inform the caller we have filled the list with each book
                }

                override fun onCancelled(error: DatabaseError) {}
            }
            profileDataRef.addListenerForSingleValueEvent(listener!!)
        }

//        fun updateCardData(card: CardClass)
//        {
//            val cardDataRef = database.getReference("UserData").child(user.uid).child("Cards").child(card.cardNumber.toString())
//
//            // Will need to add functionality here if I allow the user to change the card data
//        }


        fun addCard(card: CardClass, owner: LifecycleOwner) {
            val selectedDeckName: String = ""
            val cardDataRef = database.getReference("UserData").child(user.uid).child("Decks").child(selectedDeckName).child("Cards")

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

            // update user's card count
            val userDataRepo = UserDataRepository(database, user.uid)
            userDataRepo.userData.observe(owner) { userProfile ->
                userProfile?.let { user ->
                    var cardCount = user.cardCount ?: 0 // Fetch the current card count from the observed user profile
                    cardCount++ // Increment the card count

                    // Update the card count for the user
                    val userDataRef = database.getReference("UserData").child("userID")
                    userDataRef.child("Card Count").setValue(cardCount)
                    userDataRepo.stopUserListener()
                }
            }
        }


        //For adding favorite functionality to cards
//        fun updateFavoriteStatus(card: CardClass)
//       {
//            val userDataRef = database.getReference("UserData").child(user.userID).child("Cards")
//           userDataRef.child(card.cardNumber.toString()).child("IsFavorite").setValue(card.isFav)
//        }


        // Initialize all cards on user login
        fun initializeCards() {
            val cardDataRef = FirebaseDatabase.getInstance().getReference("CardData")
            var imageUrl = ""

            imageUrl = "gs://deckvault-a0189.appspot.com/35.png"
            addImage(imageUrl, { imageUrl ->
                // Image uploaded successfully, use the imageUrl to initialize CardClass or perform other operations
                val card35 = CardClass(
                    "Arthur",
                    imageUrl.toString(),
                    35,
                    "Wizard's Apprentice",
                    "Amethyst",
                    mutableListOf("Dreamborn", "Hero", "Sorcerer"),
                    1,
                    3,
                    "Student - Whenever this character quests, you may return another chosen character of yours" +
                        " to your hand to gain 2 lore.",
                    3,
                    false,
                    1,
                    "Hmm.. what spell should I try next?"
                )
                cardDataRef.child(card35.cardNumber.toString()).setValue(card35)
            }, { exception ->
                // Handle failure in image upload or URL retrieval
                Log.e("InitializeCards", "Image upload failed: ${exception.message}")
            })

            imageUrl = "gs://deckvault-a0189.appspot.com/49.png"
            addImage(imageUrl, { imageUrl ->
                // Image uploaded successfully, use the imageUrl to initialize CardClass or perform other operations
                val card49 = CardClass(
                    "Madam Mim",
                    imageUrl.toString(),
                    49,
                    "Snake",
                    "Amethyst",
                    mutableListOf("Storyborn", "Villain", "Sorcerer"),
                    3,
                    3,
                    "Just you wait - When you play this character, banish her or return another chosen" +
                        " character of yours to your hand.",
                    2,
                    true,
                    1,
                    "I've got you rattled now!"
                )
                cardDataRef.child(card49.cardNumber.toString()).setValue(card49)
            }, { exception ->
                // Handle failure in image upload or URL retrieval
                Log.e("InitializeCards", "Image upload failed: ${exception.message}")
            })

            imageUrl = "gs://deckvault-a0189.appspot.com/52.png"
            addImage(imageUrl, { imageUrl ->
                // Image uploaded successfully, use the imageUrl to initialize CardClass or perform other operations
                val card52 = CardClass(
                    "Merlin",
                    imageUrl.toString(),
                    52,
                    "Rabbit",
                    "Amethyst",
                    mutableListOf("Storyborn", "Mentor", "Sorcerer"),
                    2,
                    3,
                    "Hoppity Hip! - When you play this character and when he leaves play, you may draw a card.",
                    4,
                    false,
                    1,
                    "It was turning out to be a bad hare day."
                )
                cardDataRef.child(card52.cardNumber.toString()).setValue(card52)
            }, { exception ->
                // Handle failure in image upload or URL retrieval
                Log.e("InitializeCards", "Image upload failed: ${exception.message}")
            })

            imageUrl = "gs://deckvault-a0189.appspot.com/141.png"
            addImage(imageUrl, { imageUrl ->
                // Image uploaded successfully, use the imageUrl to initialize CardClass or perform other operations
                val card141 = CardClass(
                    "Belle",
                    imageUrl.toString(),
                    141,
                    "Inventive Engineer",
                    "Sapphire",
                    mutableListOf("Dreamborn", "Hero", "Princess", "Inventor"),
                    2,
                    3,
                    "Tinker - Whenever this character quests, you pay 1 ink less for the next item you play" +
                            " this turn.",
                    3,
                    true,
                    2,
                    "A little ingenuity and a lot of heart will take you far in this world."
                )
                cardDataRef.child(card141.cardNumber.toString()).setValue(card141)
            }, { exception ->
                // Handle failure in image upload or URL retrieval
                Log.e("InitializeCards", "Image upload failed: ${exception.message}")
            })

            imageUrl = "gs://deckvault-a0189.appspot.com/142.png"
            addImage(imageUrl, { imageUrl ->
                // Image uploaded successfully, use the imageUrl to initialize CardClass or perform other operations
                val card142 = CardClass(
                    "Belle",
                    imageUrl.toString(),
                    142,
                    "Strange but Special",
                    "Sapphire",
                    mutableListOf("Storyborn", "Hero", "Princess"),
                    2,
                    4,
                    "Read a book - During your turn, you may put an additional card from your hand into your" +
                        " inkwell facedown." + "My favorite part! - While you have 10 or more cards in your inkwell, " +
                        "this character gets +4 lore.",
                    4,
                    true,
                    1,
                    "Far-off places, daring sword fights, magic spells, a prince in disguise..."
                )
                cardDataRef.child(card142.cardNumber.toString()).setValue(card142)
            }, { exception ->
                // Handle failure in image upload or URL retrieval
                Log.e("InitializeCards", "Image upload failed: ${exception.message}")
            })

            imageUrl = "gs://deckvault-a0189.appspot.com/150.png"
            addImage(imageUrl, { imageUrl ->
                // Image uploaded successfully, use the imageUrl to initialize CardClass or perform other operations
                val card150 = CardClass(
                    "Maleficent",
                    imageUrl.toString(),
                    150,
                    "Sinister Visitor",
                    "Sapphire",
                    mutableListOf("Storyborn", "Villain", "Sorcerer"),
                    3,
                    4,
                    "",
                    4,
                    true,
                    2,
                    "The princess shall indeed grow in grace and beauty, beloved by all who know here. " +
                            " But before the un sets on her sixteenth birthday, she shall prick her finger on the spindle of a " +
                            "spinning wheel...."
                )
                cardDataRef.child(card150.cardNumber.toString()).setValue(card150)
            }, { exception ->
                // Handle failure in image upload or URL retrieval
                Log.e("InitializeCards", "Image upload failed: ${exception.message}")
            })

            imageUrl = "gs://deckvault-a0189.appspot.com/193.png"
            addImage(imageUrl, { imageUrl ->
                // Image uploaded successfully, use the imageUrl to initialize CardClass or perform other operations
                val card193 = CardClass(
                    "Robin Hood",
                    imageUrl.toString(),
                    193,
                    "Capable Fighter",
                    "Steel",
                    mutableListOf("Dreamborn", "Hero"),
                    1,
                    3,
                    "Deal 1 damage to chosen character.",
                    2,
                    false,
                    1,
                    "'Capable? You don't know the half of it.' - Little John"
                )
                cardDataRef.child(card193.cardNumber.toString()).setValue(card193)
            }, { exception ->
                // Handle failure in image upload or URL retrieval
                Log.e("InitializeCards", "Image upload failed: ${exception.message}")
            })
        }


        // Define a callback interface to handle success and failure cases
        interface CardDataCallback {
            fun onSuccess(initializedCards: List<CardClass>)
            fun onError(databaseError: DatabaseError)
        }

        // Modify your retrieveInitializedCards() function to accept a callback parameter
        fun retrieveInitializedCards(callback: CardDataCallback) {
            val cardDataRef = FirebaseDatabase.getInstance().getReference("CardData")

            cardDataRef.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    val initializedCards = mutableListOf<CardClass>()

                    for (cardSnapshot in dataSnapshot.children) {
                        // Parse data from dataSnapshot and create CardClass objects
                        val card = cardSnapshot.getValue(CardClass::class.java)
                        card?.let {
                            initializedCards.add(it)
                        }
                    }

                    // Notify the callback of success and provide the initialized cards
                    callback.onSuccess(initializedCards)
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    // Notify the callback of error
                    callback.onError(databaseError)
                }
            })
        }



        //Function to add image url to storage
        private fun addImage(imageUrl: String, onSuccess: (Uri) -> Unit, onFailure: (Exception) -> Unit) {
            val storageRef = FirebaseStorage.getInstance().getReferenceFromUrl(imageUrl)

            val localFile = File.createTempFile("images", "jpg")

            storageRef.getFile(localFile)
                .addOnSuccessListener {
                    val fileUri = Uri.fromFile(localFile)
                    onSuccess(fileUri)
                }
                .addOnFailureListener { exception ->
                    onFailure(exception)
                }
        }

        fun stopCardListener()
        {
            listener?.let {
                val userDataRef = database.getReference("UserData").child(user.uid)
                    .child("Cards")
                userDataRef.removeEventListener(it)
                listener = null
            }
        }
    }

class DeckRepository(private val database: FirebaseDatabase, private val user: FirebaseUser) {
    public var Decks = mutableListOf<DeckClass>()
    private val _isDeckDataReady = MutableLiveData<Boolean>()
    val isDeckDataReady: LiveData<Boolean>
        get() = _isDeckDataReady

    fun setDeckDataReady(isReady: Boolean) {
        _isDeckDataReady.value = isReady
    }

    private var listener: ValueEventListener? = null

    init {
        fetchDeckData()
    }

    private fun removeDeck(deck: DeckClass, owner: LifecycleOwner) {
        val database = FirebaseDatabase.getInstance()

        // Reference to the specific deck to be removed
        val deckRef = database.reference.child("UserData").child(user.uid).child("Deck").child(deck.deckName)

        // Remove the specific deck from the user's decks
        deckRef.removeValue().addOnCompleteListener { removeTask ->
            if (removeTask.isSuccessful) {
                // If the deck is successfully removed, update the user's deck count
                val userDataRef = database.reference.child("UserData").child(user.uid).child("DeckCount")

                // Get the current user's deck count and decrement it by 1
                userDataRef.get().addOnSuccessListener { dataSnapshot ->
                    val currentDeckCount = dataSnapshot.getValue(Int::class.java) ?: 0
                    val newDeckCount = currentDeckCount - 1

                    // Update the user's deck count in Firebase
                    userDataRef.setValue(newDeckCount)
                }.addOnFailureListener { exception ->
                    // Handle failure to fetch user's deck count
                }
            } else {
                // Handle failure to remove the deck
            }
        }
    }

    fun fetchDeckData() {

        val deckListRef = database.getReference("UserData/${user.uid}/Decks")
        Decks.clear() // Clear existing data

        deckListRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                _isDeckDataReady.postValue(false)

                for (deckSnapshot in dataSnapshot.children) {
                    // Retrieve deck information from Firebase
                    val deckImage = deckSnapshot.child("deckImage").getValue(String::class.java)
                    val deckName = deckSnapshot.child("deckName").getValue(String::class.java)
                    val deckCardCount = deckSnapshot.child("deckCardCount").getValue(Int::class.java)
                    val deckCardList = (deckSnapshot.child("Cards")).value as? MutableList<CardClass>

                    if (deckImage != null && deckName != null && deckCardCount != null) {
                        val deck = DeckClass(deckImage, deckName, deckCardCount, deckCardList ?: mutableListOf())
                        Decks.add(deck)
                    }
                }
                _isDeckDataReady.value = true
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Handle cancellation or errors in fetching deck list
            }
        })
    }

//        fun updateDeckData(deck: DeckClass)
//        {
//            val userDataRef = database.getReference("UserData").child(user.uid).child("Decks")
//
//            // Will need to add functionality here to allow user to modify decks
//        }


    fun addDeck(deckCover: String, deckName: String, deckCardCount: Int, deckCardList: MutableList<CardClass>) {
        val deckDataRef = database.getReference("UserData").child(user.uid).child("Decks")

        val newDeck = DeckClass(
            deckImage = deckCover,
            deckName = deckName,
            deckCardCount = deckCardCount,
            deckCardList = deckCardList ?: mutableListOf()
        )

        val newDeckRef = deckDataRef.push() // Generate a new unique key for the deck
        newDeckRef.setValue(newDeck)
            .addOnSuccessListener {
                // Increment the deck count after successfully adding the deck
                incrementDeckCount()
                // Set isDeckDataReady to true after adding the deck
                setDeckDataReady(true)
            }
            .addOnFailureListener { e ->
                // Handle failure to add the deck
            }
    }

    private fun incrementDeckCount() {
        val userDataRef = database.getReference("UserData").child(user.uid)

        // Fetch the current deck count
        userDataRef.child("deckCount").addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val currentCount = dataSnapshot.getValue(Int::class.java) ?: 0
                val newCount = currentCount + 1

                // Update the deck count in the database
                userDataRef.child("deckCount").setValue(newCount)
                    .addOnSuccessListener {
                        // Handle successful update of deck count
                    }
                    .addOnFailureListener { e ->
                        // Handle failure to update deck count
                    }
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle cancellation
            }
        })
    }

    // For adding favorite functionality to decks
    // Not currently implemented
//        fun updateFavoriteStatus(deck: DeckClass)
//       {
//           val userDataRef = database.getReference("UserData").child(user.userID).child("Decks")
//           userDataRef.child(deck.deckId).child("IsFavorite").setValue(deck.isFav)
//        }


    fun stopDeckListener() {
        listener?.let {
            val userDataRef = database.getReference("UserData").child(user.uid)
                .child("Decks")
            userDataRef.removeEventListener(it)
            listener = null
        }
    }
}

class RecentRepository(private val database: FirebaseDatabase, private val user: FirebaseUser)
{
    var recentCardFeed = mutableListOf<CardClass>()
    private val _isRecentCardDataReady = MutableLiveData<Boolean>()
    val isRecentDataReady: LiveData<Boolean>
        get() = _isRecentCardDataReady

    private var listener: ValueEventListener? = null

    private val currentUser: FirebaseUser? = FirebaseAuth.getInstance().currentUser

    init
    {
        fetchRecentData()
    }

    private fun fetchRecentData()
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

    fun addRecentCard(card: CardClass) {
        val userDataRef = database.getReference("UserData").child(currentUser!!.uid).child("Recent Cards")
        val cardNumber = card.cardNumber.toString()
        val newCardRef = userDataRef.child(cardNumber)

        val cardDetails = mapOf(
            "Card Name" to card.cardName,
            "Card Image" to card.cardImage,
            "Card Number" to card.cardNumber,
            "Card SubName" to card.cardSubName,
            "Card Color" to card.cardColor,
            "Card Classes" to card.cardClasses,
            "Card Damage" to card.cardDamage,
            "Card Defense" to card.cardDefense,
            "Card Action" to card.cardAction,
            "Card Ink" to card.cardInk,
            "Card Inkable" to card.cardInkable,
            "Card Lore" to card.cardLore,
            "Card Description" to card.cardDescription
        )

        newCardRef.setValue(cardDetails)

        recentCardFeed.add(card)

        // Limit to 10 cards in recent feed
        if (recentCardFeed.size > 10) {
            recentCardFeed.removeAt(0) // Remove the oldest card if the list exceeds the limit
        }

        _isRecentCardDataReady.postValue(true)
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
