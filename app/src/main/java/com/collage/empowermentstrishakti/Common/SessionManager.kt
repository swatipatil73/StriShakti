package com.collage.empowermentstrishakti.Common

import android.content.Context

class SessionManager(context: Context) {

    private val sharedPreferences = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
    private val editor = sharedPreferences.edit()
    private lateinit var sessionManager: SessionManager

    // Save user data
    fun saveUserData(userId: Int, userName: String, token: String) {
        editor.putInt("user_id", userId)
        editor.putString("user_name", userName)
        editor.putString("token", token)
        editor.apply()

    }

    // Get user data
    fun getUserId(): Int {
        return sharedPreferences.getInt("user_id", -1)
    }

    fun getUserName(): String? {
        return sharedPreferences.getString("user_name", "")
    }

    fun getToken(): String? {
        return sharedPreferences.getString("token", "")
    }
    fun getuserUuid(): String? {
        return sharedPreferences.getString("userUUID", "")
    }
    // Clear all data (logout)
    fun clear() {
        editor.clear()
        editor.apply()
    }
}
