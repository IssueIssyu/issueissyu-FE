package com.issueissyu.fe.core.time

import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

/**
 * API에서 내려오는 다양한 날짜 문자열을 파싱한다.
 * 예: ISO-8601, `2026-05-19 19:13:22.871108` (공백 구분 + 마이크로초)
 */
fun parseFlexibleDateTime(
    raw: String,
    zone: ZoneId = ZoneId.systemDefault(),
): ZonedDateTime? {
    val trimmed = raw.trim()
    if (trimmed.isEmpty()) return null

    runCatching { OffsetDateTime.parse(trimmed).atZoneSameInstant(zone) }
        .getOrNull()
        ?.let { return it }
    runCatching { Instant.parse(trimmed).atZone(zone) }
        .getOrNull()
        ?.let { return it }
    runCatching { LocalDateTime.parse(trimmed).atZone(zone) }
        .getOrNull()
        ?.let { return it }

    val spaceToT = trimmed.replaceFirst(' ', 'T')
    if (spaceToT != trimmed) {
        runCatching { LocalDateTime.parse(spaceToT).atZone(zone) }
            .getOrNull()
            ?.let { return it }
    }

    val localFormatters = listOf(
        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSSSSS"),
        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS"),
        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"),
        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"),
    )
    for (formatter in localFormatters) {
        runCatching { LocalDateTime.parse(trimmed, formatter).atZone(zone) }
            .getOrNull()
            ?.let { return it }
    }

    return null
}

//날짜 표기
//올해 -> MM.DD HH:MM
//else -> YY.MM.DD
fun formatPinHomeCreatedAt(
    raw: String,
    zone: ZoneId = ZoneId.systemDefault(),
): String {
    val dateTime = parseFlexibleDateTime(raw, zone) ?: return raw
    val localDate = dateTime.toLocalDate()
    return if (localDate.year == LocalDate.now(zone).year) {
        dateTime.format(DateTimeFormatter.ofPattern("MM.dd  HH:mm", Locale.getDefault()))
    } else {
        localDate.format(DateTimeFormatter.ofPattern("yy.MM.dd", Locale.getDefault()))
    }
}

/** 포스트 댓글 — 오늘이면 HH:mm (예: 06:00), 아니면 MM/dd */
fun formatPinCommentCreatedAt(
    raw: String,
    zone: ZoneId = ZoneId.systemDefault(),
): String {
    val dateTime = parseFlexibleDateTime(raw, zone) ?: return raw
    return if (dateTime.toLocalDate() == LocalDate.now(zone)) {
        dateTime.format(DateTimeFormatter.ofPattern("HH:mm", Locale.getDefault()))
    } else {
        dateTime.format(DateTimeFormatter.ofPattern("MM/dd", Locale.getDefault()))
    }
}

fun parseFlexibleDateTimeToEpochMilli(
    raw: String,
    zone: ZoneId = ZoneId.systemDefault(),
): Long = parseFlexibleDateTime(raw, zone)?.toInstant()?.toEpochMilli() ?: Long.MIN_VALUE
