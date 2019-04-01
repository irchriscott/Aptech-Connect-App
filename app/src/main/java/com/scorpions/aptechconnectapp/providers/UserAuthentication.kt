package com.scorpions.aptechconnectapp.providers

import android.content.Context
import android.content.SharedPreferences
import android.preference.PreferenceManager
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import com.scorpions.aptechconnectapp.models.User

class UserAuthentication(
    private val contxt: Context
){
    private val context: Context = contxt
    private val sharedPreferences: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
    private val sharedPreferencesEditor: SharedPreferences.Editor = sharedPreferences.edit()

    public fun saveUserToSharedPreferences(user: User){
        val gson: Gson = Gson()
        val userString = gson.toJson(user, object: TypeToken<User>(){}.type)
        this.sharedPreferencesEditor.putString("user", userString)
        this.sharedPreferencesEditor.apply()
        this.sharedPreferencesEditor.commit()
    }

    public fun getUserFromSharedPreferences() : User? {
        val gson: Gson = GsonBuilder().create()
        val userString = this.sharedPreferences.getString("user", null)
        return if (userString != null) {
            gson.fromJson(userString, User::class.java)
        } else {
            null
        }
    }

    public fun isLoggedIn(): Boolean = this.sharedPreferences.getString("user", null) != null

    public fun logOutUser(){
        this.sharedPreferencesEditor.remove("user")
        this.sharedPreferencesEditor.apply()
    }

}