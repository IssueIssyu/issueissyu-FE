package com.issueissyu.fe.data.remote.dto.mypage

import com.issueissyu.fe.data.remote.dto.response.mypage.GetMyIssueResponse
import com.issueissyu.fe.data.remote.dto.response.mypage.PinInfo
import com.issueissyu.fe.domain.model.mypage.MyIssuePage
import com.issueissyu.fe.domain.model.mypage.MyIssuePin
import com.issueissyu.fe.domain.model.pin.PinCategory
import com.issueissyu.fe.domain.model.pin.ResolutionStatus

fun GetMyIssueResponse.toMyIssuePage(): MyIssuePage {
    return MyIssuePage(
        items = pins.map { it.toMyIssuePin() },
        hasNext = pageInfo.hasNext,
        nextCursor = pageInfo.nextCursor,
    )
}

private fun PinInfo.toMyIssuePin(): MyIssuePin {
    val category = pinType.toMyIssuePinCategory()
    return MyIssuePin(
        pinId = pinId,
        pinType = category,
        title = pinTitle,
        address = pinDetailAddress,
        createdAt = createdAt,
        resolutionStatus = issuePinState.toMyIssueResolutionStatus(category),
    )
}

private fun String.toMyIssuePinCategory(): PinCategory {
    return when (trim().uppercase()) {
        "COMMUNICATION" -> PinCategory.COMMUNICATION
        else -> PinCategory.ISSUE
    }
}

//내 핀 조회
private fun String?.toMyIssueResolutionStatus(category: PinCategory): ResolutionStatus? {
    if (!isNullOrBlank()) {
        return trim().uppercase().toMyIssueResolutionStatus()
    }
    if (category == PinCategory.COMMUNICATION) {
        return null
    }
    return ResolutionStatus.BEFORE_RESOLUTION
}

private fun String.toMyIssueResolutionStatus(): ResolutionStatus {
    return when (this) {
        "IN_PROGRESS", "PROGRESS", "RESOLVING" -> ResolutionStatus.IN_PROGRESS
        "RESOLVED", "DONE" -> ResolutionStatus.RESOLVED
        "BEFORE_PROGRESS", "BEFORE_RESOLUTION", "BEFORE", "READY" -> ResolutionStatus.BEFORE_RESOLUTION
        else -> ResolutionStatus.BEFORE_RESOLUTION
    }
}
