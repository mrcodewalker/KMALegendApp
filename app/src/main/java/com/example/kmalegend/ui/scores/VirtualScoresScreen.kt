package com.example.kmalegend.ui.scores

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.kmalegend.data.*
import com.example.kmalegend.ui.common.*
import com.example.kmalegend.ui.theme.*

// ─── Column widths ────────────────────────────────────────────────────────────
private val COL_CB     = 32.dp   // checkbox
private val COL_NAME   = 160.dp  // sticky subject name
private val COL_TC     = 40.dp
private val COL_SCORE  = 56.dp
private val COL_LETTER = 48.dp
private val COL_DEL    = 36.dp

@Composable
fun VirtualScoresScreen(navController: NavController, vm: VirtualScoresViewModel = viewModel()) {
    val uiState by vm.uiState.collectAsState()
    LaunchedEffect(Unit) { vm.init() }

    val virtualCPA     = vm.getVirtualCPA()
    val selectedTC     = uiState.virtualScores.filter { it.isSelected }.sumOf { it.subjectCredit }
    val allSelected    = uiState.virtualScores.isNotEmpty() && uiState.virtualScores.all { it.isSelected }

    var showCpaDialog      by remember { mutableStateOf(false) }
    var showAddDialog      by remember { mutableStateOf(false) }
    var showImportDialog   by remember { mutableStateOf(false) }
    var showSaveConfirm    by remember { mutableStateOf(false) }
    var showRestoreConfirm by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Bảng điểm ảo", fontWeight = FontWeight.Bold, color = White)
                        Text(
                            "CPA: ${"%.2f".format(virtualCPA)} • $selectedTC TC đã chọn",
                            fontSize = 12.sp, color = White.copy(alpha = 0.85f)
                        )
                    }
                },
                backgroundColor = KmaRed,
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
                // ── Action bar ──────────────────────────────────────────────
                ActionButtonsBar(
                    allSelected    = allSelected,
                    onSelectAll    = { vm.toggleSelectAll() },
                    onAddSubject   = { showAddDialog = true },
                    onImport       = { showImportDialog = true },
                    onRestore      = { showRestoreConfirm = true },
                    onCpaTarget    = { showCpaDialog = true },
                    onSave         = { showSaveConfirm = true }
                )

                // ── Table ───────────────────────────────────────────────────
                val hScroll = rememberScrollState()
                // Header: sticky left part + scrollable right part
                Row(
                    modifier = Modifier.fillMaxWidth().background(KmaRed),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Sticky: checkbox + name
                    Row(
                        modifier = Modifier.width(COL_CB + COL_NAME).padding(start = 4.dp, top = 8.dp, bottom = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Spacer(Modifier.width(COL_CB))
                        HeaderCell("Môn học", COL_NAME, TextAlign.Start)
                    }
                    // Scrollable score columns
                    Row(
                        modifier = Modifier.weight(1f).horizontalScroll(hScroll).padding(top = 8.dp, bottom = 8.dp, end = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        HeaderCell("TC",   COL_TC)
                        HeaderCell("GK",   COL_SCORE)
                        HeaderCell("CC",   COL_SCORE)
                        HeaderCell("CK",   COL_SCORE)
                        HeaderCell("TK",   COL_SCORE)
                        HeaderCell("Chữ",  COL_LETTER)
                        Spacer(Modifier.width(COL_DEL))
                    }
                }

                LazyColumn(modifier = Modifier.weight(1f)) {
                    itemsIndexed(uiState.virtualScores) { index, score ->
                        VirtualScoreRow(
                            score       = score,
                            hScroll     = hScroll,
                            onToggle    = { vm.toggleSelection(index) },
                            onDelete    = { vm.removeScore(index) },
                            onSave      = { updated -> vm.updateScore(index, updated) }
                        )
                    }
                    if (uiState.virtualScores.isEmpty() && !uiState.isLoading) {
                        item {
                            Box(
                                modifier = Modifier.fillMaxWidth().padding(48.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(Icons.Default.TableChart, contentDescription = null,
                                        tint = OnSurfaceMedium.copy(alpha = 0.4f), modifier = Modifier.size(48.dp))
                                    Spacer(Modifier.height(8.dp))
                                    Text("Chưa có môn học nào", color = OnSurfaceMedium)
                                    Text("Nhấn \"Thêm môn\" để bắt đầu", fontSize = 12.sp, color = OnSurfaceMedium.copy(alpha = 0.7f))
                                }
                            }
                        }
                    }
                }
            }

            // ── Toasts ──────────────────────────────────────────────────────
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

    // ── Dialogs ─────────────────────────────────────────────────────────────
    if (showCpaDialog)
        StatsDialog(scores = uiState.virtualScores, cpa = virtualCPA, onDismiss = { showCpaDialog = false })

    if (showAddDialog)
        AddSubjectDialog(onAdd = { vm.addScore(it) }, onDismiss = { showAddDialog = false })

    if (showImportDialog)
        ImportKhaoThiDialog(
            existingNames = uiState.virtualScores.map { it.subjectName },
            onImport      = { vm.importScores(it) },
            onDismiss     = { showImportDialog = false }
        )

    if (showSaveConfirm) ConfirmDialog(
        title       = "Lưu lên server",
        message     = "Lưu bảng điểm ảo hiện tại lên server?",
        confirmText = "Lưu",
        onConfirm   = { vm.saveToServer(); showSaveConfirm = false },
        onDismiss   = { showSaveConfirm = false }
    )
    if (showRestoreConfirm) ConfirmDialog(
        title       = "Khôi phục",
        message     = "Khôi phục bảng điểm về điểm gốc từ server?",
        confirmText = "Khôi phục",
        onConfirm   = { vm.restoreFromServer(); showRestoreConfirm = false },
        onDismiss   = { showRestoreConfirm = false }
    )
}

