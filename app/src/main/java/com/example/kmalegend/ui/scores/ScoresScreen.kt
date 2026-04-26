package com.example.kmalegend.ui.scores

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.kmalegend.data.*
import com.example.kmalegend.ui.common.*
import com.example.kmalegend.ui.theme.*

@Composable
fun ScoresScreen(navController: NavController, vm: ScoresViewModel = viewModel()) {
    val uiState by vm.uiState.collectAsState()
    var searchInput by remember { mutableStateOf("") }
    var showHistory by remember { mutableStateOf(false) }
    var showGradeTable by remember { mutableStateOf(false) }
    var showCpaDialog by remember { mutableStateOf(false) }
    val focusManager = LocalFocusManager.current

    Scaffold(topBar = { KmaTopBar(title = "Tra cứu điểm", navController = navController, showBack = true) }) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            LazyColumn(modifier = Modifier.fillMaxSize()) {

                // Search section
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        shape = RoundedCornerShape(16.dp),
                        elevation = 0.dp,
                        backgroundColor = White
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("Tra cứu điểm", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.subtitle1)
                            Text("Nhập mã sinh viên để xem bảng điểm", style = MaterialTheme.typography.caption, color = OnSurfaceMedium)
                            Spacer(Modifier.height(14.dp))
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                OutlinedTextField(
                                    value = searchInput,
                                    onValueChange = { searchInput = it; showHistory = false },
                                    placeholder = { Text("VD: CT070218") },
                                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = if (searchInput.isNotEmpty()) KmaRed else OnSurfaceMedium) },
                                    trailingIcon = {
                                        if (uiState.searchHistory.isNotEmpty()) {
                                            IconButton(onClick = { showHistory = !showHistory }) {
                                                Icon(Icons.Default.History, contentDescription = null, tint = OnSurfaceMedium)
                                            }
                                        }
                                    },
                                    modifier = Modifier.weight(1f),
                                    singleLine = true,
                                    shape = RoundedCornerShape(12.dp),
                                    colors = TextFieldDefaults.outlinedTextFieldColors(
                                        focusedBorderColor = KmaRed, cursorColor = KmaRed
                                    ),
                                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                                    keyboardActions = KeyboardActions(onSearch = { focusManager.clearFocus(); vm.searchScores(searchInput) })
                                )
                                Button(
                                    onClick = { focusManager.clearFocus(); vm.searchScores(searchInput) },
                                    enabled = !uiState.isLoading,
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.height(56.dp),
                                    colors = ButtonDefaults.buttonColors(backgroundColor = KmaRed),
                                    elevation = ButtonDefaults.elevation(0.dp)
                                ) {
                                    Icon(Icons.Default.Search, contentDescription = null, tint = White)
                                }
                            }
                            // History
                            if (showHistory && uiState.searchHistory.isNotEmpty()) {
                                Spacer(Modifier.height(8.dp))
                                Card(shape = RoundedCornerShape(12.dp), elevation = 4.dp) {
                                    Column {
                                        uiState.searchHistory.forEach { code ->
                                            Row(
                                                modifier = Modifier.fillMaxWidth()
                                                    .clickable { searchInput = code; showHistory = false; vm.searchScores(code) }
                                                    .padding(horizontal = 16.dp, vertical = 12.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Icon(Icons.Default.History, contentDescription = null, modifier = Modifier.size(16.dp), tint = OnSurfaceMedium)
                                                Spacer(Modifier.width(10.dp))
                                                Text(code, style = MaterialTheme.typography.body2)
                                            }
                                            Divider(color = SurfaceVariant)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // Results
                if (uiState.scores.isNotEmpty()) {
                    // Student info card
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                            shape = RoundedCornerShape(16.dp),
                            elevation = 0.dp,
                            backgroundColor = White
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                // Student header
                                uiState.studentInfo?.let { info ->
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(
                                            modifier = Modifier.size(48.dp).background(KmaRed, CircleShape),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                info.studentName.split(" ").lastOrNull()?.firstOrNull()?.toString() ?: "?",
                                                color = White, fontWeight = FontWeight.Bold, fontSize = 20.sp
                                            )
                                        }
                                        Spacer(Modifier.width(14.dp))
                                        Column {
                                            Text(info.studentName, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.subtitle1)
                                            Text("${info.studentCode} • ${info.studentClass}", style = MaterialTheme.typography.caption, color = OnSurfaceMedium)
                                        }
                                    }
                                }
                                Spacer(Modifier.height(16.dp))
                                // Stats row
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    StatChip("CPA", "%.2f".format(uiState.cpa), KmaRed, Modifier.weight(1f))
                                    StatChip("Tín chỉ", "${uiState.totalCredits}", KmaBlue, Modifier.weight(1f))
                                    StatChip("Trượt", "${uiState.failedCount}", if (uiState.failedCount > 0) Error else Success, Modifier.weight(1f))
                                }
                                Spacer(Modifier.height(14.dp))
                                // Action buttons
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    OutlinedButton(
                                        onClick = { showGradeTable = true },
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(10.dp),
                                        colors = ButtonDefaults.outlinedButtonColors(contentColor = KmaRed),
                                        border = ButtonDefaults.outlinedBorder.copy()
                                    ) {
                                        Icon(Icons.Default.TableChart, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(Modifier.width(4.dp))
                                        Text("Quy đổi", fontSize = 12.sp, fontWeight = FontWeight.Medium)
                                    }
                                    OutlinedButton(
                                        onClick = { showCpaDialog = true },
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(10.dp),
                                        colors = ButtonDefaults.outlinedButtonColors(contentColor = KmaRed)
                                    ) {
                                        Icon(Icons.Default.Calculate, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(Modifier.width(4.dp))
                                        Text("CPA mục tiêu", fontSize = 12.sp, fontWeight = FontWeight.Medium)
                                    }
                                }
                            }
                        }
                        Spacer(Modifier.height(12.dp))
                    }

                    // Table header
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth()
                                .padding(horizontal = 16.dp)
                                .background(KmaRed, RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp))
                                .padding(horizontal = 12.dp, vertical = 10.dp)
                        ) {
                            Text("Môn học", color = White, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(3f), fontSize = 11.sp)
                            Text("TC", color = White, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(0.6f), fontSize = 11.sp)
                            Text("TP1", color = White, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(0.7f), fontSize = 11.sp)
                            Text("TP2", color = White, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(0.7f), fontSize = 11.sp)
                            Text("CK", color = White, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(0.7f), fontSize = 11.sp)
                            Text("HP", color = White, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(0.7f), fontSize = 11.sp)
                            Text("Chữ", color = White, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(0.7f), fontSize = 11.sp)
                        }
                    }

                    itemsIndexed(uiState.scores) { index, score ->
                        ScoreRow(score = score, isEven = index % 2 == 0)
                    }

                    item {
                        Box(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)
                            .background(SurfaceVariant, RoundedCornerShape(bottomStart = 12.dp, bottomEnd = 12.dp))
                            .padding(10.dp), contentAlignment = Alignment.Center) {
                            Text("${uiState.scores.size} môn học", style = MaterialTheme.typography.caption, color = OnSurfaceMedium)
                        }
                        Spacer(Modifier.height(24.dp))
                    }
                }
            }

            uiState.error?.let {
                Box(modifier = Modifier.align(Alignment.BottomCenter)) {
                    ToastMessage(it, ToastType.ERROR) { vm.clearError() }
                }
            }
            if (uiState.isLoading) LoadingOverlay()
        }
    }

    if (showGradeTable) GradeConversionDialog { showGradeTable = false }
    if (showCpaDialog) CPACalculatorDialog(uiState.cpa, uiState.totalCredits) { showCpaDialog = false }
}

