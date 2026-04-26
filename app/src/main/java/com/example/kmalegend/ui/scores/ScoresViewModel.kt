package com.example.kmalegend.ui.scores

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.kmalegend.data.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class ScoresUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val success: String? = null,
    val scores: List<ScoreDTO> = emptyList(),
    val studentInfo: StudentBasicInfo? = null,
    val virtualScores: List<VirtualScore> = emptyList(),
    val searchHistory: List<String> = emptyList(),
    val cpa: Double = 0.0,
    val totalCredits: Int = 0,
    val failedCount: Int = 0,
    val isVirtualMode: Boolean = false,
    val hasUnsavedChanges: Boolean = false
)

class ScoresViewModel(application: Application) : AndroidViewModel(application) {
    private val prefs = PrefsManager(application)
    private val repository = Repository(prefs)

    private val _uiState = MutableStateFlow(ScoresUiState(
        searchHistory = prefs.getSearchHistory(),
        virtualScores = prefs.getVirtualScores()
    ))
    val uiState: StateFlow<ScoresUiState> = _uiState

    fun searchScores(studentCode: String) {
        if (studentCode.isBlank()) return
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            when (val result = repository.getScores(studentCode)) {
                is Result.Success -> {
                    val data = result.data
                    val scores = data.scoreDTOS ?: emptyList()
                    val cpa = calculateCPA(scores)
                    val totalCredits = scores.filter { !isExcludedFromGPA(it.subjectName) && !isFailedSubject(it.scoreFinal, it.scoreOverall) }
                        .sumOf { it.subjectCredit }
                    val failedCount = scores.count { isFailedSubject(it.scoreFinal, it.scoreOverall) }
                    prefs.addSearchHistory(studentCode)
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        scores = scores,
                        studentInfo = data.studentDTO,
                        cpa = cpa,
                        totalCredits = totalCredits,
                        failedCount = failedCount,
                        searchHistory = prefs.getSearchHistory()
                    )
                }
                is Result.Error -> {
                    _uiState.value = _uiState.value.copy(isLoading = false, error = result.message)
                }
            }
        }
    }

    fun initVirtualScores() {
        val current = _uiState.value
        val studentCode = current.studentInfo?.studentCode ?: return
        viewModelScope.launch {
            _uiState.value = current.copy(isLoading = true)
            // Thử load từ server trước
            when (val result = repository.getScoreBatch(studentCode)) {
                is Result.Success -> {
                    val items = result.data.score_batch?.scoreItems?.map { it.toVirtualScore() }
                    val batchInfo = result.data.score_batch
                    if (!items.isNullOrEmpty()) {
                        prefs.saveVirtualScores(items)
                        prefs.saveVirtualScoresSnapshot(items)
                        // Cập nhật studentInfo từ score_batch nếu chưa có
                        val info = current.studentInfo ?: batchInfo?.let {
                            StudentBasicInfo(it.studentCode, it.studentName, it.studentClass)
                        }
                        _uiState.value = current.copy(
                            isLoading = false,
                            virtualScores = items,
                            studentInfo = info ?: current.studentInfo,
                            isVirtualMode = true,
                            success = "Đã tải bảng điểm ảo từ server"
                        )
                        return@launch
                    }
                }
                is Result.Error -> { /* fallback về local */ }
            }
            // Fallback: tạo từ điểm tra cứu
            if (current.scores.isEmpty()) {
                _uiState.value = current.copy(isLoading = false, error = "Không có dữ liệu điểm")
                return@launch
            }
            val virtual = current.scores.map { s ->
                VirtualScore(
                    scoreText = s.scoreText, scoreFirst = s.scoreFirst,
                    scoreSecond = s.scoreSecond, scoreFinal = s.scoreFinal,
                    scoreOverall = s.scoreOverall, subjectName = s.subjectName,
                    subjectCredit = s.subjectCredit, isSelected = true
                )
            }
            prefs.saveVirtualScores(virtual)
            prefs.saveVirtualScoresSnapshot(virtual)
            _uiState.value = current.copy(isLoading = false, virtualScores = virtual, isVirtualMode = true)
        }
    }

    fun updateVirtualScore(index: Int, updated: VirtualScore) {
        val list = _uiState.value.virtualScores.toMutableList()
        if (index < list.size) {
            list[index] = updated
            prefs.saveVirtualScores(list)
            _uiState.value = _uiState.value.copy(virtualScores = list, hasUnsavedChanges = true)
        }
    }

    fun toggleVirtualScoreSelection(index: Int) {
        val list = _uiState.value.virtualScores.toMutableList()
        if (index < list.size) {
            list[index] = list[index].copy(isSelected = !list[index].isSelected)
            prefs.saveVirtualScores(list)
            _uiState.value = _uiState.value.copy(virtualScores = list, hasUnsavedChanges = true)
        }
    }

    fun addVirtualScore(score: VirtualScore) {
        val list = _uiState.value.virtualScores.toMutableList()
        list.add(score)
        prefs.saveVirtualScores(list)
        _uiState.value = _uiState.value.copy(virtualScores = list, hasUnsavedChanges = true)
    }

    fun removeVirtualScore(index: Int) {
        val list = _uiState.value.virtualScores.toMutableList()
        if (index < list.size) {
            list.removeAt(index)
            prefs.saveVirtualScores(list)
            _uiState.value = _uiState.value.copy(virtualScores = list, hasUnsavedChanges = true)
        }
    }

    fun getVirtualCPA(): Double {
        val selected = _uiState.value.virtualScores.filter { it.isSelected && !isExcludedFromGPA(it.subjectName) }
        val totalCredits = selected.sumOf { it.subjectCredit }
        if (totalCredits == 0) return 0.0
        return selected.sumOf { scoreToGrade4(it.scoreOverall) * it.subjectCredit } / totalCredits
    }

    fun toggleSelectAll() {
        val list = _uiState.value.virtualScores
        val allSelected = list.all { it.isSelected }
        val updated = list.map { it.copy(isSelected = !allSelected) }
        prefs.saveVirtualScores(updated)
        _uiState.value = _uiState.value.copy(virtualScores = updated, hasUnsavedChanges = true)
    }

    fun importScores(scores: List<VirtualScore>) {
        val current = _uiState.value.virtualScores.toMutableList()
        current.addAll(scores)
        prefs.saveVirtualScores(current)
        _uiState.value = _uiState.value.copy(virtualScores = current, hasUnsavedChanges = true, success = "Đã import ${scores.size} môn")
    }

    fun saveToServer() {
        val current = _uiState.value
        val studentInfo = current.studentInfo ?: return
        viewModelScope.launch {
            _uiState.value = current.copy(isLoading = true)
            val request = ScoreBatchRequest(
                studentInfo = studentInfo,
                scores = current.virtualScores,
                lastUpdated = java.time.LocalDateTime.now().toString()
            )
            when (val result = repository.saveScoreBatch(request)) {
                is Result.Success -> _uiState.value = _uiState.value.copy(
                    isLoading = false, success = "Đã lưu lên server", hasUnsavedChanges = false
                )
                is Result.Error -> _uiState.value = _uiState.value.copy(isLoading = false, error = result.message)
            }
        }
    }

    fun restoreFromServer() {
        val studentCode = _uiState.value.studentInfo?.studentCode ?: return
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            when (val result = repository.restoreScores(studentCode)) {
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
                    _uiState.value = _uiState.value.copy(
                        isLoading = false, virtualScores = virtual, success = "Đã khôi phục điểm gốc"
                    )
                }
                is Result.Error -> _uiState.value = _uiState.value.copy(isLoading = false, error = result.message)
            }
        }
    }

    fun clearError() { _uiState.value = _uiState.value.copy(error = null) }
    fun clearSuccess() { _uiState.value = _uiState.value.copy(success = null) }
    fun setVirtualMode(enabled: Boolean) { _uiState.value = _uiState.value.copy(isVirtualMode = enabled) }
}