// ─── Action buttons bar ───────────────────────────────────────────────────────
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
        modifier          = Modifier.fillMaxWidth(),
        elevation         = 2.dp,
        shape             = RoundedCornerShape(0.dp),
        backgroundColor   = KmaRedSurface
    ) {
        Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ActionChip(
                    icon     = if (allSelected) Icons.Default.CheckBox else Icons.Default.CheckBoxOutlineBlank,
                    label    = if (allSelected) "Bỏ chọn tất cả" else "Chọn tất cả",
                    color    = KmaRed,
                    modifier = Modifier.weight(1f),
                    onClick  = onSelectAll
                )
                ActionChip(Icons.Default.Add, "Thêm môn", KmaBlue, Modifier.weight(1f), onAddSubject)
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ActionChip(Icons.Default.FileUpload, "Từ Khảo Thí", Color(0xFF2E7D32), Modifier.weight(1f), onImport)
                ActionChip(Icons.Default.Restore, "Khôi phục", Color(0xFFE65100), Modifier.weight(1f), onRestore)
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ActionChip(Icons.Default.Calculate, "Thống kê", Color(0xFF00695C), Modifier.weight(1f), onCpaTarget)
                ActionChip(Icons.Default.CloudUpload, "Lưu lên server", KmaRedDark, Modifier.weight(1f), onSave)
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
        onClick          = onClick,
        modifier         = modifier.height(40.dp),
        shape            = RoundedCornerShape(10.dp),
        colors           = ButtonDefaults.buttonColors(backgroundColor = color),
        contentPadding   = PaddingValues(horizontal = 8.dp),
        elevation        = ButtonDefaults.elevation(0.dp)
    ) {
        Icon(icon, contentDescription = null, tint = White, modifier = Modifier.size(16.dp))
        Spacer(Modifier.width(4.dp))
        Text(label, color = White, fontSize = 11.sp, fontWeight = FontWeight.Medium, maxLines = 1)
    }
}

// ─── Table helpers ────────────────────────────────────────────────────────────
@Composable
private fun HeaderCell(text: String, width: Dp, align: TextAlign = TextAlign.Center) {
    Text(
        text       = text,
        color      = White,
        fontWeight = FontWeight.SemiBold,
        fontSize   = 11.sp,
        textAlign  = align,
        modifier   = Modifier.width(width)
    )
}

@Composable
private fun DataCell(text: String, width: Dp, color: Color = OnSurfaceHigh, bold: Boolean = false, align: TextAlign = TextAlign.Center) {
    Text(
        text       = text,
        fontSize   = 11.sp,
        color      = color,
        fontWeight = if (bold) FontWeight.Bold else FontWeight.Normal,
        textAlign  = align,
        modifier   = Modifier.width(width),
        maxLines   = 2,
        overflow   = TextOverflow.Ellipsis
    )
}

