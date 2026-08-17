package com.ambrxsh.buzzbuddy.utils

import android.content.Context
import android.content.SharedPreferences

class SessionStore(context: Context) {

    private val prefs: SharedPreferences =
        context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun getAccessToken(): String? {
        return prefs.getString(KEY_ACCESS_TOKEN, null)
            ?: prefs.getString(KEY_LEGACY_TOKEN, null)
    }

    fun getRefreshToken(): String? = prefs.getString(KEY_REFRESH_TOKEN, null)

    fun getEmail(): String? = prefs.getString(KEY_EMAIL, null)

    fun getName(): String? = prefs.getString(KEY_NAME, null)

    fun isLoggedIn(): Boolean = !getAccessToken().isNullOrBlank()

    fun saveSession(
        accessToken: String,
        refreshToken: String?,
        email: String,
        name: String? = getName()
    ) {
        prefs.edit()
            .putString(KEY_ACCESS_TOKEN, accessToken)
            .putString(KEY_LEGACY_TOKEN, accessToken)
            .putString(KEY_REFRESH_TOKEN, refreshToken)
            .putString(KEY_EMAIL, email)
            .apply {
                if (!name.isNullOrBlank()) putString(KEY_NAME, name)
            }
            .apply()
    }

    fun saveProfile(name: String?, email: String?) {
        prefs.edit()
            .apply {
                if (!name.isNullOrBlank()) putString(KEY_NAME, name)
                if (!email.isNullOrBlank()) putString(KEY_EMAIL, email)
            }
            .apply()
    }

    fun clear() {
        prefs.edit()
            .remove(KEY_ACCESS_TOKEN)
            .remove(KEY_LEGACY_TOKEN)
            .remove(KEY_REFRESH_TOKEN)
            .remove(KEY_EMAIL)
            .remove(KEY_NAME)
            .apply()
    }

    companion object {
        const val PREFS_NAME = "MyPrefs"
        private const val KEY_ACCESS_TOKEN = "access_token"
        private const val KEY_LEGACY_TOKEN = "token"
        private const val KEY_REFRESH_TOKEN = "refresh_token"
        private const val KEY_EMAIL = "email"
        private const val KEY_NAME = "name"
    }
}
