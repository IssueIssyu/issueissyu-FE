package com.issueissyu.fe.domain.model.notification

enum class NotificationType {
    LIKE,
    EVENT,
    HOT,
    STORE,
    ;

    companion object {
        fun fromServer(value: String?): NotificationType? = when (value?.uppercase()) {
            "LIKE" -> LIKE
            "EVENT" -> EVENT
            "HOT" -> HOT
            "STORE" -> STORE
            else -> null
        }
    }
}