// ─── Score row (sticky name + inline edit) ───────────────────────────────────
@Composable
private fun VirtualScoreRow(
    score: VirtualScore,
    hScroll: androidx.compose.foundation.ScrollState,
    onToggle: () -> Unit,
    onDelete: () -> Unit,
    onSave: (VirtualScore) -> Unit
) {
    var editing by remember { mutableStateOf(false) }
    var tp1     by remember(score) { mutableStateOf(if (score.scoreFirst  == 0.0) "" else "%.1f".format(score.scoreFirst)) }
    var tp2     by remember(score) { mutableStateOf(if (score.scoreSecond == 0.0) "" else "%.1f".format(score.scoreSecond)) }
    var ck      by remember(score) { mutableStateOf(if (score.scoreFinal  == 0.0) "" else "%.1f".format(score.scoreFinal)) }

    val t1Live      = tp1.toDoubleOrNull() ?: score.scoreFirst
    val t2Live      = tp2.toDoubleOrNull() ?: score.scoreSecond
    val ckLive      = ck.toDoubleOrNull()  ?: score.scoreFinal
    val overallLive = calculateScoreOverall(t1Live, t2Live, ckLive)

    val isFailed = isFailedSubject(ckLive, overallLive)
    val bg = when {
        editing           -> KmaRedSurface
        !score.isSelected -> SurfaceVariant
        isFailed          -> ErrorSurface
        else              -> White
    }
    val letterColor = when {
        isFailed             -> Error
        overallLive >= 9.0   -> GradeA
        overallLive >= 8.5   -> GradeA
        overallLive >= 7.8   -> GradeB
        overallLive >= 7.0   -> GradeB
        overallLive >= 6.3   -> GradeC
        overallLive >= 5.5   -> GradeC
        overallLive >= 4.0   -> GradeD
        else                 -> Error
    }
    val textAlpha = if (score.isSelected) 1f else 0.45f

    Column {
        Row(
            modifier = Modifier.fillMaxWidth().background(bg),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // ── Sticky left: checkbox + subject name ──────────────────────
            Row(
                modifier = Modifier
                    .width(COL_CB + COL_NAME)
                    .padding(start = 2.dp, top = 4.dp, bottom = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked         = score.isSelected,
                    onCheckedChange = { onToggle() },
                    modifier        = Modifier.size(COL_CB),
                    colors          = CheckboxDefaults.colors(checkedColor = KmaRed, uncheckedColor = OnSurfaceMedium)
                )
                Text(
                    text      = score.subjectName,
                    fontSize  = 11.sp,
                    color     = (if (score.isSelected) OnSurfaceHigh else OnSurfaceMedium).copy(alpha = textAlpha),
                    maxLines  = 2,
                    overflow  = TextOverflow.Ellipsis,
                    modifier  = Modifier
                        .width(COL_NAME)
                        .clickable { editing = !editing }
                        .padding(end = 4.dp)
                )
            }

            // ── Scrollable score columns ──────────────────────────────────
            Row(
                modifier = Modifier.weight(1f).horizontalScroll(hScroll).padding(end = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // TC
                Text("${score.subjectCredit}", fontSize = 11.sp, textAlign = TextAlign.Center,
                    color = OnSurfaceMedium.copy(alpha = textAlpha), modifier = Modifier.width(COL_TC))

                if (editing) {
                    // Inline score inputs
                    InlineScoreInput(tp1, { tp1 = it }, COL_SCORE)
                    InlineScoreInput(tp2, { tp2 = it }, COL_SCORE)
                    InlineScoreInput(ck,  { ck  = it }, COL_SCORE)
                    // TK live preview
                    Text("%.2f".format(overallLive), fontSize = 11.sp, fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center, color = KmaRed, modifier = Modifier.width(COL_SCORE))
                    // Letter live
                    Text(scoreToLetter(overallLive), fontSize = 11.sp, fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center, color = letterColor, modifier = Modifier.width(COL_LETTER))
                    // Save button
                    IconButton(
                        onClick = {
                            onSave(score.copy(
                                scoreFirst   = t1Live, scoreSecond = t2Live, scoreFinal = ckLive,
                                scoreOverall = overallLive, scoreText = scoreToLetter(overallLive)
                            ))
                            editing = false
                        },
                        modifier = Modifier.size(COL_DEL)
                    ) {
                        Icon(Icons.Default.Check, contentDescription = "Lưu",
                            tint = KmaRed, modifier = Modifier.size(18.dp))
                    }
                } else {
                    DataCell("%.1f".format(score.scoreFirst),   COL_SCORE, OnSurfaceMedium.copy(alpha = textAlpha))
                    DataCell("%.1f".format(score.scoreSecond),  COL_SCORE, OnSurfaceMedium.copy(alpha = textAlpha))
                    DataCell("%.1f".format(score.scoreFinal),   COL_SCORE, OnSurfaceMedium.copy(alpha = textAlpha))
                    DataCell("%.2f".format(score.scoreOverall), COL_SCORE,
                        color = (if (score.isSelected) OnSurfaceHigh else OnSurfaceMedium).copy(alpha = textAlpha), bold = true)
                    DataCell(scoreToLetter(score.scoreOverall), COL_LETTER,
                        color = (if (score.isSelected) letterColor else OnSurfaceMedium).copy(alpha = textAlpha), bold = true)
                    IconButton(onClick = onDelete, modifier = Modifier.size(COL_DEL)) {
                        Icon(Icons.Default.DeleteOutline, contentDescription = null,
                            tint = Error.copy(alpha = 0.65f), modifier = Modifier.size(18.dp))
                    }
                }
            }
        }
        Divider(color = Outline.copy(alpha = 0.5f))
    }
}

@Composable
private fun InlineScoreInput(value: String, onValueChange: (String) -> Unit, width: Dp) {
    val interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()
    Column(
        modifier            = Modifier.width(width).padding(horizontal = 3.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        androidx.compose.foundation.text.BasicTextField(
            value             = value,
            onValueChange     = onValueChange,
            singleLine        = true,
            keyboardOptions   = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            textStyle         = androidx.compose.ui.text.TextStyle(
                fontSize  = 11.sp,
                textAlign = TextAlign.Center,
                color     = OnSurfaceHigh,
                fontWeight = FontWeight.Medium
            ),
            interactionSource = interactionSource,
            modifier          = Modifier.fillMaxWidth()
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(if (isFocused) KmaRed else Outline)
        )
    }
}

// ─── Stats Dialog ─────────────────────────────────────────────────────────────
@Composable
fun StatsDialog(scores: List<VirtualScore>, cpa: Double, onDismiss: () -> Unit) {
    val selected = scores.filter { it.isSelected }
    val totalTC  = selected.sumOf { it.subjectCredit }
    val passCount = selected.count { !isFailedSubject(it.scoreFinal, it.scoreOverall) }
    val failCount = selected.size - passCount

    // Grade distribution
    val gradeOrder = listOf("A+","A","B+","B","C+","C","D+","D","F")
    val gradeColors = mapOf(
        "A+" to Color(0xFF1B5E20), "A"  to Color(0xFF2E7D32),
        "B+" to Color(0xFF1565C0), "B"  to Color(0xFF1976D2),
        "C+" to Color(0xFFF57F17), "C"  to Color(0xFFF9A825),
        "D+" to Color(0xFFE65100), "D"  to Color(0xFFBF360C),
        "F"  to Color(0xFFB71C1C)
    )
    val gradeCounts = gradeOrder.associateWith { g ->
        selected.count { scoreToLetter(it.scoreOverall) == g }
    }
    val maxGradeCount = gradeCounts.values.maxOrNull()?.coerceAtLeast(1) ?: 1

    // Score timeline (all selected, trimmed name)
    val timeline = selected.mapIndexed { i, s -> i to s.scoreOverall }

    androidx.compose.ui.window.Dialog(onDismissRequest = onDismiss) {
        Card(
            shape           = RoundedCornerShape(20.dp),
            elevation       = 8.dp,
            modifier        = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Title
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.BarChart, contentDescription = null, tint = KmaRed, modifier = Modifier.size(22.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Thống kê bảng điểm", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = OnSurfaceHigh)
                }

                // ── Summary chips ────────────────────────────────────────
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    SummaryChip("CPA", "%.2f".format(cpa), KmaRed, Modifier.weight(1f))
                    SummaryChip("TC", "$totalTC", KmaBlue, Modifier.weight(1f))
                    SummaryChip("Pass", "$passCount", Color(0xFF2E7D32), Modifier.weight(1f))
                    SummaryChip("Fail", "$failCount", Error, Modifier.weight(1f))
                }

                // ── Bar chart: grade distribution ────────────────────────
                StatSectionTitle("Phân bố điểm chữ")
                GradeBarChart(
                    gradeCounts = gradeCounts,
                    gradeOrder  = gradeOrder,
                    gradeColors = gradeColors,
                    maxCount    = maxGradeCount
                )

                // ── Line chart: score timeline ───────────────────────────
                if (timeline.size >= 2) {
                    StatSectionTitle("Điểm TK theo môn học")
                    ScoreLineChart(timeline = timeline)
                }

                // ── Grade breakdown list ─────────────────────────────────
                StatSectionTitle("Chi tiết phân bố")
                GradeBreakdownList(gradeCounts, gradeOrder, gradeColors, selected.size)

                TextButton(
                    onClick  = onDismiss,
                    modifier = Modifier.align(Alignment.End)
                ) { Text("Đóng", color = KmaRed, fontWeight = FontWeight.SemiBold) }
            }
        }
    }
}

