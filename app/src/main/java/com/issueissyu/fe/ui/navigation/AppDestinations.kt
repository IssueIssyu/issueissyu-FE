package com.issueissyu.fe.ui.navigation

object AppDestinations {

    // 온보딩
    object Onboarding {
        const val SPLASH_ROUTE = "splash_route"
        const val LOGIN_ROUTE = "login_route"
        const val SIGNUP_ROUTE = "signup_route"
        const val TERM_ROUTE = "term_route"
        const val TERM_DETAIL_ROUTE = "term_detail_route/{termsType}"
        const val USER_VERIFICATION_ROUTE = "user_verification_route"
        const val LOCAL_VERIFICATION_ROUTE = "local_verification_route"
        const val COMPLETE_ROUTE = "onboarding_complete_route"

        fun termDetailRoute(termsType: String): String {
            return "term_detail_route/$termsType"
        }
    }

    // 메인 탭 / 일반 화면
    const val HOME_ROUTE = "home_route"
    const val COLLECTION_ROUTE = "collection_route"
    const val TOWN_ROUTE = "town_route"
    const val COMMUNITY_ROUTE = "community_route"
    const val MYPAGE_ROUTE = "mypage_route"

    // 지도 관련
    const val PATCH_NOTE_ROUTE = "patch_note_route"
    const val PIN_CREATION_ROUTE = "pin_creation_route"
    const val PIN_DETAIL_ROUTE = "pin_detail_route/{pinId}"
    const val PIN_REPORT_ROUTE = "pin_report_route/{pinId}"

    fun pinDetailRoute(pinId: String): String {
        return "pin_detail_route/$pinId"
    }

    fun pinReportRoute(pinId: String): String {
        return "pin_report_route/$pinId"
    }
}