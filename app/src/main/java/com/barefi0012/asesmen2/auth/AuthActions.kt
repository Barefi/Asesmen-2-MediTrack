package com.barefi0012.asesmen2.auth

import android.content.Context
import android.util.Log
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.GetCredentialResponse
import androidx.credentials.exceptions.ClearCredentialException
import androidx.credentials.exceptions.GetCredentialException
import com.barefi0012.asesmen2.BuildConfig
import com.barefi0012.asesmen2.data.UserPreferences
import com.barefi0012.asesmen2.model.UserProfile
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenParsingException

object AuthActions {
    suspend fun signIn(context: Context, userPreferences: UserPreferences): String? {
        if (BuildConfig.API_KEY.isBlank()) {
            return "API_KEY belum diisi di local.properties."
        }

        val googleIdOption = GetGoogleIdOption.Builder()
            .setFilterByAuthorizedAccounts(false)
            .setServerClientId(BuildConfig.API_KEY)
            .build()
        val request = GetCredentialRequest.Builder()
            .addCredentialOption(googleIdOption)
            .build()

        return try {
            val credentialManager = CredentialManager.create(context)
            val result = credentialManager.getCredential(context, request)
            handleSignIn(result, userPreferences)
        } catch (e: GetCredentialException) {
            Log.e("SIGN-IN", "Error: ${e.errorMessage}")
            e.errorMessage?.toString() ?: "Login Google dibatalkan atau gagal."
        }
    }

    private suspend fun handleSignIn(
        result: GetCredentialResponse,
        userPreferences: UserPreferences
    ): String? {
        val credential = result.credential
        if (credential !is CustomCredential ||
            credential.type != GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
        ) {
            return "Credential Google tidak dikenali."
        }

        return try {
            val googleIdToken = GoogleIdTokenCredential.createFrom(credential.data)
            userPreferences.saveUser(
                UserProfile(
                    name = googleIdToken.displayName.orEmpty(),
                    email = googleIdToken.id,
                    photoUrl = googleIdToken.profilePictureUri?.toString().orEmpty()
                )
            )
            null
        } catch (e: GoogleIdTokenParsingException) {
            Log.e("SIGN-IN", "Error: ${e.message}")
            e.message ?: "Profil Google gagal dibaca."
        }
    }

    suspend fun signOut(context: Context, userPreferences: UserPreferences): String? {
        return try {
            CredentialManager.create(context).clearCredentialState(ClearCredentialStateRequest())
            userPreferences.clearUser()
            null
        } catch (e: ClearCredentialException) {
            Log.e("SIGN-IN", "Error: ${e.errorMessage}")
            e.errorMessage?.toString() ?: "Logout gagal."
        }
    }
}
