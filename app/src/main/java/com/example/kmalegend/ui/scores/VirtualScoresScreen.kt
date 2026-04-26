package com.example.kmalegend.ui.scores

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.kmalegend.data.*
import com.example.kmalegend.ui.common.*
import com.example.kmalegend.ui.theme.*

private val Purple = Color(0xFF6A1B9A)
private val PurpleLight = Color(0xFFF3E5F5)

@Composable
fun VirtualScoresScreen(navController: NavController, vm: VirtualScoresViewModel = viewModel()) {
    val uiState by vm.uiState.collectAsState()

    LaunchedEffect(Unit) { vm.init() }

    val virtualCPA = vm.getVirtualCPA()
    val selectedCredits = uiState.virtualScores.filter { it.isSelected }.sumOf { it.subjectCredit }
    val allSelected = uiState.virtualScores.isNotEmpty() && uiState.virtualScores.all { it.isSelected }

    var showCpaDialog by remember { mutableStateOf(false) }
    var showAddDialog by remember { mutableStateOf(false) }
    var showImportDialog by remember { mutableStateOf(false) }
    var showSaveConfirm by remember { mutableStateOf(false) }
    var showRestoreConfirm by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Bảng điểm ảo", fontWeight = FontWeight.Bold, color = White)
                        Text("CPA: ${"%.2f".format(virtualCPA)} • $selectedCredits TC", fontSize = 12.sp, color = White.copy(alpha = 0.85f))
                    }
                },
                backgroundColor = Purple,
                contentColor = White,
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = null)
                    }
                }
            )
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            Column(modifier = Modifier.fillMaxSize()) {
                ActionButtonsBar(
                    allSelected = allSelected,
                    onSelectAll = { vm.toggleSelectAll() },
                    onAddSubject = { showAddDialog = true },
                    onImport = { showImportDialog = true },
                    onRestore = { showRestoreConfirm = true },
                    onCpaTarget = { showCpaDialog = true },
                    onSave = { showSaveConfirm = true }
                )
                VirtualTableHeader()
                LazyColumn(modifier = Modifier.weight(1f)) {
                    itemsIndexed(uiState.virtualScores) { index, score ->
                        VirtualScoreRow(
                            score = score,
                            onToggle = { vm.toggleSelection(index) },
                            onDelete = { vm.removeScore(index) }
                        )
                    }
                    if (uiState.virtualScores.isEmpty() && !uiState.isLoading) {
                        item {
                            Box(modifier = Modifier.fillMaxWidth().padding(40.dp), contentAlignment = Alignment.Center) {
                                Text("Chưa có môn học nào", color = OnSurfaceMedium)
                            }
                        }
                    }
                }
            }

            uiState.error?.let {
                Box(modifier = Modifier.align(Alignment.BottomCenter)) {
                    ToastMessage(it, ToastType.ERROR) { vm.clearError() }
                }
            }
            uiState.success?.let {
                Box(modifier = Modifier.align(Alignment.BottomCenter)) {
                    ToastMessage(it, ToastType.SUCCESS) { vm.clearSuccess() }
                }
            }
            if (uiState.isLoading) LoadingOverlay()
        }
    }

    if (showCpaDialog) VirtualCPADialog(virtualCPA, selectedCredits) { showCpaDialog = false }
    if (showAddDialog) AddSubjectDialog(onAdd = { vm.addScore(it) }, onDismiss = { showAddDialog = false })
    if (showImportDialog) ImportKhaoThiDialog(onImport = { vm.importScores(it) }, onDismiss = { showImportDialog = false })
    if (showSaveConfirm) ConfirmDialog(
        title = "Lưu lên server",
        message = "Lưu bảng điểm ảo hiện tại lên server?",
        confirmText = "Lưu",
        onConfirm = { vm.saveToServer(); showSaveConfirm = false },
        onDismiss = { showSaveConfirm = false }
    )
    if (showRestoreConfirm) ConfirmDialog(
        title = "Khôi phục",
        message = "Khôi phục bảng điểm về điểm gốc từ server?",
        confirmText = "Khôi phục",
        onConfirm = { vm.restoreFromServer(); showRestoreConfirm = false },
        onDismiss = { showRestoreConfirm = false }
    )
}

