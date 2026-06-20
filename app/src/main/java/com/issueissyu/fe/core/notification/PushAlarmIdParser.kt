package com.issueissyu.fe.core.notification

import android.content.Intent

object PushAlarmIdParser {

    private val knownKeys = listOf(
        NotificationHelper.EXTRA_ALARM_ID,
        "alarmId",
        "likeAlarmId",
        "eventAlarmId",
        "storeAlarmId",
        "hotAlarmId",
    )

    fun parseRaw(data: Map<String, String>): String? {
        val known = knownKeys.firstNotNullOfOrNull { key ->
            data[key]?.takeIf { it.isNotBlank() }
        }
        if (known != null) return known
        return data.entries
            .firstOrNull { (key, value) ->
                key.endsWith("AlarmId", ignoreCase = true) && value.isNotBlank()
            }
            ?.value
    }

    fun parseRaw(intent: Intent?): String? {
        if (intent == null) return null
        val known = knownKeys.firstNotNullOfOrNull { key ->
            intent.getStringExtra(key)?.takeIf { it.isNotBlank() }
        }
        if (known != null) return known
        return intent.extras
            ?.keySet()
            ?.firstOrNull { key -> key.endsWith("AlarmId", ignoreCase = true) }
            ?.let { key -> intent.extras?.get(key)?.toString() }
            ?.takeIf { it.isNotBlank() }
    }

    fun parseLong(data: Map<String, String>): Long? = parseRaw(data)?.toLongOrNull()

    fun parseLong(intent: Intent?): Long? = parseRaw(intent)?.toLongOrNull()

    fun clearFromIntent(intent: Intent) {
        knownKeys.forEach { key -> intent.removeExtra(key) }
    }
}