@Composable
private fun SummaryChip(label: String, value: String, color: Color, modifier: Modifier) {
    Card(
        modifier        = modifier,
        shape           = RoundedCornerShape(12.dp),
        backgroundColor = color.copy(alpha = 0.1f),
        elevation       = 0.dp
    ) {
        Column(
            modifier              = Modifier.padding(vertical = 8.dp, horizontal = 4.dp),
            horizontalAlignment   = Alignment.CenterHorizontally
        ) {
            Text(value, color = color, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Text(label, color = color.copy(alpha = 0.75f), fontSize = 10.sp)
        }
    }
}

@Composable
private fun StatSectionTitle(title: String) {
    Text(title, fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = OnSurfaceHigh)
}

@Composable
private fun GradeBarChart(
    gradeCounts: Map<String, Int>,
    gradeOrder: List<String>,
    gradeColors: Map<String, Color>,
    maxCount: Int
) {
    val barH = 22.dp
    val labelW = 28.dp
    val countW = 24.dp
    val animProgress = remember { androidx.compose.animation.core.Animatable(0f) }
    LaunchedEffect(gradeCounts) {
        animProgress.snapTo(0f)
        animProgress.animateTo(1f, animationSpec = androidx.compose.animation.core.tween(700))
    }
    val prog by animProgress.asState()

    Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
        gradeOrder.forEach { grade ->
            val count = gradeCounts[grade] ?: 0
            if (count == 0 && grade == "F") return@forEach // skip F if none
            val color = gradeColors[grade] ?: KmaRed
            val fraction = (count.toFloat() / maxCount) * prog
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(grade, fontSize = 11.sp, fontWeight = FontWeight.Bold,
                    color = color, modifier = Modifier.width(labelW), textAlign = TextAlign.Center)
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(barH)
                        .clip(RoundedCornerShape(4.dp))
                        .background(color.copy(alpha = 0.12f))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .fillMaxWidth(fraction)
                            .clip(RoundedCornerShape(4.dp))
                            .background(color)
                    )
                    if (count > 0) {
                        Text(
                            "$count môn",
                            fontSize = 10.sp, color = White,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.align(Alignment.CenterStart).padding(start = 6.dp)
                        )
                    }
                }
                Text("$count", fontSize = 11.sp, color = color,
                    fontWeight = FontWeight.Bold, modifier = Modifier.width(countW), textAlign = TextAlign.End)
            }
        }
    }
}

