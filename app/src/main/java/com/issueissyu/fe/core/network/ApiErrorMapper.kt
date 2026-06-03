package com.issueissyu.fe.core.network

import com.google.gson.Gson
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ApiErrorMapper @Inject constructor(
    private val gson: Gson,
) {
    private data class ErrorEnvelope(
        val code: String = "",
        val message: String = "",
    )

    fun toUserMessage(throwable: Throwable, fallback: String): String {
        return when (throwable) {
            is HttpException -> messageFromHttp(throwable, fallback)
            is IOException -> NETWORK_ERROR_MESSAGE
            else -> throwable.message?.takeIf { isUserFacingMessage(it) } ?: fallback
        }
    }

    fun toException(throwable: Throwable, fallback: String): Exception {
        val message = toUserMessage(throwable, fallback)
        return if (throwable is Exception) {
            Exception(message, throwable)
        } else {
            Exception(message)
        }
    }

    private fun messageFromHttp(exception: HttpException, fallback: String): String {
        val envelope = exception.response()
            ?.errorBody()
            ?.string()
            ?.takeIf { it.isNotBlank() }
            ?.let { body ->
                runCatching { gson.fromJson(body, ErrorEnvelope::class.java) }.getOrNull()
            }
        return envelope?.message?.takeIf { isUserFacingMessage(it) } ?: fallback
    }

    private fun isUserFacingMessage(message: String): Boolean {
        if (message.isBlank()) return false
        if (message.startsWith("HTTP ")) return false
        return true
    }

    companion object {
        const val NETWORK_ERROR_MESSAGE = "네트워크 연결을 확인한 뒤 다시 시도해주세요."
    }
}
