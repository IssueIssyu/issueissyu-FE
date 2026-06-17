package com.issueissyu.fe.ui.screens.pindetail

import android.app.Activity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.issueissyu.fe.data.local.TokenManager
import com.issueissyu.fe.domain.model.billing.BillingPurchaseEvent
import com.issueissyu.fe.domain.model.pin.IssuePinDetail
import com.issueissyu.fe.domain.model.pin.IssueResolverParticipation
import com.issueissyu.fe.domain.model.pin.PinDetail
import com.issueissyu.fe.domain.model.pin.Pin
import com.issueissyu.fe.domain.model.pin.PinCategory
import com.issueissyu.fe.domain.model.pin.PinComment
import com.issueissyu.fe.domain.model.pin.PinEmojiCandidate
import com.issueissyu.fe.domain.model.pin.PinPostSympathyContent
import com.issueissyu.fe.domain.model.pin.PinSolveInfo
import com.issueissyu.fe.domain.model.pin.PinUser
import com.issueissyu.fe.domain.model.pin.PetitionStatusInfo
import com.issueissyu.fe.domain.model.pin.ProblemSolverInfo
import com.issueissyu.fe.domain.model.pin.ProblemSolverParticipantInfo
import com.issueissyu.fe.domain.model.issue.IssueReliability
import com.issueissyu.fe.domain.model.issue.IssueReliabilityStatus
import com.issueissyu.fe.domain.model.pin.ResolutionStatus
import com.issueissyu.fe.core.constants.PinImageUploadConstraints
import com.issueissyu.fe.core.issue.IssueReliabilityPolling
import com.issueissyu.fe.domain.model.pin.PinEditRateLimitQuota
import com.issueissyu.fe.domain.model.pin.PinHomeEditSubmitResult
import com.issueissyu.fe.domain.model.pin.PinImageRef
import com.issueissyu.fe.domain.model.pin.UpdatePinEditRequest
import com.issueissyu.fe.domain.model.pin.canEditBy
import com.issueissyu.fe.domain.model.pin.homeEditImageAttachments
import com.issueissyu.fe.domain.model.pin.homeEditMainImageKey
import com.issueissyu.fe.domain.model.pin.supportsHomeEdit
import com.issueissyu.fe.domain.model.pin.toPostSympathyContent
import com.issueissyu.fe.domain.model.pin.withHomeFallback
import com.issueissyu.fe.domain.repository.PinRepository
import com.issueissyu.fe.domain.repository.BillingRepository
import com.issueissyu.fe.domain.repository.IssueRepository
import com.issueissyu.fe.domain.usecase.issue.GetIssueReliabilityUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.Instant
import javax.inject.Inject

enum class PinDetailTab {
    HOME,
    POST,
    RESOLUTION,
}

data class PinHomeEditUiState(
    val isActive: Boolean = false,
    val title: String = "",
    val description: String = "",
    val existingImages: List<PinImageRef> = emptyList(),
    val newImageUris: List<String> = emptyList(),
    val mainImageKey: String? = null,
    val baselineExistingImages: List<PinImageRef> = emptyList(),
    val baselineMainImageKey: String? = null,
    val isSubmitting: Boolean = false,
    val isLoadingQuota: Boolean = false,
    val showConfirmDialog: Boolean = false,
    val rateLimitQuota: PinEditRateLimitQuota? = null,
    val submitFailed: Boolean = false,
    val toneOptions: List<String> = emptyList(),
    val selectedTone: String? = null,
    val isLoadingToneOptions: Boolean = false,
    val isGeneratingAiContent: Boolean = false,
    val isLoadingAiDraftQuota: Boolean = false,
    val showAiDraftConfirmDialog: Boolean = false,
    val aiDraftRateLimitQuota: PinEditRateLimitQuota? = null,
) {
    val photoCount: Int get() = existingImages.size + newImageUris.size

    fun toUpdateRequest(): UpdatePinEditRequest? {
        val trimmedTitle = title.trim()
        val trimmedDescription = description.trim()
        if (trimmedTitle.isBlank() || trimmedDescription.isBlank()) return null
        val mainKey = mainImageKey
        return UpdatePinEditRequest(
            title = trimmedTitle,
            description = trimmedDescription,
            existingImages = existingImages.map { image ->
                image.copy(isMain = image.imageUrl == mainKey)
            },
            newImageUris = newImageUris,
            mainNewImageUri = newImageUris.firstOrNull { it == mainKey },
            baselineExistingImages = baselineExistingImages,
            baselineMainImageKey = baselineMainImageKey,
        )
    }

    fun cleared(quota: PinEditRateLimitQuota? = null) = PinHomeEditUiState(rateLimitQuota = quota)

    fun openedFrom(pin: Pin): PinHomeEditUiState {
        val attachments = pin.homeEditImageAttachments()
        val mainKey = attachments.homeEditMainImageKey()
        return copy(
            isActive = true,
            title = pin.title,
            description = pin.description,
            existingImages = attachments,
            newImageUris = emptyList(),
            mainImageKey = mainKey,
            baselineExistingImages = attachments,
            baselineMainImageKey = mainKey,
            isSubmitting = false,
            isLoadingQuota = false,
            showConfirmDialog = false,
            rateLimitQuota = null,
            submitFailed = false,
        )
    }

    fun withAddedImageUris(uris: List<String>): PinHomeEditUiState {
        if (uris.isEmpty()) return this
        val maxNew = (PinImageUploadConstraints.MAX_COUNT - existingImages.size).coerceAtLeast(0)
        val merged = (newImageUris + uris).distinct().take(maxNew)
        val resolvedMainKey = mainImageKey
            ?: existingImages.homeEditMainImageKey()
            ?: merged.firstOrNull()
        return copy(newImageUris = merged, mainImageKey = resolvedMainKey)
    }

    fun withRemovedExistingImage(imageUrl: String): PinHomeEditUiState {
        val remaining = existingImages.filterNot { it.imageUrl == imageUrl }
        val resolvedMainKey = when (mainImageKey) {
            imageUrl -> remaining.homeEditMainImageKey() ?: newImageUris.firstOrNull()
            else -> mainImageKey
        }
        return copy(existingImages = remaining, mainImageKey = resolvedMainKey)
    }

    fun withRemovedNewImageUri(uri: String): PinHomeEditUiState {
        val remaining = newImageUris.filterNot { it == uri }
        val resolvedMainKey = when (mainImageKey) {
            uri -> existingImages.homeEditMainImageKey() ?: remaining.firstOrNull()
            else -> mainImageKey
        }
        return copy(newImageUris = remaining, mainImageKey = resolvedMainKey)
    }
}

