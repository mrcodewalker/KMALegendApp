package com.example.kmalegend.ui.scores

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.kmalegend.data.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.time.LocalDateTime

data class VirtualScoresUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val success: String? = null,
    val virtualScores: List<VirtualScore> = emptyList(),
    val studentInfo: StudentBasicInfo? = null
)

class VirtualScoresViewModel(application: Application) : AndroidViewModel(application) {
    private val prefs = PrefsManager(application)
    private val repository = Repository(prefs)

    private val _uiState = MutableStateFlow(VirtualScoresUiState())
    val uiState: StateFlow<VirtualScoresUiState> = _uiState

    // studentCode lấy từ schedule_secret (username đã đăng nhập)
    private val studentCode: String? get() =
        prefs.getScheduleSecret()?.data?.student_info?.student_code

    fun init() {
        val code = studentCode ?: run {
            _uiState.value = _uiState.value.copy(error = "Chưa đăng nhập")
            return
        }
        // Load local cache trước
        val cached = prefs.getVirtualScores()
        if (cached.isNotEmpty()) {
            _uiState.value = _uiState.value.copy(virtualScores = cached)
        }
        // Fetch từ server
        fetchFromServer(code)
    }

    private fun fetchFromServer(code: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            when (val result = repository.getScoreBatch(code)) {
                is Result.Success -> {
                    val batch = result.data.score_batch
                    val items = batch?.scoreItems?.map { it.toVirtualScore() }
                    val info = batch?.let { StudentBasicInfo(it.studentCode, it.studentName, it.studentClass) }
                    if (!items.isNullOrEmpty()) {
                        prefs.saveVirtualScores(items)
                        _uiState.value = _uiState.value.copy(
                            isLoading = false, virtualScores = items, studentInfo = info
                        )
                    } else {
                        // Không có trên server → tạo từ điểm tra cứu
                        loadFromScores(code)
                    }
                }
                is Result.Error -> {
                    // 404 hoặc lỗi → tạo từ điểm tra cứu
                    loadFromScores(code)
                }
            }
        }
    }

    private suspend fun loadFromScores(code: String) {
        when (val result = repository.getScores(code)) {
            is Result.Success -> {
                val scores = result.data.scoreDTOS ?: emptyList()
                val virtual = scores.map { s ->
                    VirtualScore(
                        scoreText = s.scoreText, scoreFirst = s.scoreFirst,
                        scoreSecond = s.scoreSecond, scoreFinal = s.scoreFinal,
                        scoreOverall = s.scoreOverall, subjectName = s.subjectName,
                        subjectCredit = s.subjectCredit, isSelected = true
                    )
                }
                val info = result.data.studentDTO
                prefs.saveVirtualScores(virtual)
                _uiState.value = _uiState.value.copy(
                    isLoading = false, virtualScores = virtual, studentInfo = info
                )
            }
            is Result.Error -> {
                _uiState.value = _uiState.value.copy(isLoading = false, error = result.message)
            }
        }
    }

    fun toggleSelectAll() {
        val list = _uiState.value.virtualScores
        val allSelected = list.all { it.isSelected }
        val updated = list.map { it.copy(isSelected = !allSelected) }
        prefs.saveVirtualScores(updated)
        _uiState.value = _uiState.value.copy(virtualScores = updated)
    }

    fun toggleSelection(index: Int) {
        val list = _uiState.value.virtualScores.toMutableList()
        if (index < list.size) {
            list[index] = list[index].copy(isSelected = !list[index].isSelected)
            prefs.saveVirtualScores(list)
            _uiState.value = _uiState.value.copy(virtualScores = list)
        }
    }

    fun addScore(score: VirtualScore) {
        val list = _uiState.value.virtualScores.toMutableList()
        list.add(score)
        prefs.saveVirtualScores(list)
        _uiState.value = _uiState.value.copy(virtualScores = list)
    }

    fun removeScore(index: Int) {
        val list = _uiState.value.virtualScores.toMutableList()
        if (index < list.size) { list.removeAt(index); prefs.saveVirtualScores(list) }
        _uiState.value = _uiState.value.copy(virtualScores = list)
    }

    fun importScores(scores: List<VirtualScore>) {
        val list = _uiState.value.virtualScores.toMutableList()
        list.addAll(scores)
        prefs.saveVirtualScores(list)
        _uiState.value = _uiState.value.copy(virtualScores = list, success = "Đã import ${scores.size} môn")
    }

    fun saveToServer() {
        val current = _uiState.value
        val info = current.studentInfo ?: return
        viewModelScope.launch {
            _uiState.value = current.copy(isLoading = true)
            val request = ScoreBatchRequest(
                studentInfo = info,
                scores = current.virtualScores,
                lastUpdated = LocalDateTime.now().toString()
            )
            when (val result = repository.saveScoreBatch(request)) {
                is Result.Success -> _uiState.value = _uiState.value.copy(isLoading = false, success = "Đã lưu lên server")
                is Result.Error -> _uiState.value = _uiState.value.copy(isLoading = false, error = result.message)
            }
        }
    }

    fun restoreFromServer() {
        val code = studentCode ?: return
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            when (val result = repository.restoreScores(code)) {
                is Result.Success -> {
                    val scores = result.data.scoreDTOS ?: emptyList()
                    val virtual = scores.map { s ->
                        VirtualScore(
                            scoreText = s.scoreText, scoreFirst = s.scoreFirst,
                            scoreSecond = s.scoreSecond, scoreFinal = s.scoreFinal,
                            scoreOverall = s.scoreOverall, subjectName = s.subjectName,
                            subjectCredit = s.subjectCredit, isSelected = true
                        )
                    }
                    prefs.saveVirtualScores(virtual)
                    _uiState.value = _uiState.value.copy(isLoading = false, virtualScores = virtual, success = "Đã khôi phục điểm gốc")
                }
                is Result.Error -> _uiState.value = _uiState.value.copy(isLoading = false, error = result.message)
            }
        }
    }

    fun getVirtualCPA(): Double {
        val selected = _uiState.value.virtualScores.filter { it.isSelected && !isExcludedFromGPA(it.subjectName) }
        val totalCredits = selected.sumOf { it.subjectCredit }
        if (totalCredits == 0) return 0.0
        return selected.sumOf { scoreToGrade4(it.scoreOverall) * it.subjectCredit } / totalCredits
    }

    fun clearError() { _uiState.value = _uiState.value.copy(error = null) }
    fun clearSuccess() { _uiState.value = _uiState.value.copy(success = null) }
}