@Composable
fun ScoreRow(score: ScoreDTO, isEven: Boolean = false) {
    val isFailed = isFailedSubject(score.scoreFinal, score.scoreOverall)
    val bg = when {
        isFailed -> ErrorSurface
        isEven -> SurfaceVariant
        else -> White
    }
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)
            .background(bg).padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(score.subjectName, modifier = Modifier.weight(3f), fontSize = 11.sp, lineHeight = 15.sp, color = OnSurfaceHigh)
        Text("${score.subjectCredit}", modifier = Modifier.weight(0.6f), fontSize = 11.sp, color = OnSurfaceMedium)
        Text("%.1f".format(score.scoreFirst), modifier = Modifier.weight(0.7f), fontSize = 11.sp, color = OnSurfaceMedium)
        Text("%.1f".format(score.scoreSecond), modifier = Modifier.weight(0.7f), fontSize = 11.sp, color = OnSurfaceMedium)
        Text("%.1f".format(score.scoreFinal), modifier = Modifier.weight(0.7f), fontSize = 11.sp, color = OnSurfaceMedium)
        Text("%.1f".format(score.scoreOverall), modifier = Modifier.weight(0.7f), fontSize = 11.sp, fontWeight = FontWeight.Medium, color = OnSurfaceHigh)
        Box(
            modifier = Modifier.weight(0.7f),
            contentAlignment = Alignment.CenterStart
        ) {
            val letterColor = when {
                isFailed -> Error
                score.scoreOverall >= 8.5 -> GradeA
                score.scoreOverall >= 7.0 -> GradeB
                score.scoreOverall >= 5.5 -> GradeC
                else -> GradeD
            }
            Text(score.scoreText, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = letterColor)
        }
    }
    Divider(color = Outline.copy(alpha = 0.5f), modifier = Modifier.padding(horizontal = 16.dp))
}

