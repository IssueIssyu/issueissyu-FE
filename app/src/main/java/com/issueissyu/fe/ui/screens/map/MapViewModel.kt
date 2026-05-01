package com.issueissyu.fe.ui.screens.map

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class MapViewModel @Inject constructor() : ViewModel() {

    private val _showResearchButton = MutableStateFlow(false)
    val showResearchButton: StateFlow<Boolean> = _showResearchButton.asStateFlow()

    private val _showPinTypeSelector = MutableStateFlow(false)
    val showPinTypeSelector: StateFlow<Boolean> = _showPinTypeSelector.asStateFlow()

    private val _selectedCategory = MutableStateFlow<String?>(null)
    val selectedCategory: StateFlow<String?> = _selectedCategory.asStateFlow()

    // TODO: 핀 담당자가 공용 Pin 모델을 완성하면, 이 String 값을 PinCategory enum과 매핑하여 사용
    fun onCategorySelected(category: String?) {
        _selectedCategory.value = category
    }

    fun openPinTypeSelector() {
        _showPinTypeSelector.value = true
    }

    fun closePinTypeSelector() {
        _showPinTypeSelector.value = false
    }

    fun showResearchAreaButton() {
        _showResearchButton.value = true
    }

    fun hideResearchAreaButton() {
        _showResearchButton.value = false
    }
}