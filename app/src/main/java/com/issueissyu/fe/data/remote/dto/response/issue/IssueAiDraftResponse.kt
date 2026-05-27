package com.issueissyu.fe.data.remote.dto.response.issue

import com.issueissyu.fe.domain.model.issue.IssueAiDraft

data class IssueAiDraftResponse(
    val title: String? = null,
    val pinTitle: String? = null,
    val content: String? = null,
    val pinContent: String? = null,
    val description: String? = null,
    val generatedContent: String? = null,
)

fun IssueAiDraftResponse.toDomain(): IssueAiDraft {
    return IssueAiDraft(
        title = title.firstNotBlank(pinTitle),
        content = content.firstNotBlank(pinContent, description, generatedContent),
    )
}

private fun String?.firstNotBlank(vararg candidates: String?): String? {
    return sequenceOf(this, *candidates)
        .firstOrNull { !it.isNullOrBlank() }
}
