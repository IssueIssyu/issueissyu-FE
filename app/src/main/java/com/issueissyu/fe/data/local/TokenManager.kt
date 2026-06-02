package com.issueissyu.fe.data.local

import android.content.Context
import android.content.SharedPreferences
import android.util.Base64
import android.util.Log
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import dagger.hilt.android.qualifiers.ApplicationContext
import org.json.JSONObject
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TokenManager @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private var encryptedPrefs: SharedPreferences? = openEncryptedStorageWithRecovery()

    val isStorageAvailable: Boolean
        get() = encryptedPrefs != null

    private fun openEncryptedStorageWithRecovery(): SharedPreferences? {
        return try {
            createEncryptedSharedPreferences()
        } catch (e: Exception) {
            Log.w(TAG, "Encrypted prefs open failed, recreating store", e)
            context.deleteSharedPreferences(PREFS_NAME_ENCRYPTED)
            context.deleteSharedPreferences(PREFS_NAME_PLAIN_FALLBACK_LEGACY)
            try {
                createEncryptedSharedPreferences()
            } catch (retry: Exception) {
                Log.e(TAG, "Encrypted prefs unavailable", retry)
                null
            }
        }
    }

    private fun createEncryptedSharedPreferences(): SharedPreferences {
        val masterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()
        return EncryptedSharedPreferences.create(
            context,
            PREFS_NAME_ENCRYPTED,
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM,
        )
    }

    /**
     * 앱 기동 시 저장소를 열지 못한 뒤, 로그인·토큰 저장 시에만 한 번 재시도한다.
     * 읽기 경로에서는 호출하지 않는다.
     */
    fun retryOpenEncryptedStorage(): Boolean {
        if (encryptedPrefs != null) return true
        encryptedPrefs = openEncryptedStorageWithRecovery()
        return encryptedPrefs != null
    }

    fun saveTokens(
        accessToken: String,
        refreshToken: String,
        isNewUser: Boolean? = null,
        loginSocialType: String? = null,
        userUuid: String? = null,
        userName: String? = null,
    ): Boolean {
        if (!retryOpenEncryptedStorage()) return false
        val store = encryptedPrefs ?: return false
        store.edit().apply {
            putString(KEY_ACCESS_TOKEN, accessToken)
            putString(KEY_REFRESH_TOKEN, refreshToken)
            if (isNewUser != null) {
                putBoolean(KEY_IS_NEW_USER, isNewUser)
            }
            if (!loginSocialType.isNullOrBlank()) {
                putString(KEY_LOGIN_SOCIAL_TYPE, loginSocialType)
            }
            if (!userUuid.isNullOrBlank()) {
                putString(KEY_USER_UUID, userUuid)
            }
            if (!userName.isNullOrBlank()) {
                putString(KEY_USER_NAME, userName)
            }
            apply()
        }
        return true
    }

    fun getAccessToken(): String? =
        encryptedPrefs?.getString(KEY_ACCESS_TOKEN, null)

    fun getRefreshToken(): String? =
        encryptedPrefs?.getString(KEY_REFRESH_TOKEN, null)

    fun getLoginSocialType(): String? =
        encryptedPrefs?.getString(KEY_LOGIN_SOCIAL_TYPE, null)

    fun saveCurrentUser(
        userUuid: String,
        userName: String? = null,
    ): Boolean {
        if (!retryOpenEncryptedStorage()) return false
        val store = encryptedPrefs ?: return false
        store.edit().apply {
            putString(KEY_USER_UUID, userUuid)
            if (!userName.isNullOrBlank()) {
                putString(KEY_USER_NAME, userName)
            }
            apply()
        }
        return true
    }

    fun getCurrentUserUuid(): String? {
        val savedUuid = encryptedPrefs?.getString(KEY_USER_UUID, null)
            ?.takeIf { it.isNotBlank() }
        if (savedUuid != null) return savedUuid

        val tokenUuid = extractUserUuidFromAccessToken()
        if (!tokenUuid.isNullOrBlank()) {
            saveCurrentUser(userUuid = tokenUuid)
        }
        return tokenUuid
    }

    fun getCurrentUserName(): String? =
        encryptedPrefs?.getString(KEY_USER_NAME, null)

    fun clearCurrentUser() {
        encryptedPrefs?.edit()?.apply {
            remove(KEY_USER_UUID)
            remove(KEY_USER_NAME)
            apply()
        }
    }

    fun clearTokens() {
        encryptedPrefs?.edit()?.apply {
            remove(KEY_ACCESS_TOKEN)
            remove(KEY_REFRESH_TOKEN)
            remove(KEY_IS_NEW_USER)
            remove(KEY_LOGIN_SOCIAL_TYPE)
            remove(KEY_USER_UUID)
            remove(KEY_USER_NAME)
            apply()
        }
    }

    fun getIsNewUser(): Boolean {
        if (!hasTokens()) return false
        return encryptedPrefs?.getBoolean(KEY_IS_NEW_USER, false) == true
    }

    fun clearNewUserFlag() {
        encryptedPrefs?.edit()?.putBoolean(KEY_IS_NEW_USER, false)?.apply()
    }

    fun hasTokens(): Boolean {
        val store = encryptedPrefs ?: return false
        return !store.getString(KEY_ACCESS_TOKEN, null).isNullOrBlank() &&
            !store.getString(KEY_REFRESH_TOKEN, null).isNullOrBlank()
    }

    private fun extractUserUuidFromAccessToken(): String? {
        val token = getAccessToken()?.takeIf { it.isNotBlank() } ?: return null
        val payload = token.split(".").getOrNull(1) ?: return null
        return runCatching {
            val normalizedPayload = payload.padEnd(((payload.length + 3) / 4) * 4, '=')
            val decodedBytes = Base64.decode(
                normalizedPayload,
                Base64.URL_SAFE or Base64.NO_WRAP,
            )
            val json = JSONObject(String(decodedBytes, Charsets.UTF_8))
            listOf("uuid", "userUuid", "sub")
                .firstNotNullOfOrNull { key ->
                    json.optString(key).takeIf { it.isNotBlank() }
                }
        }.getOrNull()
    }

    companion object {
        private const val TAG = "TokenManager"
        private const val PREFS_NAME_ENCRYPTED = "auth_prefs"
        private const val PREFS_NAME_PLAIN_FALLBACK_LEGACY = "auth_prefs_plain_fallback"
        private const val KEY_ACCESS_TOKEN = "access_token"
        private const val KEY_REFRESH_TOKEN = "refresh_token"
        private const val KEY_IS_NEW_USER = "is_new_user"
        private const val KEY_LOGIN_SOCIAL_TYPE = "login_social_type"
        private const val KEY_USER_UUID = "user_uuid"
        private const val KEY_USER_NAME = "user_name"
    }
}