data class PinDetailUiState(
    val isLoading: Boolean = true,
    val isDeleting: Boolean = false,
    val isResolutionJoining: Boolean = false,
    val isResolutionProofSubmitting: Boolean = false,
    val isPetitionSubmitting: Boolean = false,
    val pin: Pin? = null,
    val postSympathy: PinPostSympathyContent? = null,
    val postEmojis: PinDetailPostEmojis = PinDetailPostEmojis(),
    val postComments: List<PinComment> = emptyList(),
    val isCommentsLoading: Boolean = false,
    val isCommentSubmitting: Boolean = false,
    val commentInputRevision: Int = 0,
    val editingCommentId: Long? = null,
    val selectedTab: PinDetailTab = PinDetailTab.HOME,
    val reliabilityScore: Int? = null,
    val reliabilityReason: String? = null,
    val reliabilityStatus: IssueReliabilityStatus? = null,
    val errorMessage: String? = null,
    val homeEdit: PinHomeEditUiState = PinHomeEditUiState(),
)

data class PinDetailEmojiPickerUiState(
    val isVisible: Boolean = false,
    val candidates: List<PinEmojiCandidate> = emptyList(),
    val pickedEmojiId: Long? = null,
    val isLoading: Boolean = false,
    val isSubmitting: Boolean = false,
    val errorMessage: String? = null,
)

sealed interface PinDetailEffect {
    data class ShowToast(val message: String) : PinDetailEffect
}

