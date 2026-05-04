package com.example.kmalegend.ui.virtualcalendar

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.kmalegend.data.*
import com.example.kmalegend.ui.common.*
import com.example.kmalegend.ui.navigation.Routes
import com.example.kmalegend.ui.schedule.getColorFromSeed
import com.example.kmalegend.ui.theme.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun VirtualCalendarScreen(navController: NavController) {
    val context = LocalContext.current
    val prefs = remember { PrefsManager(context) }
    val repository = remember { Repository(prefs) }
    var virtualData by remember { mutableStateOf(prefs.getVirtualCalendarSecret()) }
    var isLoading by remember { mutableStateOf(virtualData?.data == null) }
    var error by remember { mutableStateOf<String?>(null) }
    LaunchedEffect(Unit) {
        if (virtualData?.data == null) {
            when (val result = repository.loadVirtualCalendar()) {
                is Result.Success -> { virtualData = prefs.getVirtualCalendarSecret(); isLoading = false }
                is Result.Error -> { error = result.message; isLoading = false }
            }
        } else isLoading = false
    }
    if (!prefs.isLoggedIn()) {
        LaunchedEffect(Unit) { navController.navigate(Routes.LOGIN) { popUpTo(0) { inclusive = true } } }
        return
    }
    if (isLoading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                CircularProgressIndicator()
                Spacer(Modifier.height(8.dp))
                Text("Đang tải lịch ảo...")
            }
        }
        return
    }
    error?.let {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(24.dp)) {
                Text(it, color = Error, textAlign = TextAlign.Center)
                Spacer(Modifier.height(16.dp))
                Button(onClick = { navController.popBackStack() }) { Text("Quay lại") }
            }
        }
        return
    }
    val allItems = remember(virtualData) { virtualData?.data?.virtual_calendar ?: emptyList() }
    var savedClasses by remember { mutableStateOf(prefs.getSavedClasses()) }
    var selectedTab by remember { mutableStateOf(0) }
    var showClearDialog by remember { mutableStateOf(false) }
    var showLogoutDialog by remember { mutableStateOf(false) }
    var showImportDialog by remember { mutableStateOf(false) }
    var shareToast by remember { mutableStateOf(false) }
    val context2 = LocalContext.current
    fun buildShareText(): String = savedClasses.joinToString("\n") { item ->
        "${item.details.course_name} ||| ${item.base_time} ||| ${item.details.teacher}"
    }
    fun parseImportGroups(text: String): Map<String, List<VirtualCalendarItem>> {
        val result = linkedMapOf<String, List<VirtualCalendarItem>>()
        text.lines().forEach { line ->
            val trimmed = line.trim().ifEmpty { return@forEach }
            val parts = trimmed.split(" ||| ")
            val found: VirtualCalendarItem? = if (parts.size == 3) {
                val key = "${parts[1].trim()}|${parts[2].trim()}"
                allItems.firstOrNull { classKey(it) == key }
            } else {
                allItems.firstOrNull { it.details.course_name.equals(trimmed, ignoreCase = true) }
                    ?: allItems.firstOrNull { it.details.course_name.contains(trimmed, ignoreCase = true) }
            }
            if (found != null) result[trimmed] = listOf(found)
        }
        return result
    }
    Scaffold(
        topBar = {
            KmaTopBar(title = "Lịch ảo", navController = navController, showBack = true,
                actions = {
                    IconButton(onClick = { showImportDialog = true }) {
                        Icon(Icons.Default.FileDownload, contentDescription = null, tint = White)
                    }
                    IconButton(onClick = {
                        val clip = android.content.ClipData.newPlainText("lịch ảo", buildShareText())
                        (context2.getSystemService(android.content.Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager).setPrimaryClip(clip)
                        shareToast = true
                    }) { Icon(Icons.Default.ContentCopy, contentDescription = null, tint = White) }
                    IconButton(onClick = { showClearDialog = true }) {
                        Icon(Icons.Default.DeleteSweep, contentDescription = null, tint = White)
                    }
                    IconButton(onClick = { showLogoutDialog = true }) {
                        Icon(Icons.Default.Logout, contentDescription = null, tint = White)
                    }
                }
            )
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            Column(modifier = Modifier.fillMaxSize()) {
                TabRow(selectedTabIndex = selectedTab, backgroundColor = MaterialTheme.colors.primary, contentColor = White) {
                    Tab(selected = selectedTab == 0, onClick = { selectedTab = 0 },
                        text = { Text("Chọn lớp", fontWeight = FontWeight.Medium) })
                    Tab(selected = selectedTab == 1, onClick = { selectedTab = 1 },
                        text = { Text("Xem lịch (${savedClasses.size})", fontWeight = FontWeight.Medium) })
                }
                when (selectedTab) {
                    0 -> PickerTab(allItems, savedClasses) { savedClasses = it; prefs.saveSavedClasses(it) }
                    1 -> ScheduleTab(savedClasses)
                }
            }
            if (shareToast) {
                Box(modifier = Modifier.align(Alignment.BottomCenter)) {
                    ToastMessage("Đã copy lịch vào clipboard", ToastType.SUCCESS) { shareToast = false }
                }
            }
        }
    }
    if (showClearDialog) ConfirmDialog(
        title = "Xóa tất cả", message = "Xóa toàn bộ lớp đã chọn?", confirmText = "Xóa",
        onConfirm = { savedClasses = emptyList(); prefs.saveSavedClasses(emptyList()); showClearDialog = false },
        onDismiss = { showClearDialog = false }
    )
    if (showLogoutDialog) ConfirmDialog(
        title = "Đăng xuất", message = "Bạn có chắc muốn đăng xuất?", confirmText = "Đăng xuất",
        onConfirm = { prefs.logout(); navController.navigate(Routes.LOGIN) { popUpTo(0) { inclusive = true } } },
        onDismiss = { showLogoutDialog = false }
    )
    if (showImportDialog) ImportScheduleDialog(
        onImport = { imported ->
            val merged = savedClasses.toMutableList()
            imported.forEach { item ->
                merged.removeAll { baseSubjectName(it.course_name) == baseSubjectName(item.course_name) }
                merged.add(item)
            }
            savedClasses = merged; prefs.saveSavedClasses(merged); showImportDialog = false
        },
        onDismiss = { showImportDialog = false },
        parseGroups = { parseImportGroups(it) }
    )
}

