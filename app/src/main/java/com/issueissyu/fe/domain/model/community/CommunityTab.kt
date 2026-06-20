package com.issueissyu.fe.domain.model.community

enum class CommunityTab(val displayName: String) {
    ALL("전체"),
    HOT("HOT"),
    ISSUE("이슈"),
    COMMUNICATION("소통"),
    STORE("가게 홍보"),
    FESTIVAL("축제·행사"),
    POLICY("정책"),
    CONTEST("공모전"),
    CARDNEWS("카드뉴스");

    companion object {
        val visibleTabs = listOf(
            HOT,
            ISSUE,
            STORE,
            FESTIVAL,
            POLICY,
            CONTEST,
            CARDNEWS,
            ALL
        )
    }
}
