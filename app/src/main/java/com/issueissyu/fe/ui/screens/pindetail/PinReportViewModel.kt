package com.issueissyu.fe.ui.screens.pindetail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.issueissyu.fe.domain.repository.PinRepository
import com.issueissyu.fe.domain.usecase.community.DeclareCommunityUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch

@HiltViewModel
class PinReportViewModel @Inject constructor(
    private val pinRepository: PinRepository,
    private val declareCommunityUseCase: DeclareCommunityUseCase,
) : ViewModel() {

    private val _snackbarMessage = MutableSharedFlow<String>(extraBufferCapacity = 1)
    val snackbarMessage = _snackbarMessage.asSharedFlow()

    fun report(targetId: String, targetType: ReportTargetType, reasonIndex: Int, onSuccess: () -> Unit) {
        viewModelScope.launch {
            val id = targetId.toLongOrNull() ?: return@launch
            val result = when (targetType) {
                ReportTargetType.PIN -> pinRepository.declarePin(id, reasonIndex)
                ReportTargetType.COMMUNITY -> declareCommunityUseCase(id, reasonIndex)
            }

            result
                .onSuccess { onSuccess() }
                .onFailure { e -> _snackbarMessage.emit(e.message.orEmpty()) }
        }
    }
}