private fun baseSubjectName(courseName: String): String = courseName.substringBefore("(").trim()
private fun classKey(item: VirtualCalendarItem) = "${item.base_time}|${item.details.teacher}"
private enum class SheetType { BLOCK, SUBJECT, NONE }

@OptIn(ExperimentalMaterialApi::class)
@Composable
private fun PickerTab(
    allItems: List<VirtualCalendarItem>,
    savedClasses: List<VirtualCalendarItem>,
    onSavedChange: (List<VirtualCalendarItem>) -> Unit
) {
    val courseBlocks = remember(allItems) { allItems.map { it.course }.distinct().sorted() }
    var selectedBlock by remember { mutableStateOf(courseBlocks.firstOrNull() ?: "") }
    val subjectsForBlock by remember(selectedBlock, allItems) {
        derivedStateOf { allItems.filter { it.course == selectedBlock }.map { baseSubjectName(it.course_name) }.distinct().sorted() }
    }
    var selectedSubject by remember { mutableStateOf("") }
    val classesForSubject by remember(selectedSubject, selectedBlock, allItems) {
        derivedStateOf { allItems.filter { it.course == selectedBlock && baseSubjectName(it.course_name) == selectedSubject } }
    }
    val selectedClassKey by derivedStateOf {
        savedClasses.firstOrNull { baseSubjectName(it.course_name) == selectedSubject }?.let { classKey(it) }
    }
    val blocksWithSelection by derivedStateOf { savedClasses.map { it.course }.toSet() }
    val subjectsWithSelection by derivedStateOf {
        savedClasses.filter { it.course == selectedBlock }.map { baseSubjectName(it.course_name) }.toSet()
    }
    var activeSheet by remember { mutableStateOf(SheetType.NONE) }
    val sheetState = rememberModalBottomSheetState(initialValue = ModalBottomSheetValue.Hidden, skipHalfExpanded = true)
    val scope = rememberCoroutineScope()
    var conflictMsg by remember { mutableStateOf<String?>(null) }
    LaunchedEffect(activeSheet) { if (activeSheet != SheetType.NONE) sheetState.show() }
    LaunchedEffect(sheetState.isVisible) { if (!sheetState.isVisible) activeSheet = SheetType.NONE }
    fun checkConflict(item: VirtualCalendarItem, against: List<VirtualCalendarItem>): String? {
        val newDays = item.details.study_days.split(" ").map { it.trim() }.toSet()
        val newTime = getLessonTime(item.details.lessons.split(" ").firstOrNull() ?: "")
        for (saved in against) {
            val savedDays = saved.details.study_days.split(" ").map { it.trim() }.toSet()
            val savedTime = getLessonTime(saved.details.lessons.split(" ").firstOrNull() ?: "")
            if (newDays.intersect(savedDays).isNotEmpty() && newTime.start == savedTime.start)
                return "Trùng lịch với: ${saved.details.course_name}"
        }
        return null
    }
    ModalBottomSheetLayout(
        sheetState = sheetState,
        sheetShape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
        sheetBackgroundColor = White,
        sheetContent = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Box(modifier = Modifier.fillMaxWidth().padding(top = 12.dp, bottom = 8.dp), contentAlignment = Alignment.Center) {
                    Box(modifier = Modifier.width(40.dp).height(4.dp).background(Outline, RoundedCornerShape(2.dp)))
                }
                when (activeSheet) {
                    SheetType.BLOCK -> {
                        Text("Chọn khối học phần", fontWeight = FontWeight.Bold, fontSize = 17.sp,
                            modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp))
                        Column(modifier = Modifier.verticalScroll(rememberScrollState()).padding(bottom = 24.dp)) {
                            courseBlocks.forEach { block ->
                                val isCurrent = block == selectedBlock
                                val hasSel = block in blocksWithSelection
                                Row(modifier = Modifier.fillMaxWidth()
                                    .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) {
                                        selectedBlock = block; selectedSubject = ""; scope.launch { sheetState.hide() }
                                    }
                                    .background(when { isCurrent -> Success.copy(alpha = 0.08f); hasSel -> Success.copy(alpha = 0.04f); else -> Color.Transparent })
                                    .padding(horizontal = 20.dp, vertical = 14.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(block, modifier = Modifier.weight(1f),
                                        fontWeight = if (isCurrent || hasSel) FontWeight.SemiBold else FontWeight.Normal,
                                        color = if (isCurrent || hasSel) Success else OnSurfaceHigh, fontSize = 15.sp)
                                    when {
                                        isCurrent -> Icon(Icons.Default.Check, contentDescription = null, tint = Success, modifier = Modifier.size(20.dp))
                                        hasSel -> Box(modifier = Modifier.size(8.dp).background(Success, CircleShape))
                                    }
                                }
                                Divider(color = Outline.copy(alpha = 0.4f), modifier = Modifier.padding(horizontal = 20.dp))
                            }
                        }
                    }
                    SheetType.SUBJECT -> {
                        Text("Chọn môn học", fontWeight = FontWeight.Bold, fontSize = 17.sp,
                            modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp))
                        Column(modifier = Modifier.verticalScroll(rememberScrollState()).padding(bottom = 24.dp)) {
                            subjectsForBlock.forEach { subject ->
                                val isCurrent = subject == selectedSubject
                                val hasSel = subject in subjectsWithSelection
                                Row(modifier = Modifier.fillMaxWidth()
                                    .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) {
                                        selectedSubject = subject; scope.launch { sheetState.hide() }
                                    }
                                    .background(when { isCurrent -> Success.copy(alpha = 0.08f); hasSel -> Success.copy(alpha = 0.04f); else -> Color.Transparent })
                                    .padding(horizontal = 20.dp, vertical = 14.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(subject, fontWeight = if (isCurrent || hasSel) FontWeight.SemiBold else FontWeight.Normal,
                                            color = if (isCurrent || hasSel) Success else OnSurfaceHigh, fontSize = 14.sp)
                                        if (hasSel) {
                                            val chosen = savedClasses.firstOrNull { baseSubjectName(it.course_name) == subject }
                                            if (chosen != null) Text("✓ ${chosen.details.course_name}", fontSize = 11.sp, color = Success.copy(alpha = 0.85f))
                                        }
                                    }
                                    when {
                                        isCurrent -> Icon(Icons.Default.Check, contentDescription = null, tint = Success, modifier = Modifier.size(20.dp))
                                        hasSel -> Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Success, modifier = Modifier.size(18.dp))
                                    }
                                }
                                Divider(color = Outline.copy(alpha = 0.4f), modifier = Modifier.padding(horizontal = 20.dp))
                            }
                        }
                    }
                    SheetType.NONE -> Spacer(Modifier.height(1.dp))
                }
            }
        }
    ) {
        LazyColumn(modifier = Modifier.fillMaxSize()) {
            item {
                Card(modifier = Modifier.fillMaxWidth().padding(12.dp), elevation = 0.dp,
                    shape = RoundedCornerShape(16.dp), backgroundColor = White) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text("Xếp lịch học", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = OnSurfaceHigh)
                        PickerButton(label = "Khối học phần", value = selectedBlock.ifEmpty { null },
                            placeholder = "Chọn khối học phần", hasSelection = selectedBlock in blocksWithSelection,
                            onClick = { activeSheet = SheetType.BLOCK; scope.launch { sheetState.show() } })
                        if (selectedBlock.isNotEmpty()) {
                            PickerButton(label = "Môn học", value = selectedSubject.ifEmpty { null },
                                placeholder = "Chọn môn học", hasSelection = selectedSubject in subjectsWithSelection,
                                onClick = { activeSheet = SheetType.SUBJECT; scope.launch { sheetState.show() } })
                        }
                        if (selectedSubject.isNotEmpty() && classesForSubject.isNotEmpty()) {
                            Divider(color = Outline)
                            Text("Chọn lớp  •  ${classesForSubject.size} lớp", fontSize = 12.sp, color = OnSurfaceMedium, fontWeight = FontWeight.Medium)
                            classesForSubject.forEach { item ->
                                val isSelected = classKey(item) == selectedClassKey
                                val noRipple = remember { MutableInteractionSource() }
                                Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                                    .background(if (isSelected) KmaRed.copy(alpha = 0.08f) else SurfaceVariant, CircleShape)
                                    .border(width = if (isSelected) 1.5.dp else 1.dp,
                                        color = if (isSelected) KmaRed else Outline, shape = CircleShape)
                                    .clickable(interactionSource = noRipple, indication = null) {
                                        if (isSelected) {
                                            onSavedChange(savedClasses.filter { classKey(it) != classKey(item) })
                                        } else {
                                            val without = savedClasses.filter { baseSubjectName(it.course_name) != selectedSubject }
                                            val conflict = checkConflict(item, without)
                                            if (conflict != null) conflictMsg = conflict else onSavedChange(without + item)
                                        }
                                    }
                                    .padding(horizontal = 12.dp, vertical = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(if (isSelected) Icons.Default.RadioButtonChecked else Icons.Default.RadioButtonUnchecked,
                                        contentDescription = null, tint = if (isSelected) KmaRed else OnSurfaceMedium, modifier = Modifier.size(20.dp))
                                    Spacer(Modifier.width(10.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(item.details.course_name, fontSize = 13.sp,
                                            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                                            color = if (isSelected) KmaRed else OnSurfaceHigh)
                                        Text(item.base_time, fontSize = 11.sp, color = OnSurfaceMedium)
                                        Text("GV: ${item.details.teacher}", fontSize = 11.sp, color = OnSurfaceMedium)
                                    }
                                    if (isSelected) Icon(Icons.Default.CheckCircle, contentDescription = null, tint = KmaRed, modifier = Modifier.size(18.dp))
                                }
                            }
                        }
                    }
                }
            }
            item {
                Row(modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text("Lớp đã chọn", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = OnSurfaceHigh)
                    Spacer(Modifier.width(8.dp))
                    if (savedClasses.isNotEmpty()) {
                        Card(shape = RoundedCornerShape(6.dp), backgroundColor = KmaRed, elevation = 0.dp) {
                            Text("${savedClasses.size}", color = White, fontSize = 11.sp, fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp))
                        }
                    }
                }
            }
            if (savedClasses.isEmpty()) {
                item {
                    Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.EventNote, contentDescription = null, tint = Outline, modifier = Modifier.size(36.dp))
                            Spacer(Modifier.height(8.dp))
                            Text("Chưa chọn lớp nào", color = OnSurfaceMedium, fontSize = 13.sp)
                        }
                    }
                }
            } else {
                items(savedClasses) { item ->
                    Card(modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 4.dp),
                        elevation = 0.dp, shape = RoundedCornerShape(12.dp), backgroundColor = White) {
                        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.width(4.dp).height(50.dp).background(KmaRed, RoundedCornerShape(2.dp)))
                            Spacer(Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(item.details.course_name, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                                Text(item.base_time, fontSize = 11.sp, color = OnSurfaceMedium)
                                Text("GV: ${item.details.teacher} | ${item.details.study_location}", fontSize = 11.sp, color = OnSurfaceMedium)
                            }
                            IconButton(onClick = { onSavedChange(savedClasses.filter { classKey(it) != classKey(item) }) }) {
                                Icon(Icons.Default.Close, contentDescription = null, tint = Error, modifier = Modifier.size(18.dp))
                            }
                        }
                    }
                }
            }
            item { Spacer(Modifier.height(16.dp)) }
        }
    }
    conflictMsg?.let { msg ->
        AlertDialog(onDismissRequest = { conflictMsg = null }, shape = RoundedCornerShape(16.dp),
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Warning, contentDescription = null, tint = Warning, modifier = Modifier.size(22.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Trùng lịch", color = Warning, fontWeight = FontWeight.Bold)
                }
            },
            text = { Text(msg, color = OnSurfaceHigh) },
            confirmButton = {
                Button(onClick = { conflictMsg = null }, shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(backgroundColor = Warning),
                    elevation = ButtonDefaults.elevation(0.dp)) { Text("Đóng", color = White) }
            }
        )
    }
}