@HiltViewModel
class PinDetailViewModel @Inject constructor(
    private val pinRepository: PinRepository,
    private val issueRepository: IssueRepository,
    private val billingRepository: BillingRepository,
    private val getIssueReliabilityUseCase: GetIssueReliabilityUseCase,
    private val tokenManager: TokenManager,
) : ViewModel() {

    private val _uiState = MutableStateFlow(PinDetailUiState())
    val uiState: StateFlow<PinDetailUiState> = _uiState.asStateFlow()

    private val _emojiPickerUiState = MutableStateFlow(PinDetailEmojiPickerUiState())
    val emojiPickerUiState: StateFlow<PinDetailEmojiPickerUiState> = _emojiPickerUiState.asStateFlow()

    private val _effect = MutableSharedFlow<PinDetailEffect>(extraBufferCapacity = 8)
    val effect: SharedFlow<PinDetailEffect> = _effect.asSharedFlow()

    private var routePinId: Long? = null
    private var emojiImageById: Map<Long, String> = emptyMap()
    private var issueReliabilityJob: Job? = null
    private var observedReliabilityPinId: Long? = null
    private var observedBillingProductId = billingRepository.pendingBillingProductId.value
    private val homeEditQuotaByPinId = mutableMapOf<Long, PinEditRateLimitQuota>()

    init {
        observeBillingPurchaseEvents()
    }

    private var pendingStartHomeEdit = false

    val currentUserId: String?
        get() = tokenManager.getCurrentUserUuid()

    fun loadPin(pinId: String, startHomeEditAfterLoad: Boolean = false) {
        pendingStartHomeEdit = startHomeEditAfterLoad
        val id = pinId.toLongOrNull() ?: run {
            pendingStartHomeEdit = false
            routePinId = null
            _uiState.update { it.copy(isLoading = false, errorMessage = "잘못된 핀 ID입니다.") }
            return
        }
        routePinId = id
        issueReliabilityJob?.cancel()
        issueReliabilityJob = null
        observedReliabilityPinId = null

        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isLoading = true,
                    errorMessage = null,
                    postSympathy = null,
                    postEmojis = PinDetailPostEmojis(),
                    postComments = emptyList(),
                    isCommentsLoading = false,
                    isResolutionJoining = false,
                    isResolutionProofSubmitting = false,
                    isPetitionSubmitting = false,
                    reliabilityScore = null,
                    reliabilityReason = null,
                    reliabilityStatus = null,
                    homeEdit = PinHomeEditUiState(),
                )
            }

            pinRepository.getPinDetailHome(id)
                .onSuccess { homeResult ->
                    homeResult.editRateLimitQuota?.let { quota ->
                        homeEditQuotaByPinId[id] = quota
                    }
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            pin = homeResult.pin,
                            postSympathy = homeResult.pin.toPostSympathyContent(),
                        )
                    }
                    if (pendingStartHomeEdit) {
                        pendingStartHomeEdit = false
                        startHomeEdit()
                    }
                    loadPostTab(id)
                    if (homeResult.pin.detail is IssuePinDetail) {
                        startIssueReliabilityObservation(id)
                        viewModelScope.launch {
                            refreshResolutionTab(id, currentUserId)
                        }
                    }
                }
                .onFailure { e ->
                    pendingStartHomeEdit = false
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = e.message?.takeIf { msg ->
                                msg.isNotBlank() && !msg.startsWith("HTTP ")
                            } ?: "핀 정보를 불러오지 못했습니다.",
                        )
                    }
                }
        }
    }

    private fun loadPostTab(pinId: Long) {
        viewModelScope.launch {
            pinRepository.getPinDetailPost(pinId).onSuccess { sympathy ->
                val pin = _uiState.value.pin?.takeIf { it.id == pinId.toString() } ?: return@onSuccess
                if (sympathy == null) return@onSuccess
                _uiState.update {
                    it.copy(
                        postSympathy = sympathy.withHomeFallback(
                            fallback = pin.toPostSympathyContent(),
                            layoutCategory = pin.category,
                        ),
                    )
                }
            }
            refreshPostEmojis(pinId)
            loadPinComments(pinId)
        }
    }

    private suspend fun loadPinComments(pinId: Long, showLoading: Boolean = true) {
        if (showLoading) {
            _uiState.update { it.copy(isCommentsLoading = true) }
        }
        pinRepository.getPinComments(pinId)
            .onSuccess { comments ->
                _uiState.update { it.copy(postComments = comments, isCommentsLoading = false) }
            }
            .onFailure { e ->
                _uiState.update { it.copy(isCommentsLoading = false) }
                showToast(e.message ?: "댓글을 불러오지 못했습니다.")
            }
    }

    private suspend fun refreshPostEmojis(pinId: Long): Result<Unit> {
        return pinRepository.getPinEmojis(pinId).fold(
            onSuccess = { data ->
                _uiState.update {
                    it.copy(postEmojis = data.toPinDetailPostEmojis(::lookupEmojiImage))
                }
                Result.success(Unit)
            },
            onFailure = { Result.failure(it) },
        )
    }

    private fun lookupEmojiImage(emojiId: Long): String? = emojiImageById[emojiId]

    fun selectTab(tab: PinDetailTab) {
        if (_uiState.value.homeEdit.isActive) return
        _uiState.update { it.copy(selectedTab = tab) }
        if (tab == PinDetailTab.POST) {
            resolvePinId()?.let { pinId ->
                viewModelScope.launch {
                    refreshPostEmojis(pinId)
                    loadPinComments(pinId)
                }
            }
        } else if (tab == PinDetailTab.RESOLUTION) {
            resolvePinId()?.let { pinId ->
                if (_uiState.value.pin?.detail is IssuePinDetail) {
                    resumeIssueReliabilityObservationIfNeeded(pinId)
                }
                viewModelScope.launch {
                    refreshResolutionTab(pinId, currentUserId)
                }
            }
        }
    }

    private fun resumeIssueReliabilityObservationIfNeeded(pinId: Long) {
        when (_uiState.value.reliabilityStatus) {
            null,
            IssueReliabilityStatus.PENDING,
            -> startIssueReliabilityObservation(pinId, resetUi = false)
            IssueReliabilityStatus.COMPLETED,
            IssueReliabilityStatus.FAILED,
            -> Unit
        }
    }

    private fun startIssueReliabilityObservation(pinId: Long, resetUi: Boolean = true) {
        val isNewPin = observedReliabilityPinId != pinId
        observedReliabilityPinId = pinId
        issueReliabilityJob?.cancel()
        issueReliabilityJob = viewModelScope.launch {
            if (resetUi || isNewPin) {
                _uiState.update {
                    it.copy(
                        reliabilityScore = null,
                        reliabilityReason = null,
                        reliabilityStatus = IssueReliabilityStatus.PENDING,
                    )
                }
            }
            IssueReliabilityPolling.fetchWithPolling(
                fetch = { getIssueReliabilityUseCase(pinId) },
                onUpdate = { reliability -> applyIssueReliability(reliability) },
            )
        }
    }

    private fun applyIssueReliability(reliability: IssueReliability) {
        if (observedReliabilityPinId != reliability.pinId) return
        _uiState.update {
            it.copy(
                reliabilityScore = reliability.score,
                reliabilityReason = reliability.reason,
                reliabilityStatus = reliability.status,
            )
        }
    }

    private suspend fun refreshResolutionTab(pinId: Long, currentUserId: String?) {
        var solveInfo: PinSolveInfo? = null
        var problemSolverInfo: ProblemSolverInfo? = null
        var petitionStatusInfo: PetitionStatusInfo? = null

        pinRepository.getPinSolve(pinId)
            .onSuccess { solveInfo = it }
            .onFailure { e ->
                showToast(e.message ?: "해결하기 정보를 불러오지 못했습니다.")
            }

        pinRepository.getPetition(pinId)
            .onSuccess { petitionStatusInfo = it }
            .onFailure { e ->
                showToast(e.message ?: "청원 현황을 불러오지 못했습니다.")
            }

        if (!currentUserId.isNullOrBlank()) {
            pinRepository.getProblemSolver(pinId, currentUserId)
                .onSuccess { problemSolverInfo = it }
                .onFailure { e ->
                    showToast(e.message ?: "시민해결사 목록을 불러오지 못했습니다.")
                }
        }

        val hasResolutionData = solveInfo != null ||
            problemSolverInfo != null ||
            petitionStatusInfo != null
        if (!hasResolutionData) return

        _uiState.update { state ->
            val currentPin = state.pin ?: return@update state
            val currentUser = buildResolutionCurrentUser(currentPin)
            val resolutionUpdatedPin = currentPin.withResolutionApiState(
                currentUserId = currentUserId,
                currentUser = currentUser,
                solveInfo = solveInfo,
                problemSolverInfo = problemSolverInfo,
            )
            state.copy(
                pin = resolutionUpdatedPin.withPetitionApiState(petitionStatusInfo),
            )
        }
    }

    fun startEditComment(commentId: Long) {
        val exists = _uiState.value.postComments.any { it.commentId == commentId && it.isMine }
        if (!exists) return
        _uiState.update { it.copy(editingCommentId = commentId) }
    }

    fun cancelEditComment() {
        _uiState.update {
            it.copy(
                editingCommentId = null,
                commentInputRevision = it.commentInputRevision + 1,
            )
        }
    }

    fun submitComment(content: String) {
        val trimmed = content.trim()
        if (trimmed.isBlank()) {
            showToast("댓글을 입력해 주세요.")
            return
        }
        if (_uiState.value.isCommentSubmitting) return

        val editingCommentId = _uiState.value.editingCommentId
        if (editingCommentId != null) {
            submitCommentUpdate(editingCommentId, trimmed)
            return
        }

        val pinId = resolvePinId() ?: run {
            showToast("핀 정보를 찾을 수 없습니다.")
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isCommentSubmitting = true) }
            pinRepository.createPinComment(pinId, trimmed)
                .onSuccess {
                    _uiState.update { state ->
                        state.copy(
                            isCommentSubmitting = false,
                            commentInputRevision = state.commentInputRevision + 1,
                        )
                    }
                    loadPinComments(pinId, showLoading = false)
                }
                .onFailure { e ->
                    _uiState.update { it.copy(isCommentSubmitting = false) }
                    showToast(e.message ?: "댓글 작성에 실패했습니다.")
                }
        }
    }

    private fun submitCommentUpdate(commentId: Long, content: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isCommentSubmitting = true) }
            pinRepository.updatePinComment(commentId, content)
                .onSuccess {
                    _uiState.update { state ->
                        state.copy(
                            isCommentSubmitting = false,
                            editingCommentId = null,
                            commentInputRevision = state.commentInputRevision + 1,
                        )
                    }
                    resolvePinId()?.let { loadPinComments(it, showLoading = false) }
                }
                .onFailure { e ->
                    _uiState.update { it.copy(isCommentSubmitting = false) }
                    showToast(e.message ?: "댓글 수정에 실패했습니다.")
                }
        }
    }

    fun deleteComment(commentId: Long) {
        viewModelScope.launch {
            pinRepository.deletePinComment(commentId)
                .onSuccess {
                    _uiState.update { state ->
                        val wasEditing = state.editingCommentId == commentId
                        state.copy(
                            postComments = state.postComments.filterNot { it.commentId == commentId },
                            editingCommentId = if (wasEditing) null else state.editingCommentId,
                            commentInputRevision = if (wasEditing) {
                                state.commentInputRevision + 1
                            } else {
                                state.commentInputRevision
                            },
                        )
                    }
                }
                .onFailure { e -> showToast(e.message ?: "댓글 삭제에 실패했습니다.") }
        }
    }

    fun toggleSympathy() {
        val sympathy = _uiState.value.postSympathy ?: return
        if (sympathy.isSympathizedByMe) return

        viewModelScope.launch {
            pinRepository.likePin(sympathy.pinId)
                .onSuccess { like ->
                    val current = _uiState.value.postSympathy ?: return@onSuccess
                    _uiState.update {
                        it.copy(
                            postSympathy = current.copy(
                                isSympathizedByMe = like.isLike,
                                sympathyCount = like.pinLikeCount,
                            ),
                        )
                    }
                }
                .onFailure { e -> showToast(e.message ?: "공감에 실패했습니다.") }
        }
    }

    fun openEmojiPicker() {
        val myEmojiId = _uiState.value.postEmojis.myEmojiId

        _emojiPickerUiState.value = PinDetailEmojiPickerUiState(
            isVisible = true,
            pickedEmojiId = myEmojiId,
            isLoading = true,
        )

        viewModelScope.launch {
            refreshEmojiCandidates()
        }
    }

    fun closeEmojiPicker() {
        _emojiPickerUiState.value = PinDetailEmojiPickerUiState()
    }

    fun pickEmojiInPicker(emojiId: Long) {
        val candidate = _emojiPickerUiState.value.candidates.firstOrNull { it.emojiId == emojiId }
            ?: return
        if (!candidate.canReact) return
        _emojiPickerUiState.update { state ->
            val nextSelection = if (state.pickedEmojiId == emojiId) null else emojiId
            state.copy(pickedEmojiId = nextSelection)
        }
    }

    fun purchaseEmoji(activity: Activity?, emojiId: Long) {
        val candidate = _emojiPickerUiState.value.candidates.firstOrNull { it.emojiId == emojiId }
            ?: return
        if (candidate.canReact || billingRepository.pendingBillingProductId.value != null) return
        val productId = candidate.productId ?: run {
            showToast("구매 정보를 찾을 수 없습니다.")
            return
        }
        if (activity == null) {
            showToast("결제 화면을 열 수 없습니다.")
            return
        }
        observedBillingProductId = productId
        viewModelScope.launch {
            billingRepository.purchaseProduct(activity, productId)
                .onFailure { error ->
                    observedBillingProductId = null
                    showToast(error.message ?: "결제창을 열지 못했습니다.")
                }
        }
    }

    private fun observeBillingPurchaseEvents() {
        viewModelScope.launch {
            billingRepository.purchaseEvents.collect { event ->
                if (event.productId != observedBillingProductId) return@collect
                when (event) {
                    is BillingPurchaseEvent.Verified -> {
                        refreshEmojiCandidates()
                        showToast("이모지를 구매했습니다.")
                        finishBillingPurchase(event.productId)
                    }
                    is BillingPurchaseEvent.Pending -> {
                        showToast("결제가 대기 중입니다.")
                    }
                    is BillingPurchaseEvent.Canceled -> {
                        finishBillingPurchase(event.productId)
                    }
                    is BillingPurchaseEvent.Failed -> {
                        showToast(event.message)
                        finishBillingPurchase(event.productId)
                    }
                }
            }
        }
    }

    private fun finishBillingPurchase(productId: String) {
        observedBillingProductId = null
        billingRepository.acknowledgePurchaseResult(productId)
    }

    private suspend fun refreshEmojiCandidates() {
        pinRepository.getEmojiCandidates()
            .onSuccess { candidates ->
                emojiImageById = candidates.associate { it.emojiId to it.emojiImageUrl }
                _emojiPickerUiState.update {
                    it.copy(
                        candidates = candidates,
                        isLoading = false,
                        errorMessage = null,
                    )
                }
            }
            .onFailure { e ->
                _emojiPickerUiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = e.message ?: "이모지 목록을 불러오지 못했습니다.",
                    )
                }
            }
    }

    fun submitPickedEmoji() {
        val pinId = resolvePinId() ?: run {
            showToast("핀 정보를 찾을 수 없습니다.")
            return
        }
        if (_emojiPickerUiState.value.isSubmitting) return

        val pickedEmojiId = _emojiPickerUiState.value.pickedEmojiId

        viewModelScope.launch {
            _emojiPickerUiState.update { it.copy(isSubmitting = true) }

            pinRepository.applyPinEmojiFromPicker(pinId, pickedEmojiId)
                .onSuccess {
                    refreshPostEmojis(pinId)
                        .onFailure { e ->
                            showToast(e.message ?: "반응 목록을 갱신하지 못했습니다.")
                        }
                    closeEmojiPicker()
                }
                .onFailure { e ->
                    _emojiPickerUiState.update { it.copy(isSubmitting = false) }
                    showToast(e.message ?: "이모지 반응 처리에 실패했습니다.")
                }
        }
    }

    fun toggleEmojiFromList(emojiId: Long) {
        val pinId = resolvePinId() ?: run {
            showToast("핀 정보를 찾을 수 없습니다.")
            return
        }

        viewModelScope.launch {
            pinRepository.togglePinEmojiFromList(pinId, emojiId)
                .onSuccess {
                    refreshPostEmojis(pinId)
                        .onFailure { e ->
                            showToast(e.message ?: "반응 목록을 갱신하지 못했습니다.")
                        }
                }
                .onFailure { e ->
                    showToast(e.message ?: "이모지 반응 처리에 실패했습니다.")
                }
        }
    }

    fun startHomeEdit() {
        val pin = _uiState.value.pin ?: run {
            showToast("핀 정보를 불러온 뒤 다시 시도해주세요.")
            return
        }
        if (!pin.supportsHomeEdit()) {
            showToast("수정할 수 없는 핀 유형입니다.")
            return
        }
        if (!pin.canEditBy(currentUserId)) {
            showToast("본인이 작성한 핀만 수정할 수 있습니다.")
            return
        }

        _uiState.update {
            it.copy(
                homeEdit = it.homeEdit.openedFrom(pin),
                selectedTab = PinDetailTab.HOME,
            )
        }
        if (pin.detail is IssuePinDetail) {
            loadHomeEditToneTypes()
        }
    }

    private fun loadHomeEditToneTypes() {
        viewModelScope.launch {
            _uiState.update {
                it.copy(homeEdit = it.homeEdit.copy(isLoadingToneOptions = true))
            }
            issueRepository.getIssueToneTypes()
                .onSuccess { tones ->
                    val labels = tones.map { it.label }
                    _uiState.update { state ->
                        state.copy(
                            homeEdit = state.homeEdit.copy(
                                toneOptions = labels,
                                selectedTone = state.homeEdit.selectedTone?.takeIf { it in labels }
                                    ?: labels.firstOrNull(),
                                isLoadingToneOptions = false,
                            ),
                        )
                    }
                }
                .onFailure {
                    _uiState.update { state ->
                        state.copy(
                            homeEdit = state.homeEdit.copy(
                                toneOptions = FALLBACK_AI_TONE_OPTIONS,
                                selectedTone = state.homeEdit.selectedTone?.takeIf { it in FALLBACK_AI_TONE_OPTIONS }
                                    ?: DEFAULT_AI_TONE,
                                isLoadingToneOptions = false,
                            ),
                        )
                    }
                }
        }
    }

    fun cancelHomeEdit() {
        _uiState.update { it.copy(homeEdit = PinHomeEditUiState()) }
    }

    fun dismissHomeEditConfirmDialog() {
        val homeEdit = _uiState.value.homeEdit
        if (homeEdit.isSubmitting || homeEdit.isLoadingQuota) return
        _uiState.update { it.copy(homeEdit = it.homeEdit.copy(showConfirmDialog = false)) }
    }

    fun onHomeEditTitleChange(value: String) {
        if (!_uiState.value.homeEdit.isActive) return
        _uiState.update { it.copy(homeEdit = it.homeEdit.copy(title = value)) }
    }

    fun onHomeEditDescriptionChange(value: String) {
        if (!_uiState.value.homeEdit.isActive) return
        _uiState.update { it.copy(homeEdit = it.homeEdit.copy(description = value)) }
    }

    fun onHomeEditToneChange(value: String) {
        if (!_uiState.value.homeEdit.isActive) return
        _uiState.update { it.copy(homeEdit = it.homeEdit.copy(selectedTone = value)) }
    }

    fun dismissHomeEditAiDraftConfirmDialog() {
        val homeEdit = _uiState.value.homeEdit
        if (homeEdit.isGeneratingAiContent || homeEdit.isLoadingAiDraftQuota) return
        _uiState.update { it.copy(homeEdit = it.homeEdit.copy(showAiDraftConfirmDialog = false)) }
    }

    fun requestHomeEditAiDraft() {
        val state = _uiState.value
        val homeEdit = state.homeEdit
        if (!homeEdit.isActive || homeEdit.isGeneratingAiContent || homeEdit.isLoadingAiDraftQuota) {
            return
        }

        val title = homeEdit.title.trim()
        val description = homeEdit.description.trim()
        when {
            title.isBlank() -> {
                showToast("제목을 먼저 입력해주세요.")
                return
            }
            description.isBlank() -> {
                showToast("상세 설명을 먼저 입력해주세요.")
                return
            }
        }

        viewModelScope.launch {
            _uiState.update {
                it.copy(homeEdit = it.homeEdit.copy(isLoadingAiDraftQuota = true))
            }
            issueRepository.getIssueAiDraftQuota()
                .onSuccess { quota ->
                    if (quota.enabled && quota.remainingCount <= 0) {
                        _uiState.update {
                            it.copy(homeEdit = it.homeEdit.copy(isLoadingAiDraftQuota = false))
                        }
                        showToast("AI 글쓰기 일일 횟수를 초과했습니다.")
                        return@launch
                    }
                    _uiState.update {
                        it.copy(
                            homeEdit = it.homeEdit.copy(
                                isLoadingAiDraftQuota = false,
                                aiDraftRateLimitQuota = quota,
                                showAiDraftConfirmDialog = true,
                            ),
                        )
                    }
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(homeEdit = it.homeEdit.copy(isLoadingAiDraftQuota = false))
                    }
                    showToast(error.message ?: "AI 글쓰기 제한 횟수 조회에 실패했습니다.")
                }
        }
    }

    fun confirmHomeEditAiDraft() {
        val state = _uiState.value
        if (!state.homeEdit.isActive || state.homeEdit.isGeneratingAiContent) return

        val pin = state.pin ?: run {
            showToast("핀 정보를 불러온 뒤 다시 시도해주세요.")
            return
        }

        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    homeEdit = it.homeEdit.copy(
                        isGeneratingAiContent = true,
                        showAiDraftConfirmDialog = false,
                    ),
                )
            }
            issueRepository.createIssueAiDraft(
                title = state.homeEdit.title.trim(),
                content = state.homeEdit.description.trim(),
                tone = state.homeEdit.selectedTone ?: DEFAULT_AI_TONE,
                latitude = pin.coordinate.latitude,
                longitude = pin.coordinate.longitude,
            ).onSuccess { draft ->
                _uiState.update {
                    it.copy(
                        homeEdit = it.homeEdit.copy(
                            title = draft.title?.takeIf { title -> title.isNotBlank() } ?: it.homeEdit.title,
                            description = draft.content.orEmpty(),
                            isGeneratingAiContent = false,
                            aiDraftRateLimitQuota = null,
                        ),
                    )
                }
            }.onFailure { error ->
                _uiState.update {
                    it.copy(homeEdit = it.homeEdit.copy(isGeneratingAiContent = false))
                }
                showToast(error.message ?: "AI 글쓰기에 실패했습니다.")
            }
        }
    }

    fun addHomeEditImageUris(uris: List<String>) {
        if (!_uiState.value.homeEdit.isActive || uris.isEmpty()) return
        _uiState.update { state ->
            state.copy(homeEdit = state.homeEdit.withAddedImageUris(uris))
        }
    }

    fun removeHomeEditExistingImage(imageUrl: String) {
        if (!_uiState.value.homeEdit.isActive) return
        _uiState.update { state ->
            state.copy(homeEdit = state.homeEdit.withRemovedExistingImage(imageUrl))
        }
    }

    fun removeHomeEditNewImageUri(uri: String) {
        if (!_uiState.value.homeEdit.isActive) return
        _uiState.update { state ->
            state.copy(homeEdit = state.homeEdit.withRemovedNewImageUri(uri))
        }
    }

    fun setHomeEditMainImage(key: String) {
        if (!_uiState.value.homeEdit.isActive) return
        _uiState.update { it.copy(homeEdit = it.homeEdit.copy(mainImageKey = key)) }
    }

    fun requestHomeEditSubmit() {
        val state = _uiState.value
        val homeEdit = state.homeEdit
        if (!homeEdit.isActive || homeEdit.isSubmitting || homeEdit.isLoadingQuota) return

        if (homeEdit.title.trim().isBlank()) {
            showToast("제목을 입력해주세요.")
            return
        }
        if (homeEdit.description.trim().isBlank()) {
            showToast("상세 설명을 입력해주세요.")
            return
        }

        val pinId = resolvePinId() ?: return
        val pin = state.pin ?: return

        if (pin.category == PinCategory.COMMUNICATION) {
            confirmHomeEditSubmit()
            return
        }

        viewModelScope.launch {
            _uiState.update {
                it.copy(homeEdit = it.homeEdit.copy(isLoadingQuota = true))
            }
            pinRepository.getIssuePinEditQuota(pinId)
                .onSuccess { quota ->
                    homeEditQuotaByPinId[pinId] = quota
                    if (quota.enabled && quota.remainingCount <= 0) {
                        _uiState.update {
                            it.copy(homeEdit = it.homeEdit.copy(isLoadingQuota = false))
                        }
                        showToast("이 핀의 일일 수정 횟수를 초과했습니다.")
                        return@launch
                    }
                    _uiState.update {
                        it.copy(
                            homeEdit = it.homeEdit.copy(
                                isLoadingQuota = false,
                                rateLimitQuota = quota,
                                showConfirmDialog = true,
                            ),
                        )
                    }
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(homeEdit = it.homeEdit.copy(isLoadingQuota = false))
                    }
                    showToast(error.message ?: "이슈 핀 수정 제한 횟수 조회에 실패했습니다.")
                }
        }
    }

    fun confirmHomeEditSubmit() {
        val state = _uiState.value
        val homeEdit = state.homeEdit
        if (!homeEdit.isActive || homeEdit.isSubmitting) return
        val pinId = resolvePinId() ?: return
        val pin = state.pin ?: return
        val editRequest = homeEdit.toUpdateRequest() ?: run {
            showToast("제목과 상세 설명을 입력해주세요.")
            return
        }

        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    homeEdit = it.homeEdit.copy(
                        isSubmitting = true,
                        showConfirmDialog = false,
                    ),
                )
            }

            pinRepository.updatePinHomeEdit(
                pinId = pinId,
                category = pin.category,
                request = editRequest,
            ).onSuccess { result ->
                when (result) {
                    is PinHomeEditSubmitResult.Issue -> {
                        result.result.rateLimitQuota?.let { quota ->
                            homeEditQuotaByPinId[pinId] = quota
                        }
                        finishHomeEditSuccess(
                            pinId = pinId,
                            isCommunicationPin = false,
                            rateLimitQuota = result.result.rateLimitQuota,
                        )
                    }

                    is PinHomeEditSubmitResult.Communication -> {
                        finishHomeEditSuccess(pinId, isCommunicationPin = true)
                    }
                }
            }.onFailure { error ->
                finishHomeEditFailure(
                    error = error,
                    isCommunicationPin = pin.category == PinCategory.COMMUNICATION,
                )
            }
        }
    }

    private fun finishHomeEditSuccess(
        pinId: Long,
        isCommunicationPin: Boolean,
        rateLimitQuota: PinEditRateLimitQuota? = null,
    ) {
        _uiState.update {
            it.copy(homeEdit = it.homeEdit.cleared(quota = rateLimitQuota))
        }
        refreshPinDetail(pinId)
        showToast(
            if (isCommunicationPin) "소통 핀이 수정되었습니다." else "이슈 핀이 수정되었습니다.",
        )
    }

    private fun finishHomeEditFailure(
        error: Throwable,
        isCommunicationPin: Boolean,
    ) {
        _uiState.update {
            it.copy(
                homeEdit = it.homeEdit.copy(
                    isSubmitting = false,
                    showConfirmDialog = false,
                    submitFailed = true,
                ),
            )
        }
        showToast(
            error.message ?: if (isCommunicationPin) {
                "소통 핀 수정에 실패했습니다."
            } else {
                "이슈 핀 수정에 실패했습니다."
            },
        )
    }

    fun submitHomeEdit() {
        requestHomeEditSubmit()
    }

    private fun refreshPinDetail(pinId: Long) {
        viewModelScope.launch {
            pinRepository.getPinDetailHome(pinId)
                .onSuccess { homeResult ->
                    homeResult.editRateLimitQuota?.let { quota ->
                        homeEditQuotaByPinId[pinId] = quota
                    }
                    _uiState.update {
                        it.copy(
                            pin = homeResult.pin,
                            postSympathy = homeResult.pin.toPostSympathyContent(),
                        )
                    }
                    loadPostTab(pinId)
                    if (homeResult.pin.detail is IssuePinDetail) {
                        startIssueReliabilityObservation(pinId, resetUi = false)
                        viewModelScope.launch {
                            refreshResolutionTab(pinId, currentUserId)
                        }
                    }
                }
                .onFailure { error ->
                    showToast(error.message ?: "핀 정보를 다시 불러오지 못했습니다.")
                }
        }
    }

    fun deletePin(pinId: String, onSuccess: () -> Unit) {
        if (_uiState.value.isDeleting) return
        val id = pinId.toLongOrNull() ?: return

        viewModelScope.launch {
            _uiState.update { it.copy(isDeleting = true) }
            pinRepository.deletePin(id)
                .onSuccess {
                    _uiState.update { it.copy(isDeleting = false) }
                    onSuccess()
                }
                .onFailure { e ->
                    _uiState.update { it.copy(isDeleting = false) }
                    showToast(e.message ?: "핀 삭제에 실패했습니다.")
                }
        }
    }

    fun joinProblemSolver(onSuccess: () -> Unit = {}) {
        val currentUserId = resolveRequiredCurrentUserId() ?: return
        val pinId = resolvePinId() ?: run {
            showToast("핀 정보를 찾을 수 없습니다.")
            return
        }
        val currentPin = _uiState.value.pin ?: run {
            showToast("핀 정보를 찾을 수 없습니다.")
            return
        }
        val issueDetail = currentPin.detail as? IssuePinDetail ?: run {
            showToast("해결 참여는 이슈 핀에서만 가능합니다.")
            return
        }
        if (_uiState.value.isResolutionJoining) return
        if (issueDetail.writer.id == currentUserId) {
            showToast("작성자는 시민해결사로 참여할 수 없습니다.")
            return
        }
        if (issueDetail.isProblemSolverByMe || issueDetail.myProblemSolverId != null) {
            showToast("이미 참여 중입니다.")
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isResolutionJoining = true) }
            val result = pinRepository.joinProblemSolver(pinId)
            result
                .onSuccess { joined ->
                    val joinedAt = Instant.now().toString()
                    _uiState.update { state ->
                        state.copy(
                            isResolutionJoining = false,
                            pin = state.pin?.withJoinedProblemSolver(
                                currentUser = buildResolutionCurrentUser(state.pin)
                                    ?: PinUser(
                                        id = currentUserId,
                                        name = resolveCurrentUserName(),
                                        imageUrl = null,
                                    ),
                                joinedAt = joinedAt,
                                problemSolverId = joined.problemSolverId,
                                problemSolveState = joined.problemSolveState,
                            ),
                        )
                    }
                    onSuccess()
                }
                .onFailure { e ->
                    _uiState.update { it.copy(isResolutionJoining = false) }
                    showToast(e.message ?: "시민해결사 참여에 실패했습니다.")
                }
            if (result.isSuccess) {
                refreshResolutionTab(pinId, currentUserId)
            }
        }
    }

    fun submitProblemSolverPhoto(
        imageUri: String,
        onSuccess: () -> Unit = {},
    ) {
        val currentUserId = resolveRequiredCurrentUserId() ?: return
        val pinId = resolvePinId() ?: run {
            showToast("핀 정보를 찾을 수 없습니다.")
            return
        }
        val currentPin = _uiState.value.pin ?: run {
            showToast("핀 정보를 찾을 수 없습니다.")
            return
        }
        val issueDetail = currentPin.detail as? IssuePinDetail ?: run {
            showToast("해결 인증은 이슈 핀에서만 가능합니다.")
            return
        }
        if (_uiState.value.isResolutionProofSubmitting) return
        if (!issueDetail.isProblemSolverByMe && issueDetail.myProblemSolverId == null) {
            showToast("참여한 시민해결사만 사진 인증을 할 수 있어요.")
            return
        }
        val problemSolverId = issueDetail.myProblemSolverId
            ?: issueDetail.resolverParticipations
                .firstOrNull { it.user.id == currentUserId }
                ?.problemSolverId
            ?: run {
            showToast("시민해결사 인증 정보를 찾을 수 없습니다.")
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isResolutionProofSubmitting = true) }
            val result = pinRepository.photoProblemSolver(problemSolverId, imageUri)
            result
                .onSuccess { photoInfo ->
                    val submittedAt = Instant.now().toString()
                    _uiState.update { state ->
                        state.copy(
                            isResolutionProofSubmitting = false,
                            pin = state.pin?.withSubmittedProblemSolverPhoto(
                                currentUserId = currentUserId,
                                imageUri = photoInfo.photoUrl.ifBlank { imageUri },
                                submittedAt = submittedAt,
                                problemSolveState = photoInfo.problemSolveState,
                            ),
                        )
                    }
                    showToast("사진 인증이 등록되었습니다.")
                    onSuccess()
                }
                .onFailure { e ->
                    _uiState.update { it.copy(isResolutionProofSubmitting = false) }
                    showToast(e.message ?: "사진 인증 등록에 실패했습니다.")
                }
            if (result.isSuccess) {
                refreshResolutionTab(pinId, currentUserId)
            }
        }
    }

    fun verifyProblemSolver(problemSolverId: Long) {
        val currentUserId = resolveRequiredCurrentUserId() ?: return
        val pinId = resolvePinId() ?: run {
            showToast("핀 정보를 찾을 수 없습니다.")
            return
        }
        val currentPin = _uiState.value.pin ?: run {
            showToast("핀 정보를 찾을 수 없습니다.")
            return
        }
        val issueDetail = currentPin.detail as? IssuePinDetail ?: run {
            showToast("해결 인증은 이슈 핀에서만 가능합니다.")
            return
        }
        if (issueDetail.writer.id != currentUserId) {
            showToast("작성자만 시민해결사 인증을 완료할 수 있어요.")
            return
        }
        if (issueDetail.resolutionStatus == ResolutionStatus.RESOLVED) {
            showToast("이미 해결 완료된 핀입니다.")
            return
        }
        val targetParticipation = issueDetail.resolverParticipations
            .firstOrNull { it.problemSolverId == problemSolverId } ?: run {
            showToast("선택한 시민해결사를 찾을 수 없습니다.")
            return
        }
        if (targetParticipation.proofImageUrls.isEmpty()) {
            showToast("사진 인증이 등록된 참여자만 확인할 수 있어요.")
            return
        }

        viewModelScope.launch {
            val result = pinRepository.verificationProblemSolver(problemSolverId)
            result
                .onSuccess {
                    val confirmedAt = Instant.now().toString()
                    _uiState.update { state ->
                        state.copy(
                            pin = state.pin?.withVerifiedProblemSolver(
                                problemSolverId = problemSolverId,
                                confirmedAt = confirmedAt,
                            ),
                        )
                    }
                    showToast("시민해결사 인증을 완료했습니다.")
                }
                .onFailure { e ->
                    showToast(e.message ?: "시민해결사 인증에 실패했습니다.")
                }
            if (result.isSuccess) {
                refreshResolutionTab(pinId, currentUserId)
            }
        }
    }

    fun joinPetition() {
        val pinId = resolvePinId() ?: run {
            showToast("핀 정보를 찾을 수 없습니다.")
            return
        }
        val currentPin = _uiState.value.pin ?: run {
            showToast("핀 정보를 찾을 수 없습니다.")
            return
        }
        val issueDetail = currentPin.detail as? IssuePinDetail ?: run {
            showToast("청원은 이슈 핀에서만 가능합니다.")
            return
        }
        if (_uiState.value.isPetitionSubmitting) return
        if (issueDetail.isPetitionedByMe) {
            showToast("이미 청원되었습니다.")
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isPetitionSubmitting = true) }
            pinRepository.joinPetition(pinId)
                .onSuccess { petitionInfo ->
                    _uiState.update { state ->
                        state.copy(
                            isPetitionSubmitting = false,
                            pin = state.pin?.withPetitionJoinState(
                                petitionCount = petitionInfo.petitionCount,
                                isPetitioned = petitionInfo.isPetitioned,
                            ),
                        )
                    }
                    showToast("청원에 성공했습니다.")
                }
                .onFailure { e ->
                    _uiState.update { it.copy(isPetitionSubmitting = false) }
                    showToast(e.message ?: "청원에 실패했습니다.")
                }
        }
    }

    private fun resolveRequiredCurrentUserId(): String? {
        return tokenManager.getCurrentUserUuid()?.takeIf { it.isNotBlank() } ?: run {
            showToast("로그인 정보를 찾을 수 없습니다. 다시 로그인해 주세요.")
            null
        }
    }

    fun resolveCurrentUserName(): String {
        return tokenManager.getCurrentUserName()
            ?.takeIf { it.isNotBlank() }
            ?: "현재 사용자"
    }

    private fun resolvePinId(): Long? {
        return routePinId
            ?: _uiState.value.postSympathy?.pinId?.takeIf { it > 0L }
            ?: _uiState.value.pin?.id?.toLongOrNull()
    }

    private fun showToast(message: String) {
        _effect.tryEmit(PinDetailEffect.ShowToast(message))
    }

    companion object {
        private const val DEFAULT_AI_TONE = "없음"
        private val FALLBACK_AI_TONE_OPTIONS = listOf(
            "없음",
            "한줄요약형",
            "상황설명형",
            "개선요청형",
            "긴급요청형",
            "불편호소형",
        )
    }
}

