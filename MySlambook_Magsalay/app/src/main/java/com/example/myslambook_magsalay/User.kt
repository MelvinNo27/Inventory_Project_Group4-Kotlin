package com.example.myslambook_magsalay

import android.os.Parcel
import android.os.Parcelable

data class User(
    val firstName: String,
    val lastName: String,
    val nickname: String,
    val friendsCallYou: String,
    val likeToCallMe: String,
    val email: String,
    val status: String,
    val address: String,
    val contactNumber: String,
    val gender: String,
    val birthDate: String
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: ""
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(firstName)
        parcel.writeString(lastName)
        parcel.writeString(nickname)
        parcel.writeString(friendsCallYou)
        parcel.writeString(likeToCallMe)
        parcel.writeString(email)
        parcel.writeString(status)
        parcel.writeString(address)
        parcel.writeString(contactNumber)
        parcel.writeString(gender)
        parcel.writeString(birthDate)
    }

    override fun describeContents(): Int = 0

    companion object CREATOR : Parcelable.Creator<User> {
        override fun createFromParcel(parcel: Parcel): User {
            return User(parcel)
        }

        override fun newArray(size: Int): Array<User?> {
            return arrayOfNulls(size)
        }
    }
}