@Composable
private fun PickerButton(label: String, value: String?, placeholder: String, hasSelection: Boolean = false, onClick: () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(label, fontSize = 12.sp, color = OnSurfaceMedium, fontWeight = FontWeight.Medium)
            if (hasSelection) {
                Spacer(Modifier.width(6.dp))
                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = KmaRed, modifier = Modifier.size(13.dp))
            }
        }
        OutlinedButton(onClick = onClick, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(10.dp),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = if (hasSelection) KmaRed else if (value != null) OnSurfaceHigh else OnSurfaceMedium),
            border = androidx.compose.foundation.BorderStroke(width = if (hasSelection) 1.5.dp else 1.dp, color = if (hasSelection) KmaRed else Outline),
            interactionSource = remember { MutableInteractionSource() }
        ) {
            Text(value ?: placeholder, modifier = Modifier.weight(1f),
                fontWeight = if (value != null) FontWeight.Medium else FontWeight.Normal, fontSize = 14.sp, maxLines = 1,
                color = if (hasSelection) KmaRed else if (value != null) OnSurfaceHigh else OnSurfaceMedium)
            Icon(Icons.Default.KeyboardArrowDown, contentDescription = null, tint = if (hasSelection) KmaRed else OnSurfaceMedium)
        }
    }
}