@Composable
private fun ActionButtonsBar(
    allSelected: Boolean,
    onSelectAll: () -> Unit,
    onAddSubject: () -> Unit,
    onImport: () -> Unit,
    onRestore: () -> Unit,
    onCpaTarget: () -> Unit,
    onSave: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = 4.dp,
        shape = RoundedCornerShape(0.dp),
        backgroundColor = PurpleLight
    ) {
        Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ActionChip(
                    icon = if (allSelected) Icons.Default.CheckBox else Icons.Default.CheckBoxOutlineBlank,
                    label = if (allSelected) "Bỏ chọn tất cả" else "Chọn tất cả",
                    color = Purple, modifier = Modifier.weight(1f), onClick = onSelectAll
                )
                ActionChip(Icons.Default.Add, "Thêm môn", Color(0xFF1565C0), Modifier.weight(1f), onAddSubject)
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ActionChip(Icons.Default.FileUpload, "Từ Khảo Thí", Color(0xFF2E7D32), Modifier.weight(1f), onImport)
                ActionChip(Icons.Default.Restore, "Khôi phục", Color(0xFFE65100), Modifier.weight(1f), onRestore)
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ActionChip(Icons.Default.Calculate, "CPA mục tiêu", Color(0xFF00695C), Modifier.weight(1f), onCpaTarget)
                ActionChip(Icons.Default.CloudUpload, "Lưu lên server", Color(0xFFC62828), Modifier.weight(1f), onSave)
            }
        }
    }
}

@Composable
private fun ActionChip(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    color: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        modifier = modifier.height(40.dp),
        shape = RoundedCornerShape(10.dp),
        colors = ButtonDefaults.buttonColors(backgroundColor = color),
        contentPadding = PaddingValues(horizontal = 8.dp)
    ) {
        Icon(icon, contentDescription = null, tint = White, modifier = Modifier.size(16.dp))
        Spacer(Modifier.width(4.dp))
        Text(label, color = White, fontSize = 11.sp, fontWeight = FontWeight.Medium, maxLines = 1)
    }
}

@Composable
private fun VirtualTableHeader() {
    Row(
        modifier = Modifier.fillMaxWidth()
            .background(Purple)
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Spacer(Modifier.width(28.dp))
        Text("Môn học", color = White, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(3f), fontSize = 11.sp)
        Text("TC", color = White, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(0.6f), fontSize = 11.sp)
        Text("HP", color = White, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(0.7f), fontSize = 11.sp)
        Text("Chữ", color = White, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(0.7f), fontSize = 11.sp)
        Spacer(Modifier.width(32.dp))
    }
}

@Composable
private fun VirtualScoreRow(score: VirtualScore, onToggle: () -> Unit, onDelete: () -> Unit) {
    val isFailed = isFailedSubject(score.scoreFinal, score.scoreOverall)
    val bg = when {
        !score.isSelected -> SurfaceVariant
        isFailed -> ErrorSurface
        else -> White
    }
    Row(
        modifier = Modifier.fillMaxWidth().background(bg).padding(horizontal = 8.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(
            checked = score.isSelected, onCheckedChange = { onToggle() },
            modifier = Modifier.size(28.dp),
            colors = CheckboxDefaults.colors(checkedColor = Purple, uncheckedColor = OnSurfaceMedium)
        )
        Text(score.subjectName, modifier = Modifier.weight(3f), fontSize = 11.sp, lineHeight = 15.sp,
            color = if (score.isSelected) OnSurfaceHigh else OnSurfaceMedium)
        Text("${score.subjectCredit}", modifier = Modifier.weight(0.6f), fontSize = 11.sp, color = OnSurfaceMedium)
        Text("%.1f".format(score.scoreOverall), modifier = Modifier.weight(0.7f), fontSize = 11.sp, fontWeight = FontWeight.Medium,
            color = if (score.isSelected) OnSurfaceHigh else OnSurfaceMedium)
        val letterColor = when {
            isFailed -> Error
            score.scoreOverall >= 8.5 -> GradeA
            score.scoreOverall >= 7.0 -> GradeB
            score.scoreOverall >= 5.5 -> GradeC
            else -> GradeD
        }
        Text(scoreToLetter(score.scoreOverall), modifier = Modifier.weight(0.7f), fontSize = 11.sp,
            fontWeight = FontWeight.Bold, color = if (score.isSelected) letterColor else OnSurfaceMedium)
        IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
            Icon(Icons.Default.DeleteOutline, contentDescription = null, tint = Error.copy(alpha = 0.7f), modifier = Modifier.size(18.dp))
        }
    }
    Divider(color = Outline.copy(alpha = 0.4f))
}

