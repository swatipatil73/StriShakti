package com.collage.empowermentstrishakti.Common

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

class SessionManager(context: Context) {

    private val sharedPreferences: SharedPreferences

    init {
        val masterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()

        sharedPreferences = EncryptedSharedPreferences.create(
            context,
            "secure_user_prefs",
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    // Save user data
    fun saveUserData(userId: Int, userName: String, token: String) {
        sharedPreferences.edit().apply {
            putInt("user_id", userId)
            putString("user_name", userName)
            putString("token", token)
            apply()
        }
    }



    fun saveRefreshToken(token: String) {
        sharedPreferences.edit().putString("refresh_token", token).apply()
    }

    fun getRefreshToken(): String? {
        return sharedPreferences.getString("refresh_token", null)
    }

    // Save boolean flags (fixed)
    fun setIsSwayamsiddha(value: Boolean) {
        sharedPreferences.edit().putBoolean("isSwayamsiddha", value).apply()
    }

    fun setIsAdiShakti(value: Boolean) {
        sharedPreferences.edit().putBoolean("isAdiShakti", value).apply()
    }

    // Get boolean flags
    fun isSwayamsiddhas(): Boolean {
        return sharedPreferences.getBoolean("isSwayamsiddha", false)
    }

    fun isAdiShaktis(): Boolean {
        return sharedPreferences.getBoolean("isAdiShakti", false)
    }

    // Optional: convenience method to save UUID separately (do not conflict with existing callsites)



    fun saveUserUuid(uuid: String) {
        sharedPreferences.edit().apply {
            putString("user_uuid", uuid)
            apply()
        }
    }
    companion object {
        private const val KEY_EXPIRY = "TOKEN_EXPIRY"
    }





    fun saveTokenExpiry(expiry: String) {
        sharedPreferences.edit().apply {
            putString(KEY_EXPIRY, expiry)
            apply()
        }
    }

    fun getTokenExpiry(): String? {
        return sharedPreferences.getString(KEY_EXPIRY, null)
    }
    // Get user data
    fun getUserId(): Int {
        return sharedPreferences.getInt("user_id", -1)
    }
    fun isLoggedIn(): Boolean {
        return !getToken().isNullOrEmpty()
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


    fun clear() {
        sharedPreferences.edit().apply {
            clear()
            apply()
        }
    }


    fun saveUserRoles(isSwayamsiddha: Boolean, isAdiShakti: Boolean) {
        sharedPreferences.edit().apply {
            putBoolean("IS_SWAYAMSIDHA", isSwayamsiddha)
            putBoolean("IS_ADISHAKTI", isAdiShakti)
            apply()
        }
    }


    fun isSwayamsiddha(): Boolean =
        sharedPreferences.getBoolean("IS_SWAYAMSIDHA", false)

    fun isAdiShakti(): Boolean =
        sharedPreferences.getBoolean("IS_ADISHAKTI", false)

}
