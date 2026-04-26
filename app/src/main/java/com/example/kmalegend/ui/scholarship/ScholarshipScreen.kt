package com.example.kmalegend.ui.scholarship

import android.app.Application
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Celebration
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.kmalegend.data.*
import com.example.kmalegend.ui.common.*
import com.example.kmalegend.ui.theme.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import nl.dionsegijn.konfetti.compose.KonfettiView
import nl.dionsegijn.konfetti.core.Party
import nl.dionsegijn.konfetti.core.Position
import nl.dionsegijn.konfetti.core.emitter.Emitter
import java.util.concurrent.TimeUnit

data class ScholarshipUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val students: List<ScholarshipStudent> = emptyList(),
    val selectedCode: String = "CT07",
    val showFireworks: Boolean = true,
    val fireworksActive: Boolean = false
)

class ScholarshipViewModel(application: Application) : AndroidViewModel(application) {
    private val prefs = PrefsManager(application)
    private val repository = Repository(prefs)
    private val _uiState = MutableStateFlow(ScholarshipUiState())
    val uiState: StateFlow<ScholarshipUiState> = _uiState

    fun load(code: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null, selectedCode = code)
            when (val r = repository.getScholarship(code)) {
                is Result.Success -> _uiState.value = _uiState.value.copy(isLoading = false, students = r.data, fireworksActive = _uiState.value.showFireworks)
                is Result.Error -> _uiState.value = _uiState.value.copy(isLoading = false, error = r.message)
            }
        }
    }

    fun toggleFireworks() {
        val s = _uiState.value
        _uiState.value = s.copy(showFireworks = !s.showFireworks, fireworksActive = false)
    }

    fun clearError() { _uiState.value = _uiState.value.copy(error = null) }
    fun stopFireworks() { _uiState.value = _uiState.value.copy(fireworksActive = false) }
}