@Composable
private fun ScoreLineChart(timeline: List<Pair<Int, Double>>) {
    val animProgress = remember { androidx.compose.animation.core.Animatable(0f) }
    LaunchedEffect(timeline) {
        animProgress.snapTo(0f)
        animProgress.animateTo(1f, animationSpec = androidx.compose.animation.core.tween(900))
    }
    val prog by animProgress.asState()

    val chartH = 140.dp
    val yLabels = listOf(10.0, 8.5, 7.0, 5.5, 4.0, 0.0)

    Box(modifier = Modifier.fillMaxWidth().height(chartH + 24.dp)) {
        Canvas(modifier = Modifier.fillMaxWidth().height(chartH).align(Alignment.TopStart)) {
            val w = size.width
            val h = size.height
            val minY = 0.0; val maxY = 10.0
            val yRange = maxY - minY

            fun xOf(i: Int) = if (timeline.size == 1) w / 2f else i.toFloat() / (timeline.size - 1) * w
            fun yOf(v: Double) = h - ((v - minY) / yRange * h).toFloat()

            // Grid lines
            yLabels.forEach { yVal ->
                val yPx = yOf(yVal)
                drawLine(
                    color = androidx.compose.ui.graphics.Color(0xFFDADCE0),
                    start = androidx.compose.ui.geometry.Offset(0f, yPx),
                    end   = androidx.compose.ui.geometry.Offset(w, yPx),
                    strokeWidth = 1f
                )
            }

            // Threshold lines
            listOf(8.5 to androidx.compose.ui.graphics.Color(0xFF2E7D32),
                   7.0 to androidx.compose.ui.graphics.Color(0xFF1565C0),
                   5.5 to androidx.compose.ui.graphics.Color(0xFFF9A825),
                   4.0 to androidx.compose.ui.graphics.Color(0xFFD93025)).forEach { (yVal, col) ->
                val yPx = yOf(yVal)
                drawLine(color = col.copy(alpha = 0.35f),
                    start = androidx.compose.ui.geometry.Offset(0f, yPx),
                    end   = androidx.compose.ui.geometry.Offset(w, yPx),
                    strokeWidth = 1.5f,
                    pathEffect = androidx.compose.ui.graphics.PathEffect.dashPathEffect(floatArrayOf(8f, 6f))
                )
            }

            // Fill area under line
            val visibleCount = (timeline.size * prog).toInt().coerceAtLeast(1)
            val path = androidx.compose.ui.graphics.Path()
            path.moveTo(xOf(0), h)
            timeline.take(visibleCount).forEachIndexed { i, (_, v) ->
                if (i == 0) path.lineTo(xOf(i), yOf(v))
                else {
                    val prev = timeline[i - 1].second
                    val cx = (xOf(i - 1) + xOf(i)) / 2f
                    path.cubicTo(cx, yOf(prev), cx, yOf(v), xOf(i), yOf(v))
                }
            }
            path.lineTo(xOf(visibleCount - 1), h)
            path.close()
            drawPath(path, brush = androidx.compose.ui.graphics.Brush.verticalGradient(
                colors = listOf(androidx.compose.ui.graphics.Color(0xFFCC0000).copy(alpha = 0.25f),
                                androidx.compose.ui.graphics.Color(0xFFCC0000).copy(alpha = 0.0f)),
                startY = 0f, endY = h
            ))

            // Line
            val linePath = androidx.compose.ui.graphics.Path()
            timeline.take(visibleCount).forEachIndexed { i, (_, v) ->
                if (i == 0) linePath.moveTo(xOf(i), yOf(v))
                else {
                    val prev = timeline[i - 1].second
                    val cx = (xOf(i - 1) + xOf(i)) / 2f
                    linePath.cubicTo(cx, yOf(prev), cx, yOf(v), xOf(i), yOf(v))
                }
            }
            drawPath(linePath, color = androidx.compose.ui.graphics.Color(0xFFCC0000),
                style = androidx.compose.ui.graphics.drawscope.Stroke(width = 2.5f,
                    cap = androidx.compose.ui.graphics.StrokeCap.Round))

            // Dots
            timeline.take(visibleCount).forEachIndexed { i, (_, v) ->
                val dotColor = when {
                    v >= 8.5 -> androidx.compose.ui.graphics.Color(0xFF2E7D32)
                    v >= 7.0 -> androidx.compose.ui.graphics.Color(0xFF1565C0)
                    v >= 5.5 -> androidx.compose.ui.graphics.Color(0xFFF9A825)
                    v >= 4.0 -> androidx.compose.ui.graphics.Color(0xFFE65100)
                    else     -> androidx.compose.ui.graphics.Color(0xFFD93025)
                }
                drawCircle(color = androidx.compose.ui.graphics.Color.White, radius = 5f,
                    center = androidx.compose.ui.geometry.Offset(xOf(i), yOf(v)))
                drawCircle(color = dotColor, radius = 3.5f,
                    center = androidx.compose.ui.geometry.Offset(xOf(i), yOf(v)))
            }
        }

        // Y-axis labels
        Column(
            modifier = Modifier.align(Alignment.TopEnd).height(chartH),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            yLabels.forEach { v ->
                Text("%.0f".format(v), fontSize = 9.sp, color = OnSurfaceMedium,
                    modifier = Modifier.padding(start = 4.dp))
            }
        }
    }

    // Legend
    Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.padding(top = 4.dp)) {
        listOf("A" to Color(0xFF2E7D32), "B" to Color(0xFF1565C0),
               "C" to Color(0xFFF9A825), "D" to Color(0xFFE65100), "F" to Color(0xFFD93025))
            .forEach { (label, color) ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.size(8.dp).background(color, RoundedCornerShape(2.dp)))
                    Spacer(Modifier.width(3.dp))
                    Text(label, fontSize = 10.sp, color = color)
                }
            }
    }
}

