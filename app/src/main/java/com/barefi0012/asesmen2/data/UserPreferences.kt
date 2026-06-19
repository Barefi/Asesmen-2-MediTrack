package com.barefi0012.asesmen2.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.barefi0012.asesmen2.model.UserProfile
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class UserPreferences(private val context: Context) {
    private companion object {
        val NAME_KEY = stringPreferencesKey("user_name")
        val EMAIL_KEY = stringPreferencesKey("user_email")
        val PHOTO_URL_KEY = stringPreferencesKey("user_photo_url")
    }

    val userProfile: Flow<UserProfile> = context.dataStore.data.map { prefs ->
        UserProfile(
            name = prefs[NAME_KEY].orEmpty(),
            email = prefs[EMAIL_KEY].orEmpty(),
            photoUrl = prefs[PHOTO_URL_KEY].orEmpty()
        )
    }

    suspend fun saveUser(profile: UserProfile) {
        context.dataStore.edit { prefs ->
            prefs[NAME_KEY] = profile.name
            prefs[EMAIL_KEY] = profile.email
            prefs[PHOTO_URL_KEY] = profile.photoUrl
        }
    }

    suspend fun clearUser() {
        saveUser(UserProfile())
    }
}
