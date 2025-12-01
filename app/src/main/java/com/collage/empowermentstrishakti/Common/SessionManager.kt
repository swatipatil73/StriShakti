package com.collage.empowermentstrishakti.Common

import android.content.Context

class SessionManager(context: Context) {

    private val sharedPreferences = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
    private val editor = sharedPreferences.edit()

    // Save user data (keeps existing method)
    fun saveUserData(userId: Int, userName: String, token: String) {
        editor.putInt("user_id", userId)
        editor.putString("user_name", userName)
        editor.putString("token", token)
        editor.apply()
    }

    // Optional: convenience method to save UUID separately (do not conflict with existing callsites)
    fun saveUserUuid(uuid: String) {
        editor.putString("user_uuid", uuid)
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

    // UUID getter — matches the name you used in activity (getuserUuid)
    fun getuserUuid(): String? {
        return sharedPreferences.getString("user_uuid", "")
    }

    // optional helper to clear session (handy)
    fun clear() {
        editor.clear().apply()
    }
}