@Composable
fun VirtualCPADialog(currentCPA: Double, currentCredits: Int, onDismiss: () -> Unit) {
    var totalCredits by remember { mutableStateOf("") }
    var targetCPA by remember { mutableStateOf("") }
    var result by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("CPA mục tiêu (ảo)", fontWeight = FontWeight.Bold, color = Purple) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Card(backgroundColor = PurpleLight, shape = RoundedCornerShape(8.dp)) {
                    Row(modifier = Modifier.fillMaxWidth().padding(10.dp), horizontalArrangement = Arrangement.SpaceEvenly) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("%.2f".format(currentCPA), fontWeight = FontWeight.Bold, color = Purple, fontSize = 18.sp)
                            Text("CPA ảo", fontSize = 11.sp, color = OnSurfaceMedium)
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("$currentCredits", fontWeight = FontWeight.Bold, color = Purple, fontSize = 18.sp)
                            Text("TC đã chọn", fontSize = 11.sp, color = OnSurfaceMedium)
                        }
                    }
                }
                OutlinedTextField(value = totalCredits, onValueChange = { totalCredits = it },
                    label = { Text("Tổng TC chương trình") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(10.dp))
                OutlinedTextField(value = targetCPA, onValueChange = { targetCPA = it },
                    label = { Text("CPA mục tiêu") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(10.dp))
                Button(
                    onClick = {
                        val total = totalCredits.toIntOrNull() ?: return@Button
                        val target = targetCPA.toDoubleOrNull() ?: return@Button
                        val remaining = total - currentCredits
                        if (remaining <= 0) { result = "Đã hoàn thành!"; return@Button }
                        val needed = (target * total - currentCPA * currentCredits) / remaining
                        val hardcore = if (needed <= 4.0) ((needed * remaining) / (4.0 * 3)).toInt() + 1 else -1
                        val chill = if (needed <= 3.5) ((needed * remaining) / (3.5 * 3)).toInt() + 1 else -1
                        result = buildString {
                            appendLine("GPA cần đạt: ${"%.2f".format(needed)}")
                            appendLine("TC còn lại: $remaining")
                            if (hardcore > 0) appendLine("Hardcore: ~$hardcore môn A+ (3TC)")
                            if (chill > 0) appendLine("Chill: ~$chill môn B+ (3TC)")
                        }.trim()
                    },
                    modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(backgroundColor = Purple)
                ) { Text("Tính", color = White) }
                if (result.isNotEmpty()) {
                    Card(backgroundColor = PurpleLight, shape = RoundedCornerShape(8.dp), modifier = Modifier.fillMaxWidth()) {
                        Text(result, modifier = Modifier.padding(12.dp), color = Purple, fontWeight = FontWeight.Bold, lineHeight = 20.sp)
                    }
                }
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text("Đóng") } }
    )
}