@Composable
fun ScholarshipScreen(navController: NavController, vm: ScholarshipViewModel = viewModel()) {
    val uiState by vm.uiState.collectAsState()
    val cohorts = listOf("CT05","CT06","CT07","CT08","CT09","AT17","AT18","AT19","AT20","AT21","DT04","DT05","DT06","DT07","DT08")
    var expanded by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            topBar = {
                KmaTopBar(
                    title = "Học bổng",
                    navController = navController,
                    showBack = true,
                    actions = {
                        IconButton(onClick = { vm.toggleFireworks() }) {
                            Icon(if (uiState.showFireworks) Icons.Default.Celebration else Icons.Outlined.Celebration, contentDescription = null, tint = White)
                        }
                    }
                )
            }
        ) { padding ->
            Column(modifier = Modifier.fillMaxSize().padding(padding)) {
                // Selector
                Card(modifier = Modifier.fillMaxWidth().padding(16.dp), shape = RoundedCornerShape(16.dp), elevation = 0.dp, backgroundColor = White) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Chọn khóa học", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.subtitle1)
                        Text("Xem bảng xếp hạng học bổng theo khóa", style = MaterialTheme.typography.caption, color = OnSurfaceMedium)
                        Spacer(Modifier.height(12.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.weight(1f)) {
                                OutlinedButton(
                                    onClick = { expanded = true },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = OnSurfaceHigh)
                                ) {
                                    Text(uiState.selectedCode, fontWeight = FontWeight.Medium)
                                    Spacer(Modifier.weight(1f))
                                    Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                                }
                                DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                                    cohorts.forEach { code ->
                                        DropdownMenuItem(onClick = { expanded = false; vm.load(code) }) {
                                            Text(code, fontWeight = FontWeight.Medium)
                                        }
                                    }
                                }
                            }
                            Button(
                                onClick = { vm.load(uiState.selectedCode) },
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(backgroundColor = KmaRed),
                                elevation = ButtonDefaults.elevation(0.dp)
                            ) { Text("Tải", color = White, fontWeight = FontWeight.SemiBold) }
                        }
                    }
                }

                if (uiState.students.isNotEmpty()) {
                    // Summary chips
                    Row(modifier = Modifier.padding(horizontal = 16.dp).padding(bottom = 12.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        StatChip("Tổng", "${uiState.students.size}", KmaBlue, Modifier.weight(1f))
                        StatChip("Top GPA", "%.2f".format(uiState.students.firstOrNull()?.gpa ?: 0.0), KmaGold, Modifier.weight(1f))
                        StatChip("Khóa", uiState.selectedCode, KmaRed, Modifier.weight(1f))
                    }

                    // Table header
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)
                            .background(KmaRed, RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp))
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("#", color = White, fontWeight = FontWeight.SemiBold, modifier = Modifier.width(28.dp), fontSize = 11.sp)
                        Text("Sinh viên", color = White, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(2f), fontSize = 11.sp)
                        Text("Lớp", color = White, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f), fontSize = 11.sp)
                        Text("GPA", color = White, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(0.8f), fontSize = 11.sp)
                        Text("GPA10", color = White, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(0.8f), fontSize = 11.sp)
                    }

                    LazyColumn(modifier = Modifier.weight(1f)) {
                        itemsIndexed(uiState.students) { index, student ->
                            val bg = when {
                                index < 3 -> GoldBg
                                index < 10 -> SilverBg
                                else -> if (index % 2 == 0) SurfaceVariant else White
                            }
                            val rankColor = when {
                                index < 3 -> androidx.compose.ui.graphics.Color(0xFFB8860B)
                                index < 10 -> KmaBlue
                                else -> OnSurfaceMedium
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)
                                    .background(color = bg, shape = androidx.compose.ui.graphics.RectangleShape)
                                    .padding(horizontal = 12.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Rank badge
                                Box(modifier = Modifier.width(28.dp), contentAlignment = Alignment.Center) {
                                    if (index < 3) {
                                        val medal = listOf("🥇", "🥈", "🥉")[index]
                                        Text(medal, fontSize = 16.sp)
                                    } else {
                                        Text("${index + 1}", fontSize = 11.sp, color = OnSurfaceMedium, fontWeight = FontWeight.Medium)
                                    }
                                }
                                Column(modifier = Modifier.weight(2f)) {
                                    Text(student.studentName, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = OnSurfaceHigh)
                                    Text(student.studentCode, fontSize = 10.sp, color = OnSurfaceMedium)
                                }
                                Text(student.studentClass, modifier = Modifier.weight(1f), fontSize = 11.sp, color = OnSurfaceMedium)
                                Text("%.2f".format(student.gpa), modifier = Modifier.weight(0.8f), fontSize = 12.sp, fontWeight = FontWeight.Bold, color = rankColor)
                                Text("%.1f".format(student.asiaGpa), modifier = Modifier.weight(0.8f), fontSize = 11.sp, color = OnSurfaceMedium)
                            }
                            Divider(color = Outline.copy(alpha = 0.4f), modifier = Modifier.padding(horizontal = 16.dp))
                        }
                        item {
                            Box(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)
                                .background(SurfaceVariant, RoundedCornerShape(bottomStart = 12.dp, bottomEnd = 12.dp))
                                .padding(10.dp), contentAlignment = Alignment.Center) {
                                Text("${uiState.students.size} sinh viên", style = MaterialTheme.typography.caption, color = OnSurfaceMedium)
                            }
                            Spacer(Modifier.height(16.dp))
                        }
                    }
                }
            }
        }

        // Konfetti
        if (uiState.fireworksActive) {
            KonfettiView(
                modifier = Modifier.fillMaxSize(),
                parties = listOf(
                    Party(emitter = Emitter(3, TimeUnit.SECONDS).perSecond(80), position = Position.Relative(0.5, 0.0)),
                    Party(emitter = Emitter(3, TimeUnit.SECONDS).perSecond(40), position = Position.Relative(0.2, 0.0)),
                    Party(emitter = Emitter(3, TimeUnit.SECONDS).perSecond(40), position = Position.Relative(0.8, 0.0))
                )
            )
            LaunchedEffect(Unit) { kotlinx.coroutines.delay(4000); vm.stopFireworks() }
        }

        uiState.error?.let {
            Box(modifier = Modifier.align(Alignment.BottomCenter)) {
                ToastMessage(it, ToastType.ERROR) { vm.clearError() }
            }
        }
        if (uiState.isLoading) LoadingOverlay()
    }
}
