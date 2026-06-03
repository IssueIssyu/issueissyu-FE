package com.issueissyu.fe.core.network

import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
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

    suspend fun toUserMessage(throwable: Throwable, fallback: String): String {
        return when (throwable) {
            is HttpException -> messageFromHttp(throwable, fallback)
            is IOException -> NETWORK_ERROR_MESSAGE
            else -> throwable.message?.takeIf { isUserFacingMessage(it) } ?: fallback
        }
    }

    suspend fun toException(throwable: Throwable, fallback: String): Exception {
        val message = toUserMessage(throwable, fallback)
        return if (throwable is Exception) {
            Exception(message, throwable)
        } else {
            Exception(message)
        }
    }

    suspend fun readHttpErrorBody(exception: HttpException): String =
        withContext(Dispatchers.IO) {
            exception.response()?.errorBody()?.string().orEmpty()
        }

    suspend fun <T> parseHttpErrorEnvelope(exception: HttpException, type: Class<T>): T? =
        parseErrorBody(readHttpErrorBody(exception), type)

    fun <T> parseErrorBody(body: String, type: Class<T>): T? =
        body.takeIf { it.isNotBlank() }
            ?.let { raw ->
                runCatching { gson.fromJson(raw, type) }.getOrNull()
            }

    private suspend fun messageFromHttp(exception: HttpException, fallback: String): String {
        val envelope = parseHttpErrorEnvelope(exception, ErrorEnvelope::class.java)
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
