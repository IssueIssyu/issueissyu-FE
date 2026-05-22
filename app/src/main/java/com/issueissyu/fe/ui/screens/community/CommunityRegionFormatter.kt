package com.issueissyu.fe.ui.screens.community

import com.issueissyu.fe.domain.model.LocationRegionGroup

internal fun LocationRegionGroup.toCommunityRegion(location: String): String {
    return superLocation.toCommunityRegion(location)
}

internal fun String.toCommunityRegion(location: String): String {
    if (location == this || location.startsWith("$this ")) return location

    val city = cityByDistrict[this]?.get(location)
    return if (city != null) {
        "$this $city $location"
    } else {
        "$this $location"
    }
}

internal fun String.toRegionDisplayName(): String {
    return substringAfterLast(" ").ifBlank { this }
}

private val cityByDistrict: Map<String, Map<String, String>> = mapOf(
    "경기도" to mapOf(
        "장안구" to "수원시",
        "권선구" to "수원시",
        "팔달구" to "수원시",
        "영통구" to "수원시",
        "수정구" to "성남시",
        "중원구" to "성남시",
        "분당구" to "성남시",
        "만안구" to "안양시",
        "동안구" to "안양시",
        "상록구" to "안산시",
        "단원구" to "안산시",
        "덕양구" to "고양시",
        "일산동구" to "고양시",
        "일산서구" to "고양시",
        "처인구" to "용인시",
        "기흥구" to "용인시",
        "수지구" to "용인시",
        "원미구" to "부천시",
        "소사구" to "부천시",
        "오정구" to "부천시",
    ),
    "충청북도" to mapOf(
        "상당구" to "청주시",
        "서원구" to "청주시",
        "흥덕구" to "청주시",
        "청원구" to "청주시",
    ),
    "충청남도" to mapOf(
        "동남구" to "천안시",
        "서북구" to "천안시",
    ),
    "경상북도" to mapOf(
        "남구" to "포항시",
        "북구" to "포항시",
    ),
    "경상남도" to mapOf(
        "의창구" to "창원시",
        "성산구" to "창원시",
        "마산합포구" to "창원시",
        "마산회원구" to "창원시",
        "진해구" to "창원시",
    ),
    "전북특별자치도" to mapOf(
        "완산구" to "전주시",
        "덕진구" to "전주시",
    ),
)
