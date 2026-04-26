package com.example.kmalegend.ui.login

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.kmalegend.data.PrefsManager
import com.example.kmalegend.data.Repository
import com.example.kmalegend.data.Result
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class LoginUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val isSuccess: Boolean = false
)

class LoginViewModel(application: Application) : AndroidViewModel(application) {
    private val prefs = PrefsManager(application)
    private val repository = Repository(prefs)

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState

    fun login(username: String, password: String) {
        if (username.isBlank() || password.isBlank()) {
            _uiState.value = LoginUiState(error = "Vui lòng nhập đầy đủ thông tin")
            return
        }
        viewModelScope.launch {
            _uiState.value = LoginUiState(isLoading = true)
            when (val result = repository.login(username, password)) {
                is Result.Success -> _uiState.value = LoginUiState(isSuccess = true)
                is Result.Error -> _uiState.value = LoginUiState(error = result.message)
            }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}
