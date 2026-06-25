package com.king.kevin.tiroparabolico.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.king.kevin.tiroparabolico.domain.model.AcademicResponse
import com.king.kevin.tiroparabolico.domain.usecases.SaveAcademicResponseUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AcademicViewModel(
    private val saveAcademicResponseUseCase: SaveAcademicResponseUseCase
) : ViewModel() {

    private val _saveState = MutableStateFlow<Result<Unit>?>(null)
    val saveState = _saveState.asStateFlow()

    fun saveResponse(response: AcademicResponse) {
        viewModelScope.launch {
            val result = saveAcademicResponseUseCase(response)
            _saveState.value = result
        }
    }

    fun resetSaveState() {
        _saveState.value = null
    }
}
