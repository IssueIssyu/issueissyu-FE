package com.issueissyu.fe.domain.model.issue

data class IssueReliability(
    val issuePinId: Long,
    val pinId: Long,
    val score: Int?,
    val reason: String?,
    val status: IssueReliabilityStatus,
    val imageUploadStatus: String?,
)

enum class IssueReliabilityStatus {
    PENDING,
    COMPLETED,
    FAILED,
}