@Composable
fun GradeConversionDialog(onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(20.dp),
        title = { Text("Bảng quy đổi điểm", fontWeight = FontWeight.Bold) },
        text = {
            Column {
                Row(modifier = Modifier.fillMaxWidth().background(KmaRed, RoundedCornerShape(8.dp)).padding(10.dp)) {
                    Text("Phân loại", color = White, modifier = Modifier.weight(2f), fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                    Text("Thang 10", color = White, modifier = Modifier.weight(1.5f), fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                    Text("Thang 4", color = White, modifier = Modifier.weight(1f), fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                    Text("Chữ", color = White, modifier = Modifier.weight(0.8f), fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                }
                GRADE_TABLE.forEachIndexed { i, g ->
                    Row(modifier = Modifier.fillMaxWidth().background(if (i % 2 == 0) SurfaceVariant else White).padding(vertical = 7.dp, horizontal = 10.dp)) {
                        Text(g.label, modifier = Modifier.weight(2f), fontSize = 11.sp)
                        Text("${g.min}–${g.max}", modifier = Modifier.weight(1.5f), fontSize = 11.sp)
                        Text("${g.grade4}", modifier = Modifier.weight(1f), fontSize = 11.sp)
                        val color = when (g.letter) { "A+", "A" -> GradeA; "B+", "B" -> GradeB; "C+", "C" -> GradeC; "D+", "D" -> GradeD; else -> Error }
                        Text(g.letter, modifier = Modifier.weight(0.8f), fontSize = 11.sp, fontWeight = FontWeight.Bold, color = color)
                    }
                }
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text("Đóng", color = KmaRed, fontWeight = FontWeight.SemiBold) } }
    )
}

@Composable
fun CPACalculatorDialog(currentCPA: Double, currentCredits: Int, onDismiss: () -> Unit) {
    var totalCredits by remember { mutableStateOf("") }
    var targetCPA by remember { mutableStateOf("") }
    var result by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(20.dp),
        title = { Text("CPA mục tiêu", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(modifier = Modifier.fillMaxWidth().background(KmaRedSurface, RoundedCornerShape(12.dp)).padding(14.dp), horizontalArrangement = Arrangement.SpaceEvenly) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("%.2f".format(currentCPA), fontWeight = FontWeight.Bold, color = KmaRed, fontSize = 20.sp)
                        Text("CPA hiện tại", fontSize = 11.sp, color = OnSurfaceMedium)
                    }
                    Box(modifier = Modifier.width(1.dp).height(36.dp).background(Outline))
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("$currentCredits", fontWeight = FontWeight.Bold, color = KmaBlue, fontSize = 20.sp)
                        Text("TC tích lũy", fontSize = 11.sp, color = OnSurfaceMedium)
                    }
                }
                OutlinedTextField(value = totalCredits, onValueChange = { totalCredits = it },
                    label = { Text("Tổng TC chương trình") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp),
                    colors = TextFieldDefaults.outlinedTextFieldColors(focusedBorderColor = KmaRed, cursorColor = KmaRed))
                OutlinedTextField(value = targetCPA, onValueChange = { targetCPA = it },
                    label = { Text("CPA mục tiêu") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp),
                    colors = TextFieldDefaults.outlinedTextFieldColors(focusedBorderColor = KmaRed, cursorColor = KmaRed))
                Button(onClick = {
                    val total = totalCredits.toIntOrNull() ?: return@Button
                    val target = targetCPA.toDoubleOrNull() ?: return@Button
                    val remaining = total - currentCredits
                    if (remaining <= 0) { result = "Đã hoàn thành chương trình!"; return@Button }
                    val needed = (target * total - currentCPA * currentCredits) / remaining
                    result = "GPA cần đạt: ${"%.2f".format(needed)}\nTC còn lại: $remaining"
                }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(backgroundColor = KmaRed),
                    elevation = ButtonDefaults.elevation(0.dp)) { Text("Tính", color = White, fontWeight = FontWeight.SemiBold) }
                if (result.isNotEmpty()) {
                    Row(modifier = Modifier.fillMaxWidth().background(SuccessSurface, RoundedCornerShape(10.dp)).padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Lightbulb, contentDescription = null, tint = Success, modifier = Modifier.size(20.dp))
                        Spacer(Modifier.width(10.dp))
                        Text(result, color = Success, fontWeight = FontWeight.Medium, fontSize = 13.sp, lineHeight = 20.sp)
                    }
                }
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text("Đóng", color = KmaRed, fontWeight = FontWeight.SemiBold) } }
    )
}
