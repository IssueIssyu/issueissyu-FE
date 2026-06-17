package com.issueissyu.fe.core.time

/**
 * 서버 `timeAgo`는 경과 시간을 pseudo-datetime으로 인코딩한다.
 * 예: 1년 10개월 1일 4시간 30분 → `0001-10-01 04:30:00.000001`
 *
 * 표기 규칙 (가장 큰 해당 단위 하나만 표시):
 * - 60분 미만 → 분
 * - 24시간 미만 → 시간
 * - 31일 미만 → 일 (31일은 표기하지 않음)
 * - 12개월 미만 → 달 (12개월은 표기하지 않음)
 * - 그 이상 → 년 (개월 remainder 없음)
 */
private val ENCODED_TIME_AGO_REGEX =
    Regex("""^(\d{4})-(\d{2})-(\d{2}) (\d{2}):(\d{2}):(\d{2})(?:\.(\d+))?$""")

private const val MINUTES_PER_HOUR = 60L
private const val MINUTES_PER_DAY = 24 * MINUTES_PER_HOUR
private const val MINUTES_PER_MONTH = 30 * MINUTES_PER_DAY
private const val MINUTES_PER_YEAR = 12 * MINUTES_PER_MONTH

fun formatAlarmTimeAgo(raw: String): String {
    val trimmed = raw.trim()
    if (trimmed.isEmpty()) return trimmed

    val match = ENCODED_TIME_AGO_REGEX.matchEntire(trimmed) ?: return trimmed

    val years = match.groupValues[1].toInt()
    val months = match.groupValues[2].toInt()
    val days = match.groupValues[3].toInt()
    val hours = match.groupValues[4].toInt()
    val minutes = match.groupValues[5].toInt()
    val seconds = match.groupValues[6].toIntOrNull() ?: 0

    val totalMinutes = years * MINUTES_PER_YEAR +
        months * MINUTES_PER_MONTH +
        days * MINUTES_PER_DAY +
        hours * MINUTES_PER_HOUR +
        minutes +
        if (seconds > 0) 1 else 0

    if (totalMinutes <= 0) return "방금 전"

    return when {
        totalMinutes < MINUTES_PER_HOUR -> "${totalMinutes}분 전"
        totalMinutes < MINUTES_PER_DAY -> "${totalMinutes / MINUTES_PER_HOUR}시간 전"
        totalMinutes < 31 * MINUTES_PER_DAY -> "${totalMinutes / MINUTES_PER_DAY}일 전"
        totalMinutes < 12 * MINUTES_PER_MONTH -> "${totalMinutes / MINUTES_PER_MONTH}개월 전"
        else -> "${totalMinutes / MINUTES_PER_YEAR}년 전"
    }
}