private fun Pin.withSubmittedProblemSolverPhoto(
    currentUserId: String,
    imageUri: String,
    submittedAt: String,
    problemSolveState: String,
): Pin {
    val issueDetail = detail as? IssuePinDetail ?: return this
    val updatedParticipations = issueDetail.resolverParticipations.map { participation ->
        if (participation.user.id != currentUserId) {
            participation
        } else {
            participation.copy(
                proofImageUrls = listOf(imageUri),
                proofSubmittedAt = submittedAt,
            )
        }
    }
    return copy(
        detail = issueDetail.copy(
            myProblemSolveState = problemSolveState,
            resolverParticipations = updatedParticipations,
        ) as PinDetail
    )
}

private fun Pin.withJoinedProblemSolver(
    currentUser: PinUser,
    joinedAt: String,
    problemSolverId: Long,
    problemSolveState: String,
): Pin {
    val issueDetail = detail as? IssuePinDetail ?: return this
    if (issueDetail.resolverParticipations.any { it.user.id == currentUser.id }) return this

    return copy(
        detail = issueDetail.copy(
            isProblemSolverByMe = true,
            myProblemSolverId = problemSolverId,
            myProblemSolveState = problemSolveState,
            resolutionStatus = if (issueDetail.resolutionStatus == ResolutionStatus.BEFORE_RESOLUTION) {
                ResolutionStatus.IN_PROGRESS
            } else {
                issueDetail.resolutionStatus
            },
            resolverParticipations = issueDetail.resolverParticipations + IssueResolverParticipation(
                problemSolverId = problemSolverId,
                user = currentUser,
                joinedAt = joinedAt,
            ),
        ) as PinDetail
    )
}