@Composable
private fun ScheduleTab(savedClasses: List<VirtualCalendarItem>) {
    var currentMonth by remember { mutableStateOf(Calendar.getInstance()) }
    var selectedDate by remember { mutableStateOf(Calendar.getInstance()) }
    var selectedEvent by remember { mutableStateOf<VirtualCalendarItem?>(null) }
    val eventMap = remember(savedClasses) {
        val map = mutableMapOf<String, MutableList<VirtualCalendarItem>>()
        savedClasses.forEach { item ->
            item.details.study_days.split(" ").forEach { dayStr ->
                val key = dayStr.trim()
                if (key.isNotEmpty()) map.getOrPut(key) { mutableListOf() }.add(item)
            }
        }
        map
    }
    val eventColorMap = remember(savedClasses) {
        val map = mutableMapOf<String, MutableList<Color>>()
        savedClasses.forEach { item ->
            val color = getColorFromSeed(item.details.course_name)
            item.details.study_days.split(" ").forEach { dayStr ->
                val key = dayStr.trim()
                if (key.isNotEmpty()) map.getOrPut(key) { mutableListOf() }.add(color)
            }
        }
        map
    }
    val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    val selectedDateStr = sdf.format(selectedDate.time)
    val eventsForSelected = eventMap[selectedDateStr] ?: emptyList()
    Column(modifier = Modifier.fillMaxSize()) {
        Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
            IconButton(onClick = { currentMonth = (currentMonth.clone() as Calendar).apply { add(Calendar.MONTH, -1) } }) {
                Icon(Icons.Default.ChevronLeft, contentDescription = null)
            }
            Text(SimpleDateFormat("MMMM yyyy", Locale("vi")).format(currentMonth.time).replaceFirstChar { it.uppercase() },
                fontWeight = FontWeight.Bold, fontSize = 16.sp)
            IconButton(onClick = { currentMonth = (currentMonth.clone() as Calendar).apply { add(Calendar.MONTH, 1) } }) {
                Icon(Icons.Default.ChevronRight, contentDescription = null)
            }
        }
        Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp)) {
            listOf("CN", "T2", "T3", "T4", "T5", "T6", "T7").forEach { day ->
                Text(day, modifier = Modifier.weight(1f), textAlign = TextAlign.Center,
                    fontSize = 12.sp, fontWeight = FontWeight.Medium, color = OnSurfaceMedium)
            }
        }
        val firstDay = (currentMonth.clone() as Calendar).apply { set(Calendar.DAY_OF_MONTH, 1) }
        val startOffset = (firstDay.get(Calendar.DAY_OF_WEEK) - Calendar.SUNDAY + 7) % 7
        val daysInMonth = currentMonth.getActualMaximum(Calendar.DAY_OF_MONTH)
        val rows = (startOffset + daysInMonth + 6) / 7
        Column(modifier = Modifier.padding(horizontal = 8.dp)) {
            repeat(rows) { row ->
                Row(modifier = Modifier.fillMaxWidth()) {
                    repeat(7) { col ->
                        val day = row * 7 + col - startOffset + 1
                        if (day < 1 || day > daysInMonth) {
                            Spacer(modifier = Modifier.weight(1f).aspectRatio(1f))
                        } else {
                            val dayCal = (currentMonth.clone() as Calendar).apply { set(Calendar.DAY_OF_MONTH, day) }
                            val dateStr = sdf.format(dayCal.time)
                            val dayColors = eventColorMap[dateStr] ?: emptyList()
                            val isSelected = dateStr == selectedDateStr
                            val isToday = dateStr == sdf.format(Calendar.getInstance().time)
                            Box(modifier = Modifier.weight(1f).aspectRatio(1f).padding(2.dp)
                                .background(when { isSelected -> KmaRed; isToday -> KmaRedSurface; else -> Color.Transparent }, CircleShape)
                                .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) { selectedDate = dayCal },
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("$day", fontSize = 13.sp,
                                        fontWeight = if (isSelected || isToday) FontWeight.Bold else FontWeight.Normal,
                                        color = when { isSelected -> White; isToday -> KmaRed; else -> OnSurfaceHigh })
                                    if (dayColors.isNotEmpty()) {
                                        Row(
                                            horizontalArrangement = Arrangement.spacedBy(2.dp),
                                            modifier = Modifier.padding(top = 1.dp)
                                        ) {
                                            repeat(dayColors.size.coerceAtMost(4)) { i ->
                                                Box(modifier = Modifier.size(4.dp).background(
                                                    if (isSelected) White else dayColors[i], CircleShape))
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        Divider(modifier = Modifier.padding(vertical = 8.dp))
        LazyColumn(modifier = Modifier.fillMaxSize().padding(horizontal = 12.dp)) {
            if (eventsForSelected.isEmpty()) {
                item {
                    Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.EventBusy, contentDescription = null, tint = Outline, modifier = Modifier.size(32.dp))
                            Spacer(Modifier.height(8.dp))
                            Text("Không có lịch học", color = OnSurfaceMedium, fontSize = 13.sp)
                        }
                    }
                }
            } else {
                item {
                    Text(SimpleDateFormat("EEEE, dd/MM/yyyy", Locale("vi")).format(selectedDate.time).replaceFirstChar { it.uppercase() },
                        fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = KmaRed,
                        modifier = Modifier.padding(vertical = 8.dp))
                }
                items(eventsForSelected) { event ->
                    val color = getColorFromSeed(event.details.course_name)
                    val lessonGroup = event.details.lessons.split(" ").firstOrNull() ?: ""
                    val time = getLessonTime(lessonGroup)
                    Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp).clickable { selectedEvent = event },
                        elevation = 0.dp, shape = RoundedCornerShape(12.dp), backgroundColor = White) {
                        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.width(44.dp)) {
                                Text(time.start, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = KmaRed)
                                Box(modifier = Modifier.width(1.dp).height(16.dp).background(Outline))
                                Text(time.end, fontSize = 11.sp, color = OnSurfaceMedium)
                            }
                            Spacer(Modifier.width(12.dp))
                            Box(modifier = Modifier.width(3.dp).height(52.dp).background(color, RoundedCornerShape(2.dp)))
                            Spacer(Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(event.details.course_name, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                                Text("GV: ${event.details.teacher}", fontSize = 11.sp, color = OnSurfaceMedium)
                                Text("Phòng: ${event.details.study_location}", fontSize = 11.sp, color = OnSurfaceMedium)
                            }
                            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Outline)
                        }
                    }
                }
            }
            item { Spacer(Modifier.height(16.dp)) }
        }
    }
    selectedEvent?.let { event ->
        AlertDialog(onDismissRequest = { selectedEvent = null }, shape = RoundedCornerShape(16.dp),
            title = { Text(event.details.course_name, fontWeight = FontWeight.Bold, fontSize = 15.sp) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    DetailRow("Tiết học", event.details.lessons)
                    DetailRow("Giảng viên", event.details.teacher)
                    DetailRow("Phòng học", event.details.study_location)
                    DetailRow("Thời gian", event.base_time)
                }
            },
            confirmButton = { TextButton(onClick = { selectedEvent = null }) { Text("Đóng") } }
        )
    }
}