@Composable
private fun GradeBreakdownList(
    gradeCounts: Map<String, Int>,
    gradeOrder: List<String>,
    gradeColors: Map<String, Color>,
    total: Int
) {
    val gradeLabels = mapOf(
        "A+" to "Xuất sắc", "A" to "Giỏi", "B+" to "Khá giỏi", "B" to "Khá",
        "C+" to "Trung bình khá", "C" to "Trung bình",
        "D+" to "Trung bình yếu", "D" to "Yếu", "F" to "Kém"
    )
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        gradeOrder.forEach { grade ->
            val count = gradeCounts[grade] ?: 0
            if (count == 0) return@forEach
            val color = gradeColors[grade] ?: KmaRed
            val pct = if (total > 0) count * 100f / total else 0f
            Row(
                modifier = Modifier.fillMaxWidth()
                    .background(color.copy(alpha = 0.07f), RoundedCornerShape(8.dp))
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier.size(28.dp).background(color.copy(alpha = 0.15f), RoundedCornerShape(6.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(grade, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = color)
                }
                Spacer(Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(gradeLabels[grade] ?: grade, fontSize = 12.sp, color = OnSurfaceHigh)
                    Text("$count môn • ${"%.0f".format(pct)}%", fontSize = 10.sp, color = OnSurfaceMedium)
                }
            }
        }
    }
}

