package com.issueissyu.fe.data.local

import android.content.Context
import android.content.SharedPreferences
import android.util.Base64

//암호화 라이브러리 - deprecated 경고
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import org.json.JSONObject

import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

// 토큰 관리 담당 로직
@Singleton
class TokenManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    //암호화 키 생성
    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    private val sharedPreferences: SharedPreferences = EncryptedSharedPreferences.create(
        context,
        PREFS_NAME,
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    fun saveTokens(
        accessToken: String,
        refreshToken: String,
        isNewUser: Boolean? = null,
        tempUuid: String? = null,
        loginSocialType: String? = null,
        userUuid: String? = null,
        userName: String? = null,
    ) {
        sharedPreferences.edit().apply {
            putString(KEY_ACCESS_TOKEN, accessToken)
            putString(KEY_REFRESH_TOKEN, refreshToken)
            if (isNewUser != null) {
                putBoolean(KEY_IS_NEW_USER, isNewUser)
            }
            if (tempUuid != null) {
                putString(KEY_TEMP_UUID, tempUuid)
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
    }

    fun getAccessToken(): String? {
        return sharedPreferences.getString(KEY_ACCESS_TOKEN, null)
    }

    fun getRefreshToken(): String? {
        return sharedPreferences.getString(KEY_REFRESH_TOKEN, null)
    }

    fun getTempUuid(): String? {
        return sharedPreferences.getString(KEY_TEMP_UUID, null)
    }

    /** 로그인 연동 등으로 temp 식별자가 더 이상 유효하지 않을 때 */
    fun clearTempUuid() {
        sharedPreferences.edit().remove(KEY_TEMP_UUID).apply()
    }

    fun getLoginSocialType(): String? =
        sharedPreferences.getString(KEY_LOGIN_SOCIAL_TYPE, null)

    fun saveCurrentUser(
        userUuid: String,
        userName: String? = null,
    ) {
        sharedPreferences.edit().apply {
            putString(KEY_USER_UUID, userUuid)
            if (!userName.isNullOrBlank()) {
                putString(KEY_USER_NAME, userName)
            }
            apply()
        }
    }

    fun getCurrentUserUuid(): String? {
        val savedUuid = sharedPreferences.getString(KEY_USER_UUID, null)
            ?.takeIf { it.isNotBlank() }
        if (savedUuid != null) return savedUuid

        val tokenUuid = extractUserUuidFromAccessToken()
        if (!tokenUuid.isNullOrBlank()) {
            saveCurrentUser(userUuid = tokenUuid)
        }
        return tokenUuid
    }

    fun getCurrentUserName(): String? =
        sharedPreferences.getString(KEY_USER_NAME, null)

    fun clearCurrentUser() {
        sharedPreferences.edit().apply {
            remove(KEY_USER_UUID)
            remove(KEY_USER_NAME)
            apply()
        }
    }

    fun clearTokens() {
        sharedPreferences.edit().apply {
            remove(KEY_ACCESS_TOKEN)
            remove(KEY_REFRESH_TOKEN)
            remove(KEY_IS_NEW_USER)
            remove(KEY_TEMP_UUID)
            remove(KEY_LOGIN_SOCIAL_TYPE)
            remove(KEY_USER_UUID)
            remove(KEY_USER_NAME)
            apply()
        }
    }

    fun getIsNewUser(): Boolean {
        if (!hasTokens()) return false
        return sharedPreferences.getBoolean(KEY_IS_NEW_USER, false)
    }

    fun clearNewUserFlag() {
        sharedPreferences.edit().putBoolean(KEY_IS_NEW_USER, false).apply()
    }

    fun hasTokens(): Boolean {
        return getAccessToken() != null && getRefreshToken() != null
    }

    private fun extractUserUuidFromAccessToken(): String? {
        val token = getAccessToken()?.takeIf { it.isNotBlank() } ?: return null
        val payload = token.split(".").getOrNull(1) ?: return null
        return runCatching {
            val normalizedPayload = payload.padEnd(((payload.length + 3) / 4) * 4, '=')
            val decodedBytes = Base64.decode(
                normalizedPayload,
                Base64.URL_SAFE or Base64.NO_WRAP
            )
            val json = JSONObject(String(decodedBytes, Charsets.UTF_8))
            listOf("uuid", "userUuid", "sub")
                .firstNotNullOfOrNull { key ->
                    json.optString(key).takeIf { it.isNotBlank() }
                }
        }.getOrNull()
    }

    companion object {
        private const val PREFS_NAME = "auth_prefs"
        private const val KEY_ACCESS_TOKEN = "access_token"
        private const val KEY_REFRESH_TOKEN = "refresh_token"
        private const val KEY_IS_NEW_USER = "is_new_user"
        private const val KEY_TEMP_UUID = "temp_uuid"
        private const val KEY_LOGIN_SOCIAL_TYPE = "login_social_type"
        private const val KEY_USER_UUID = "user_uuid"
        private const val KEY_USER_NAME = "user_name"
    }
}