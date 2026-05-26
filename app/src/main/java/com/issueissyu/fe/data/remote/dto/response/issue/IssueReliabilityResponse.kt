package com.issueissyu.fe.data.remote.dto.response.issue

import com.issueissyu.fe.domain.model.issue.IssueReliability
import com.issueissyu.fe.domain.model.issue.IssueReliabilityStatus
import kotlin.math.roundToInt

data class IssueReliabilityResponse(
    val issuePinId: Long,
    val pinId: Long,
    val issueConfidence: Double?,
    val confidenceContent: String?,
    val reliabilityStatus: String?,
    val imageUploadStatus: String?,
)

fun IssueReliabilityResponse.toDomain(): IssueReliability {
    return IssueReliability(
        issuePinId = issuePinId,
        pinId = pinId,
        score = issueConfidence?.let { (it * 100).roundToInt().coerceIn(0, 100) },
        reason = confidenceContent,
        status = reliabilityStatus.toIssueReliabilityStatus(),
        imageUploadStatus = imageUploadStatus,
    )
}

private fun String?.toIssueReliabilityStatus(): IssueReliabilityStatus {
    return when {
        this.equals("completed", ignoreCase = true) -> IssueReliabilityStatus.COMPLETED
        this.equals("failed", ignoreCase = true) -> IssueReliabilityStatus.FAILED
        else -> IssueReliabilityStatus.PENDING
    }
}