// ─── Add subject dialog ───────────────────────────────────────────────────────
@Composable
fun AddSubjectDialog(onAdd: (VirtualScore) -> Unit, onDismiss: () -> Unit) {
    var name    by remember { mutableStateOf("") }
    var credits by remember { mutableStateOf("3") }
    var tp1     by remember { mutableStateOf("") }
    var tp2     by remember { mutableStateOf("") }
    var ck      by remember { mutableStateOf("") }

    val t1      = tp1.toDoubleOrNull() ?: 0.0
    val t2      = tp2.toDoubleOrNull() ?: 0.0
    val c       = ck.toDoubleOrNull()  ?: 0.0
    val overall = calculateScoreOverall(t1, t2, c)
    val hasScores = tp1.isNotEmpty() || tp2.isNotEmpty() || ck.isNotEmpty()

    AlertDialog(
        onDismissRequest = onDismiss,
        shape            = RoundedCornerShape(20.dp),
        title            = { Text("Thêm môn học", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = name, onValueChange = { name = it },
                    label = { Text("Tên môn học") }, modifier = Modifier.fillMaxWidth(),
                    singleLine = true, shape = RoundedCornerShape(10.dp),
                    colors = TextFieldDefaults.outlinedTextFieldColors(focusedBorderColor = KmaRed, cursorColor = KmaRed))
                OutlinedTextField(value = credits, onValueChange = { credits = it },
                    label = { Text("Số tín chỉ") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(), singleLine = true, shape = RoundedCornerShape(10.dp),
                    colors = TextFieldDefaults.outlinedTextFieldColors(focusedBorderColor = KmaRed, cursorColor = KmaRed))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    ScoreField("GK",  tp1, { tp1 = it }, Modifier.weight(1f))
                    ScoreField("CC",  tp2, { tp2 = it }, Modifier.weight(1f))
                    ScoreField("CK",  ck,  { ck  = it }, Modifier.weight(1f))
                }
                if (hasScores) {
                    Card(backgroundColor = KmaRedSurface, shape = RoundedCornerShape(10.dp), elevation = 0.dp) {
                        Row(modifier = Modifier.fillMaxWidth().padding(10.dp), horizontalArrangement = Arrangement.SpaceEvenly) {
                            Text("TK: ${"%.2f".format(overall)}", fontWeight = FontWeight.Bold, color = KmaRed)
                            Text("Chữ: ${scoreToLetter(overall)}", fontWeight = FontWeight.Bold, color = KmaRed)
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isBlank()) return@Button
                    onAdd(VirtualScore(
                        scoreText    = scoreToLetter(overall), scoreFirst  = t1,
                        scoreSecond  = t2,                     scoreFinal  = c,
                        scoreOverall = overall,                subjectName = name,
                        subjectCredit = credits.toIntOrNull() ?: 3, isSelected = true
                    ))
                    onDismiss()
                },
                colors    = ButtonDefaults.buttonColors(backgroundColor = KmaRed),
                shape     = RoundedCornerShape(10.dp),
                elevation = ButtonDefaults.elevation(0.dp)
            ) { Text("Thêm", color = White, fontWeight = FontWeight.SemiBold) }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Hủy", color = OnSurfaceMedium) } }
    )
}

// ─── Edit subject dialog removed — editing is now inline on the row ──────────

@Composable
private fun ScoreField(label: String, value: String, onValueChange: (String) -> Unit, modifier: Modifier) {
    OutlinedTextField(
        value           = value,
        onValueChange   = onValueChange,
        label           = { Text(label, fontSize = 11.sp) },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
        modifier        = modifier,
        singleLine      = true,
        shape           = RoundedCornerShape(10.dp),
        colors          = TextFieldDefaults.outlinedTextFieldColors(focusedBorderColor = KmaRed, cursorColor = KmaRed)
    )
}

// ─── Import Khảo Thí dialog ───────────────────────────────────────────────────
@Composable
fun ImportKhaoThiDialog(
    existingNames: List<String>,
    onImport: (List<VirtualScore>) -> Unit,
    onDismiss: () -> Unit
) {
    var rawText by remember { mutableStateOf("") }
    var step    by remember { mutableStateOf(1) }
    var parsed  by remember { mutableStateOf<List<VirtualScore>>(emptyList()) }

    // Normalise for duplicate check
    fun normalise(s: String) = s.trim().lowercase()
    val existingNorm = existingNames.map { normalise(it) }.toSet()

    AlertDialog(
        onDismissRequest = onDismiss,
        shape            = RoundedCornerShape(20.dp),
        title = {
            Text(
                if (step == 1) "Import từ Khảo Thí (1/2)" else "Xem trước (2/2)",
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            if (step == 1) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Card(backgroundColor = KmaRedSurface, shape = RoundedCornerShape(10.dp), elevation = 0.dp) {
                        Text(
                            "Vào trang Khảo Thí → chọn tất cả bảng điểm → Copy → Paste vào đây.",
                            modifier = Modifier.padding(10.dp),
                            fontSize = 12.sp, color = OnSurfaceMedium, lineHeight = 18.sp
                        )
                    }
                    OutlinedTextField(
                        value         = rawText,
                        onValueChange = { rawText = it },
                        modifier      = Modifier.fillMaxWidth().height(160.dp),
                        label         = { Text("Dán dữ liệu vào đây") },
                        shape         = RoundedCornerShape(10.dp),
                        colors        = TextFieldDefaults.outlinedTextFieldColors(focusedBorderColor = KmaRed, cursorColor = KmaRed)
                    )
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    // Legend
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.padding(bottom = 4.dp)) {
                        LegendDot(KmaRedSurface, KmaRed, "Đã tồn tại")
                        LegendDot(SuccessSurface, Success, "Mới")
                    }
                    LazyColumn(modifier = Modifier.height(280.dp)) {
                        itemsIndexed(parsed) { _, score ->
                            val isDuplicate = existingNorm.contains(normalise(score.subjectName))
                            val rowBg       = if (isDuplicate) KmaRedSurface else SuccessSurface
                            val nameColor   = if (isDuplicate) KmaRed.copy(alpha = 0.6f) else OnSurfaceHigh
                            val textAlpha   = if (isDuplicate) 0.55f else 1f
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(rowBg)
                                    .padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                if (isDuplicate)
                                    Icon(Icons.Default.Warning, contentDescription = null,
                                        tint = KmaRed.copy(alpha = 0.6f), modifier = Modifier.size(14.dp).padding(end = 2.dp))
                                else
                                    Icon(Icons.Default.CheckCircle, contentDescription = null,
                                        tint = Success, modifier = Modifier.size(14.dp).padding(end = 2.dp))
                                Spacer(Modifier.width(4.dp))
                                Text(score.subjectName, modifier = Modifier.weight(1f),
                                    fontSize = 12.sp, color = nameColor.copy(alpha = textAlpha), maxLines = 2, overflow = TextOverflow.Ellipsis)
                                Text("${score.subjectCredit}TC", fontSize = 11.sp,
                                    color = OnSurfaceMedium.copy(alpha = textAlpha), modifier = Modifier.padding(horizontal = 6.dp))
                                Text(scoreToLetter(score.scoreOverall), fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = (if (isDuplicate) KmaRed else Success).copy(alpha = textAlpha))
                            }
                            Divider(color = Outline.copy(alpha = 0.4f))
                        }
                    }
                    val dupCount = parsed.count { existingNorm.contains(normalise(it.subjectName)) }
                    val newCount = parsed.size - dupCount
                    Text("$newCount môn mới • $dupCount môn trùng (sẽ được thêm thêm vào)",
                        fontSize = 11.sp, color = OnSurfaceMedium)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (step == 1) {
                        parsed = parseKhaoThiData(rawText)
                        if (parsed.isNotEmpty()) step = 2
                    } else {
                        onImport(parsed)
                        onDismiss()
                    }
                },
                colors    = ButtonDefaults.buttonColors(backgroundColor = KmaRed),
                shape     = RoundedCornerShape(10.dp),
                elevation = ButtonDefaults.elevation(0.dp)
            ) { Text(if (step == 1) "Tiếp theo" else "Import", color = White, fontWeight = FontWeight.SemiBold) }
        },
        dismissButton = {
            TextButton(onClick = { if (step == 2) step = 1 else onDismiss() }) {
                Text(if (step == 2) "Quay lại" else "Hủy", color = OnSurfaceMedium)
            }
        }
    )
}