private fun Pin.withVerifiedProblemSolver(
    problemSolverId: Long,
    confirmedAt: String,
): Pin {
    val issueDetail = detail as? IssuePinDetail ?: return this
    val targetParticipation = issueDetail.resolverParticipations
        .firstOrNull { it.problemSolverId == problemSolverId } ?: return this

    val updatedParticipations = issueDetail.resolverParticipations.map { participation ->
        if (participation.problemSolverId != problemSolverId) {
            participation
        } else {
            participation.copy(
                isConfirmedByWriter = true,
                confirmedAt = confirmedAt,
            )
        }
    }

    return copy(
        detail = issueDetail.copy(
            resolutionStatus = ResolutionStatus.RESOLVED,
            resolverParticipations = updatedParticipations,
            resolvedBy = targetParticipation.user,
            resolvedAt = confirmedAt,
        ) as PinDetail,
    )
}

private fun Pin.withPetitionApiState(
    petitionStatusInfo: PetitionStatusInfo?,
): Pin {
    if (petitionStatusInfo == null) return this
    val issueDetail = detail as? IssuePinDetail ?: return this
    return copy(
        detail = issueDetail.copy(
            petitionCount = petitionStatusInfo.petitionCount,
            isPetitionedByMe = petitionStatusInfo.isPetitioned,
            petitionTargetCount = petitionStatusInfo.targetPetitionCount,
        ) as PinDetail,
    )
}