@Composable
fun AddSubjectDialog(onAdd: (VirtualScore) -> Unit, onDismiss: () -> Unit) {
    var name by remember { mutableStateOf("") }
    var credits by remember { mutableStateOf("3") }
    var tp1 by remember { mutableStateOf("") }
    var tp2 by remember { mutableStateOf("") }
    var ck by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Thêm môn học", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Tên môn học") },
                    modifier = Modifier.fillMaxWidth(), singleLine = true, shape = RoundedCornerShape(10.dp))
                OutlinedTextField(value = credits, onValueChange = { credits = it }, label = { Text("Số tín chỉ") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(), singleLine = true, shape = RoundedCornerShape(10.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = tp1, onValueChange = { tp1 = it }, label = { Text("TP1") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.weight(1f), singleLine = true, shape = RoundedCornerShape(10.dp))
                    OutlinedTextField(value = tp2, onValueChange = { tp2 = it }, label = { Text("TP2") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.weight(1f), singleLine = true, shape = RoundedCornerShape(10.dp))
                    OutlinedTextField(value = ck, onValueChange = { ck = it }, label = { Text("CK") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.weight(1f), singleLine = true, shape = RoundedCornerShape(10.dp))
                }
                val t1 = tp1.toDoubleOrNull() ?: 0.0
                val t2 = tp2.toDoubleOrNull() ?: 0.0
                val c = ck.toDoubleOrNull() ?: 0.0
                val overall = calculateScoreOverall(t1, t2, c)
                if (tp1.isNotEmpty() || tp2.isNotEmpty() || ck.isNotEmpty()) {
                    Card(backgroundColor = Purple.copy(alpha = 0.08f), shape = RoundedCornerShape(8.dp)) {
                        Row(modifier = Modifier.fillMaxWidth().padding(10.dp), horizontalArrangement = Arrangement.SpaceEvenly) {
                            Text("HP: ${"%.2f".format(overall)}", fontWeight = FontWeight.Bold, color = Purple)
                            Text("Chữ: ${scoreToLetter(overall)}", fontWeight = FontWeight.Bold, color = Purple)
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isBlank()) return@Button
                    val t1 = tp1.toDoubleOrNull() ?: 0.0
                    val t2 = tp2.toDoubleOrNull() ?: 0.0
                    val c = ck.toDoubleOrNull() ?: 0.0
                    val overall = calculateScoreOverall(t1, t2, c)
                    onAdd(VirtualScore(
                        scoreText = scoreToLetter(overall), scoreFirst = t1, scoreSecond = t2,
                        scoreFinal = c, scoreOverall = overall, subjectName = name,
                        subjectCredit = credits.toIntOrNull() ?: 3, isSelected = true
                    ))
                    onDismiss()
                },
                colors = ButtonDefaults.buttonColors(backgroundColor = Purple)
            ) { Text("Thêm", color = White) }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Hủy") } }
    )
}

@Composable
fun ImportKhaoThiDialog(onImport: (List<VirtualScore>) -> Unit, onDismiss: () -> Unit) {
    var rawText by remember { mutableStateOf("") }
    var step by remember { mutableStateOf(1) }
    var parsed by remember { mutableStateOf<List<VirtualScore>>(emptyList()) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (step == 1) "Import từ Khảo Thí (1/2)" else "Xem trước (2/2)", fontWeight = FontWeight.Bold) },
        text = {
            if (step == 1) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Paste dữ liệu tab-separated từ trang Khảo Thí:", fontSize = 13.sp, color = OnSurfaceMedium)
                    OutlinedTextField(value = rawText, onValueChange = { rawText = it },
                        modifier = Modifier.fillMaxWidth().height(160.dp), label = { Text("Dán dữ liệu vào đây") })
                }
            } else {
                LazyColumn(modifier = Modifier.height(300.dp)) {
                    itemsIndexed(parsed) { _, score ->
                        Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                            Text(score.subjectName, modifier = Modifier.weight(3f), fontSize = 12.sp)
                            Text("${score.subjectCredit}TC", modifier = Modifier.weight(0.8f), fontSize = 12.sp)
                            Text(score.scoreText, modifier = Modifier.weight(0.7f), fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Purple)
                        }
                        Divider(color = Outline)
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (step == 1) { parsed = parseKhaoThiData(rawText); if (parsed.isNotEmpty()) step = 2 }
                    else { onImport(parsed); onDismiss() }
                },
                colors = ButtonDefaults.buttonColors(backgroundColor = Purple)
            ) { Text(if (step == 1) "Tiếp theo" else "Import", color = White) }
        },
        dismissButton = {
            TextButton(onClick = { if (step == 2) step = 1 else onDismiss() }) {
                Text(if (step == 2) "Quay lại" else "Hủy")
            }
        }
    )
}

private fun parseKhaoThiData(raw: String): List<VirtualScore> {
    return raw.lines().mapNotNull { line ->
        val cols = line.split("\t").map { it.trim() }
        if (cols.size < 4) return@mapNotNull null
        try {
            val offset = if (cols[0].toIntOrNull() != null) 1 else 0
            val name = cols.getOrNull(offset) ?: return@mapNotNull null
            val credit = cols.getOrNull(offset + 1)?.toIntOrNull() ?: return@mapNotNull null
            val tp1 = cols.getOrNull(offset + 2)?.toDoubleOrNull() ?: 0.0
            val tp2 = cols.getOrNull(offset + 3)?.toDoubleOrNull() ?: 0.0
            val ck = cols.getOrNull(offset + 4)?.toDoubleOrNull() ?: 0.0
            val overall = calculateScoreOverall(tp1, tp2, ck)
            VirtualScore(
                scoreText = scoreToLetter(overall), scoreFirst = tp1, scoreSecond = tp2,
                scoreFinal = ck, scoreOverall = overall, subjectName = name,
                subjectCredit = credit, isSelected = true
            )
        } catch (e: Exception) { null }
    }
}
