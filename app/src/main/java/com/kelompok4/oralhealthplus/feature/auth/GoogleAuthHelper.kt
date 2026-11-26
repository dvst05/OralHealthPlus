package com.kelompok4.oralhealthplus.feature.auth

import android.content.Context
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.GetCredentialResponse
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.GetCredentialException
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenParsingException
import com.kelompok4.oralhealthplus.BuildConfig

data class GoogleUserData(
    val email: String,
    val displayName: String,
    val idToken: String
)

object GoogleAuthHelper {

    suspend fun signInWithGoogle(context: Context): Result<GoogleUserData> {
        val serverClientId = BuildConfig.GOOGLE_WEB_CLIENT_ID
        if (serverClientId.isBlank()) {
            return Result.failure(IllegalStateException("GOOGLE_WEB_CLIENT_ID belum dikonfigurasi di local.properties"))
        }

        val credentialManager = CredentialManager.create(context)
        val googleIdOption = GetGoogleIdOption.Builder()
            .setFilterByAuthorizedAccounts(false)
            .setServerClientId(serverClientId)
            .setAutoSelectEnabled(false)
            .build()

        val request = GetCredentialRequest.Builder()
            .addCredentialOption(googleIdOption)
            .build()

        return try {
            val response: GetCredentialResponse = credentialManager.getCredential(
                request = request,
                context = context
            )
            handleCredentialResponse(response)
        } catch (e: GetCredentialCancellationException) {
            Result.failure(Exception("Login Google dibatalkan oleh pengguna"))
        } catch (e: GetCredentialException) {
            Result.failure(Exception("Gagal melakukan autentikasi Google: ${e.message}"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun handleCredentialResponse(response: GetCredentialResponse): Result<GoogleUserData> {
        val credential = response.credential
        return if (credential is CustomCredential &&
            credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
        ) {
            try {
                val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                val email = googleIdTokenCredential.id
                val displayName = googleIdTokenCredential.displayName
                    ?: googleIdTokenCredential.givenName
                    ?: email.substringBefore("@")
                Result.success(
                    GoogleUserData(
                        email = email,
                        displayName = displayName,
                        idToken = googleIdTokenCredential.idToken
                    )
                )
            } catch (e: GoogleIdTokenParsingException) {
                Result.failure(Exception("Token Google tidak valid: ${e.message}"))
            }
        } else {
            Result.failure(Exception("Kredensial yang diterima tidak dikenali"))
        }
    }
}