private fun Pin.withPetitionJoinState(
    petitionCount: Int,
    isPetitioned: Boolean,
): Pin {
    val issueDetail = detail as? IssuePinDetail ?: return this
    return copy(
        detail = issueDetail.copy(
            petitionCount = petitionCount,
            isPetitionedByMe = isPetitioned,
        ) as PinDetail,
    )
}

private fun Pin.withResolutionApiState(
    currentUserId: String?,
    currentUser: PinUser?,
    solveInfo: PinSolveInfo?,
    problemSolverInfo: ProblemSolverInfo?,
): Pin {
    val issueDetail = detail as? IssuePinDetail ?: return this
    val knownMyProblemSolverId = solveInfo?.userProblemSolverId
        ?: issueDetail.myProblemSolverId
        ?: issueDetail.resolverParticipations
            .firstOrNull { it.user.id == currentUserId }
            ?.problemSolverId
    val nextParticipations = problemSolverInfo?.problemSolvers?.map { solver ->
        solver.toIssueResolverParticipation(
            currentUserId = currentUserId,
            currentUser = currentUser,
            knownMyProblemSolverId = knownMyProblemSolverId,
        )
    } ?: issueDetail.resolverParticipations
    val myProblemSolverInfo = problemSolverInfo?.problemSolvers
        ?.firstOrNull { it.problemSolverId == knownMyProblemSolverId }
    val resolvedParticipation = nextParticipations.firstOrNull { it.isConfirmedByWriter }
    val isMyProblemSolver = solveInfo?.userProblemSolverId != null
    val nextResolutionStatus = when {
        resolvedParticipation != null -> ResolutionStatus.RESOLVED
        nextParticipations.isNotEmpty() || isMyProblemSolver -> ResolutionStatus.IN_PROGRESS
        else -> issueDetail.resolutionStatus
    }
    return copy(
        detail = issueDetail.copy(
            resolutionStatus = nextResolutionStatus,
            isProblemSolverByMe = when {
                solveInfo != null -> isMyProblemSolver
                problemSolverInfo != null -> problemSolverInfo.isGoNow
                else -> issueDetail.isProblemSolverByMe
            },
            myProblemSolverId = knownMyProblemSolverId,
            myProblemSolveState = solveInfo?.userProblemSolveState
                ?: myProblemSolverInfo?.problemSolveState
                ?: issueDetail.myProblemSolveState,
            resolverParticipations = nextParticipations,
            resolvedBy = resolvedParticipation?.user ?: issueDetail.resolvedBy,
            resolvedAt = resolvedParticipation?.confirmedAt ?: issueDetail.resolvedAt,
            isPetitionedByMe = solveInfo?.isPetitioned ?: issueDetail.isPetitionedByMe,
        ) as PinDetail,
    )
}

