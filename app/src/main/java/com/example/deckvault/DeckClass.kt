package com.example.deckvault

import android.os.Parcel
import android.os.Parcelable

data class DeckClass(
        var deckImage: String = "",
        var deckName: String = "",
        var deckCardCount: Int = 0,
        var deckCardList: MutableList<CardClass> = mutableListOf()
) : Parcelable {

        constructor(parcel: Parcel) : this(
                parcel.readString() ?: "",
                parcel.readString() ?: "",
                parcel.readInt(),
                parcel.createTypedArrayList(CardClass.CREATOR) ?: mutableListOf()
        )

        override fun writeToParcel(parcel: Parcel, flags: Int) {
                parcel.writeString(deckImage)
                parcel.writeString(deckName)
                parcel.writeInt(deckCardCount)
                parcel.writeTypedList(deckCardList)
        }

        override fun describeContents(): Int {
                return 0
        }

        companion object CREATOR : Parcelable.Creator<DeckClass> {
                override fun createFromParcel(parcel: Parcel): DeckClass {
                        return DeckClass(parcel)
                }

                override fun newArray(size: Int): Array<DeckClass?> {
                        return arrayOfNulls(size)
                }
        }
}
