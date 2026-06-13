package com.issueissyu.fe.domain.model.notification

enum class NotificationType {
    LIKE,
    EVENT,
    HOT,
    STORE,
    ;

    //helper에서도 사용가능한 형태
    companion object {
        fun fromServer(value: String?): NotificationType? = when (value?.uppercase()) {
            "LIKE", "PIN_LIKED" -> LIKE
            "EVENT", "PIN_EVENT" -> EVENT
            "HOT", "PIN_POPULAR", "POPULAR" -> HOT
            "STORE", "PIN_STORE_AD", "STORE_AD" -> STORE
            else -> null
        }
    }
}