private fun ProblemSolverParticipantInfo.toIssueResolverParticipation(
    currentUserId: String?,
    currentUser: PinUser?,
    knownMyProblemSolverId: Long?,
): IssueResolverParticipation {
    val isCurrentUser = knownMyProblemSolverId != null && problemSolverId == knownMyProblemSolverId
    val user = if (isCurrentUser && currentUser != null && currentUserId != null) {
        currentUser.copy(
            name = nickname.ifBlank { currentUser.name },
            imageUrl = profileUrl ?: currentUser.imageUrl,
        )
    } else {
        PinUser(
            id = problemSolverId.toString(),
            name = nickname,
            imageUrl = profileUrl,
        )
    }
    val normalizedState = problemSolveState.trim().uppercase()
    return IssueResolverParticipation(
        problemSolverId = problemSolverId,
        user = if (isCurrentUser && currentUserId != null) {
            user.copy(id = currentUserId)
        } else {
            user
        },
        joinedAt = createdAt,
        proofImageUrls = problemSolverImageUrl?.takeIf { it.isNotBlank() }?.let(::listOf).orEmpty(),
        proofSubmittedAt = if (normalizedState == "VERIFIED" || normalizedState == "RESOLVED") {
            createdAt
        } else {
            null
        },
        isConfirmedByWriter = normalizedState == "RESOLVED",
        confirmedAt = if (normalizedState == "RESOLVED") createdAt else null,
    )
}

private fun PinDetailViewModel.buildResolutionCurrentUser(existingPin: Pin?): PinUser? {
    val resolvedCurrentUserId = currentUserId ?: return null
    val existingDetail = existingPin?.detail as? IssuePinDetail
    val existingUser = existingDetail?.resolverParticipations
        ?.firstOrNull { it.user.id == resolvedCurrentUserId }
        ?.user
    if (existingUser != null) return existingUser

    return PinUser(
        id = resolvedCurrentUserId,
        name = resolveCurrentUserName(),
        imageUrl = null,
    )
}
