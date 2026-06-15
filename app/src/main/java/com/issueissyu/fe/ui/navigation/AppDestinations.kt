package com.issueissyu.fe.ui.navigation

object AppDestinations {

    // 온보딩
    object Onboarding {
        const val SPLASH_ROUTE = "splash_route"
        const val LOGIN_ROUTE = "login_route"
        const val LOGIN_ROUTE_WITH_ARGS = "login_route?storageWarning={storageWarning}"

        fun loginRoute(showStorageWarning: Boolean = false): String =
            if (showStorageWarning) {
                "$LOGIN_ROUTE?storageWarning=true"
            } else {
                LOGIN_ROUTE
            }
        const val SIGNUP_ROUTE = "signup_route"
        const val TERM_ROUTE = "term_route"
        const val TERM_DETAIL_ROUTE = "term_detail_route/{termsType}"
        const val USER_VERIFICATION_ROUTE = "user_verification_route"
        const val LOCAL_VERIFICATION_ROUTE = "local_verification_route"
        const val COMPLETE_ROUTE = "onboarding_complete_route"
        const val LANDING_ROUTE = "landing_route"

        fun termDetailRoute(termsType: String): String {
            return "term_detail_route/$termsType"
        }
    }

    //마이페이지
    object MyPage{
        const val MYPAGE_ROUTE = "mypage_route"
        const val PROFILE_CHANGE_ROUTE = "mypage/profile_change"
        const val LOCAL_CHANGE_ROUTE = "mypage/local_change"
        const val TERMS_ROUTE = "mypage/terms"
        const val MY_ISSUES_ROUTE = "mypage/my_issues"
        const val ALARM_SETTINGS_ROUTE = "mypage/alarm_settings"
    }

    // 메인 탭 / 일반 화면
    const val MYPAGE_ROUTE = MyPage.MYPAGE_ROUTE
    const val TOWN_ROUTE = "town_route"
    const val COLLECTION_ROUTE = "collection_route"
    const val COMMUNITY_ROUTE = "community_route"
    const val COMMUNITY_DETAIL_ROUTE = "community_detail_route/{communityId}"
    const val COMMUNITY_REPORT_ROUTE = "community_report_route/{communityId}"

    // 지도 관련
    const val PATCH_NOTE_ROUTE = "patch_note_route"
    const val PIN_CREATION_ROUTE = "pin_creation_route"
    const val PIN_DETAIL_ROUTE = "pin_detail_route/{pinId}"
    const val PIN_REPORT_ROUTE = "pin_report_route/{pinId}"
    const val TOWN_ROUTE_WITH_FOCUS_PIN = "town_route?focusPinId={focusPinId}"
    fun communityDetailRoute(communityId: Long): String {
        return "community_detail_route/$communityId"
    }

    fun communityReportRoute(communityId: Long): String {
        return "community_report_route/$communityId"
    }

    fun pinDetailRoute(pinId: String): String {
        return "pin_detail_route/$pinId"
    }

    fun townRouteWithFocusPin(pinId: Long): String {
        return "town_route?focusPinId=$pinId"
    }

    fun pinReportRoute(pinId: String): String {
        return "pin_report_route/$pinId"
    }
}