@Composable
private fun LegendDot(bg: Color, fg: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(modifier = Modifier.size(10.dp).background(bg, RoundedCornerShape(3.dp)))
        Spacer(Modifier.width(4.dp))
        Text(label, fontSize = 11.sp, color = fg)
    }
}

// ─── Parse helper ─────────────────────────────────────────────────────────────
private fun parseKhaoThiData(raw: String): List<VirtualScore> {
    return raw.lines().mapNotNull { line ->
        val cols = line.split("\t").map { it.trim() }
        if (cols.size < 4) return@mapNotNull null
        try {
            val offset  = if (cols[0].toIntOrNull() != null) 1 else 0
            val name    = cols.getOrNull(offset)          ?: return@mapNotNull null
            val credit  = cols.getOrNull(offset + 1)?.toIntOrNull() ?: return@mapNotNull null
            val tp1     = cols.getOrNull(offset + 2)?.toDoubleOrNull() ?: 0.0
            val tp2     = cols.getOrNull(offset + 3)?.toDoubleOrNull() ?: 0.0
            val ck      = cols.getOrNull(offset + 4)?.toDoubleOrNull() ?: 0.0
            val overall = calculateScoreOverall(tp1, tp2, ck)
            VirtualScore(
                scoreText     = scoreToLetter(overall), scoreFirst  = tp1,
                scoreSecond   = tp2,                    scoreFinal  = ck,
                scoreOverall  = overall,                subjectName = name,
                subjectCredit = credit,                 isSelected  = true
            )
        } catch (e: Exception) { null }
    }
}