@Composable
private fun DetailRow(label: String, value: String) {
    Row {
        Text("$label: ", fontSize = 13.sp, fontWeight = FontWeight.Medium, color = OnSurfaceMedium)
        Text(value, fontSize = 13.sp, color = OnSurfaceHigh)
    }
}

@Composable
private fun ImportScheduleDialog(
    onImport: (List<VirtualCalendarItem>) -> Unit,
    onDismiss: () -> Unit,
    parseGroups: (String) -> Map<String, List<VirtualCalendarItem>>
) {
    var step by remember { mutableStateOf(1) }
    var inputText by remember { mutableStateOf("") }
    var parsedGroups by remember { mutableStateOf<Map<String, List<VirtualCalendarItem>>>(emptyMap()) }
    AlertDialog(onDismissRequest = onDismiss, shape = RoundedCornerShape(16.dp),
        title = { Text(if (step == 1) "Import lịch" else "Xác nhận import", fontWeight = FontWeight.Bold) },
        text = {
            if (step == 1) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Dán danh sách môn học (mỗi dòng 1 môn):", fontSize = 13.sp, color = OnSurfaceMedium)
                    OutlinedTextField(value = inputText, onValueChange = { inputText = it },
                        modifier = Modifier.fillMaxWidth().height(160.dp),
                        placeholder = { Text("Tên môn học...", fontSize = 12.sp) },
                        shape = RoundedCornerShape(10.dp))
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("Tìm thấy ${parsedGroups.size} môn:", fontSize = 13.sp, fontWeight = FontWeight.Medium)
                    parsedGroups.forEach { (key, items) ->
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Success, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(6.dp))
                            Text(items.firstOrNull()?.details?.course_name ?: key, fontSize = 12.sp, color = OnSurfaceHigh)
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = {
                if (step == 1) { parsedGroups = parseGroups(inputText); if (parsedGroups.isNotEmpty()) step = 2 }
                else onImport(parsedGroups.values.flatten())
            }, shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(backgroundColor = KmaRed),
                elevation = ButtonDefaults.elevation(0.dp)
            ) { Text(if (step == 1) "Tiếp theo" else "Import", color = White) }
        },
        dismissButton = {
            TextButton(onClick = { if (step == 2) step = 1 else onDismiss() }) {
                Text(if (step == 2) "Quay lại" else "Hủy", color = OnSurfaceMedium)
            }
        }
    )
}
