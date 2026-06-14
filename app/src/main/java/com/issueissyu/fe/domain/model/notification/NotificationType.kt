package com.issueissyu.fe.domain.model.notification

enum class NotificationType {
    LIKE,
    EVENT,
    HOT,
    STORE,
    ;

    companion object {
        fun fromServer(value: String?): NotificationType? = when (value?.uppercase()) {
            "LIKE", "PIN_LIKED" -> LIKE
            "EVENT", "PIN_EVENT" -> EVENT
            "HOT", "PIN_POPULAR" -> HOT
            "STORE", "PIN_STORE_AD" -> STORE
            else -> null
        }
    }
}
