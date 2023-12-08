package com.example.deckvault

import android.os.Parcel
import android.os.Parcelable

data class CardClass (
    var cardName : String? = null,
    var cardImage : String? = null,
    val cardNumber : Int? = null,
    var cardSubName: String? = null,
    var cardColor: String? = null,
    var cardClasses: MutableList<String> = mutableListOf(),
    val cardDamage: Int? = null,
    val cardDefense: Int? = null,
    var cardAction: String? = null,
    val cardInk: Int? = null,
    val cardInkable: Boolean,
    val cardLore: Int? = null,
    var cardDescription: String? = null
): Parcelable {
    // Implementing Parcelable interface methods

    constructor(parcel: Parcel) : this(
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readInt(),
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        mutableListOf<String>().apply {
            parcel.readList(this, String::class.java.classLoader)
        },
        parcel.readInt(),
        parcel.readInt(),
        parcel.readString() ?: "",
        parcel.readInt(),
        parcel.readBoolean(),
        parcel.readInt(),
        parcel.readString() ?: ""
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(cardName)
        parcel.writeString(cardImage)
        parcel.writeInt(cardNumber ?: -1)
        parcel.writeString(cardSubName)
        parcel.writeString(cardColor)
        parcel.writeList(cardClasses)
        parcel.writeInt(cardDamage ?: -1)
        parcel.writeInt(cardDefense ?: -1)
        parcel.writeString(cardAction)
        parcel.writeInt(cardInk ?: -1)
        parcel.writeByte(if (cardInkable) 1 else 0)
        parcel.writeInt(cardLore ?: -1)
        parcel.writeString(cardDescription)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<CardClass> {
        override fun createFromParcel(parcel: Parcel): CardClass {
            return CardClass(parcel)
        }

        override fun newArray(size: Int): Array<CardClass?> {
            return arrayOfNulls(size)
        }
    }
}