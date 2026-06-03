package com.issueissyu.fe.ui.screens.mypage

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.issueissyu.fe.domain.model.collection.CollectionPageSummary
import com.issueissyu.fe.domain.repository.UserCollectionsStore
import com.issueissyu.fe.domain.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileChangeViewModel @Inject constructor(
    private val userCollectionsStore: UserCollectionsStore,
    private val userRepository: UserRepository,
) : ViewModel() {

    private val _currentNickname = MutableStateFlow("")
    val currentNickname = _currentNickname.asStateFlow()

    private val _profileImageUrl = MutableStateFlow<String?>(null)
    val profileImageUrl = _profileImageUrl.asStateFlow()

    private val _isLoadingProfile = MutableStateFlow(true)
    val isLoadingProfile = _isLoadingProfile.asStateFlow()

    private val _profileLoadErrorMessage = MutableStateFlow<String?>(null)
    val profileLoadErrorMessage = _profileLoadErrorMessage.asStateFlow()

    private val _inputNickname = MutableStateFlow("")
    val inputNickname = _inputNickname.asStateFlow()

    private val _isNicknameAvailable = MutableStateFlow<Boolean?>(null)
    val isNicknameAvailable = _isNicknameAvailable.asStateFlow()

    private val _isCheckingNickname = MutableStateFlow(false)
    val isCheckingNickname = _isCheckingNickname.asStateFlow()

    private val _isCompleteEnabled = MutableStateFlow(false)
    val isCompleteEnabled = _isCompleteEnabled.asStateFlow()

    private val _isCheckButtonEnabled = MutableStateFlow(false)
    val isCheckButtonEnabled = _isCheckButtonEnabled.asStateFlow()

    private val _isUpdating = MutableStateFlow(false)
    val isUpdating = _isUpdating.asStateFlow()

    private val _showSnackbar = MutableSharedFlow<String>()
    val showSnackbar = _showSnackbar.asSharedFlow()

    private var shouldRefreshFromCollection = false
    private var myPageRefreshPending = false
    private var loadProfileJob: Job? = null

    init {
        loadProfile()
    }

    fun loadProfile() {
        loadProfileJob?.cancel()
        loadProfileJob = viewModelScope.launch {
            val hasCachedData = _currentNickname.value.isNotBlank() || _profileImageUrl.value != null
            _isLoadingProfile.value = !hasCachedData
            _profileLoadErrorMessage.value = null

            userCollectionsStore.refreshForMyPage(force = false)
                .onSuccess { summary -> applyProfileFromSummary(summary) }
                .onFailure { error ->
                    _isLoadingProfile.value = false
                    _profileLoadErrorMessage.value =
                        error.message ?: LOAD_PROFILE_ERROR_MESSAGE
                }
        }
    }

    fun openCollection() {
        shouldRefreshFromCollection = true
    }

    fun onScreenResume() {
        if (!shouldRefreshFromCollection) return
        shouldRefreshFromCollection = false
        syncProfileFromStore()
    }

    private fun syncProfileFromStore() {
        val summary = userCollectionsStore.snapshot.value
        if (summary != null) {
            applyProfileFromSummary(summary, trackImageChange = true)
            return
        }

        loadProfileJob?.cancel()
        loadProfileJob = viewModelScope.launch {
            userCollectionsStore.refreshForMyPage(force = true)
                .onSuccess { applyProfileFromSummary(it, trackImageChange = true) }
                .onFailure { error ->
                    _showSnackbar.emit(error.message ?: LOAD_PROFILE_ERROR_MESSAGE)
                }
        }
    }

    private fun applyProfileFromSummary(
        summary: CollectionPageSummary,
        trackImageChange: Boolean = false,
    ) {
        val previousProfileImageUrl = _profileImageUrl.value
        val previousNickname = _currentNickname.value

        _currentNickname.value = summary.nickname
        if (_inputNickname.value == previousNickname || _inputNickname.value.isBlank()) {
            _inputNickname.value = summary.nickname
        }
        _profileImageUrl.value = summary.profileImageUrl
        _isLoadingProfile.value = false
        _profileLoadErrorMessage.value = null
        _isNicknameAvailable.value = null

        if (trackImageChange && summary.profileImageUrl != previousProfileImageUrl) {
            myPageRefreshPending = true
        }
        updateCompleteButtonState()
    }

    fun consumeMyPageRefreshPending(): Boolean {
        val pending = myPageRefreshPending
        myPageRefreshPending = false
        return pending
    }

    fun onNicknameChange(nickname: String) {
        _inputNickname.value = nickname
        _isNicknameAvailable.value = null
        updateCompleteButtonState()
    }

    fun checkNicknameDuplicate() {
        val requestedNickname = _inputNickname.value
        if (_inputNickname.value.isBlank()) return

        viewModelScope.launch {
            _isCheckingNickname.value = true

            try {
                val isAvailable = userRepository.checkNicknameDuplicate(requestedNickname)
                if (_inputNickname.value == requestedNickname) {
                    _isNicknameAvailable.value = isAvailable
                }
            } catch (e: Exception) {
                if (_inputNickname.value == requestedNickname) {
                    _isNicknameAvailable.value = null
                    _showSnackbar.emit(
                        e.message ?: "닉네임 중복 확인에 실패했습니다",
                    )
                }
            } finally {
                _isCheckingNickname.value = false
                updateCompleteButtonState()
            }
        }
    }

    fun updateProfile(onSuccess: () -> Unit) {
        if (_isUpdating.value) return

        viewModelScope.launch {
            _isUpdating.value = true
            try {
                userRepository.updateNickname(_inputNickname.value)
                _currentNickname.value = _inputNickname.value
                userCollectionsStore.patchNickname(_inputNickname.value)
                myPageRefreshPending = true
                onSuccess()
            } catch (e: Exception) {
                _showSnackbar.emit(e.message ?: "프로필 변경에 실패했습니다")
            } finally {
                _isUpdating.value = false
                updateCompleteButtonState()
            }
        }
    }

    private fun updateCompleteButtonState() {
        _isCheckButtonEnabled.value =
            _inputNickname.value.isNotBlank() &&
                _inputNickname.value != _currentNickname.value &&
                !_isUpdating.value

        _isCompleteEnabled.value =
            _inputNickname.value.isNotBlank() &&
                _isNicknameAvailable.value == true &&
                _inputNickname.value != _currentNickname.value &&
                !_isUpdating.value
    }

    companion object {
        private const val LOAD_PROFILE_ERROR_MESSAGE = "프로필 정보를 불러오지 못했습니다."
    }
}
