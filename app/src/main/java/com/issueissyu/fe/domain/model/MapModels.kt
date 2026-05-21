package com.issueissyu.fe.domain.model

import com.issueissyu.fe.domain.model.pin.ResolutionStatus

data class MapNotice(
    val id: String,
    val pinId: String?,
    val content: String,
)

data class PatchNote(
    val id: String,
    val title: String,
    val viewCount: Int,
    val locationName: String,
    val writerName: String,
    val writerImageUrl: String?,
    val resolutionStatus: ResolutionStatus,
    val createdAt: String?,
)

data class PatchNotePage(
    val items: List<PatchNote>,
    val hasNext: Boolean,
    val nextCursor: String?,
)
