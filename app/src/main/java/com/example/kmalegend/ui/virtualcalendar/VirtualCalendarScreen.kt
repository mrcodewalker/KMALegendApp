package com.example.kmalegend.ui.virtualcalendar

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
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

    // Copy: mỗi dòng = "tên môn đầy đủ ||| base_time ||| teacher"
    // classKey = base_time|teacher — đủ để tìm lại đúng lớp khi import
    fun buildShareText(): String = savedClasses.joinToString("\n") { item ->
        "${item.details.course_name} ||| ${item.base_time} ||| ${item.details.teacher}"
    }

    // Import: parse từng dòng, match classKey (base_time|teacher), fallback tên môn
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
            KmaTopBar(
                title = "Lịch ảo",
                navController = navController,
                showBack = true,
                actions = {
                    // Import
                    IconButton(onClick = { showImportDialog = true }) {
                        Icon(Icons.Default.FileDownload, contentDescription = null, tint = White)
                    }
                    // Share/Copy
                    IconButton(onClick = {
                        val clip = android.content.ClipData.newPlainText("lịch ảo", buildShareText())
                        (context2.getSystemService(android.content.Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager).setPrimaryClip(clip)
                        shareToast = true
                    }) {
                        Icon(Icons.Default.ContentCopy, contentDescription = null, tint = White)
                    }
                    // Xóa tất cả
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
            savedClasses = merged
            prefs.saveSavedClasses(merged)
            showImportDialog = false
        },
        onDismiss = { showImportDialog = false },
        parseGroups = { parseImportGroups(it) }
    )
}

// ── Tab 1: Chọn lớp ──────────────────────────────────────────────────────────

// Lấy tên môn gốc — bỏ phần "(mã lớp)" ở cuối để group các lớp cùng môn
private fun baseSubjectName(courseName: String): String =
    courseName.substringBefore("(").trim()

// Unique key cho 1 lớp — base_time + teacher
private fun classKey(item: VirtualCalendarItem) = "${item.base_time}|${item.details.teacher}"

// Enum để biết sheet nào đang mở
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

    // Subjects grouped by base name (strip class code in parentheses)
    val subjectsForBlock by remember(selectedBlock, allItems) {
        derivedStateOf {
            allItems.filter { it.course == selectedBlock }
                .map { baseSubjectName(it.course_name) }
                .distinct().sorted()
        }
    }
    var selectedSubject by remember { mutableStateOf("") } // tên môn gốc

    // Tất cả lớp của môn đang chọn (match theo tên gốc)
    val classesForSubject by remember(selectedSubject, selectedBlock, allItems) {
        derivedStateOf {
            allItems.filter {
                it.course == selectedBlock && baseSubjectName(it.course_name) == selectedSubject
            }
        }
    }

    // Lớp đang được chọn cho môn hiện tại
    // Key unique = course_name (top-level) vì nó chứa mã lớp cụ thể
    val selectedClassKey by derivedStateOf {
        savedClasses.firstOrNull {
            baseSubjectName(it.course_name) == selectedSubject
        }?.let { classKey(it) }
    }

    // Blocks/subjects đã có lớp được chọn
    val blocksWithSelection by derivedStateOf { savedClasses.map { it.course }.toSet() }
    val subjectsWithSelection by derivedStateOf {
        savedClasses.filter { it.course == selectedBlock }
            .map { baseSubjectName(it.course_name) }.toSet()
    }

    var activeSheet by remember { mutableStateOf(SheetType.NONE) }
    val sheetState = rememberModalBottomSheetState(
        initialValue = ModalBottomSheetValue.Hidden,
        skipHalfExpanded = true
    )
    val scope = rememberCoroutineScope()
    var conflictMsg by remember { mutableStateOf<String?>(null) }

    // Mở/đóng sheet theo activeSheet
    LaunchedEffect(activeSheet) {
        if (activeSheet != SheetType.NONE) sheetState.show()
    }
    LaunchedEffect(sheetState.isVisible) {
        if (!sheetState.isVisible) activeSheet = SheetType.NONE
    }

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
            // Handle bar
            Column(modifier = Modifier.fillMaxWidth()) {
                Box(
                    modifier = Modifier.fillMaxWidth().padding(top = 12.dp, bottom = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Box(modifier = Modifier.width(40.dp).height(4.dp).background(Outline, RoundedCornerShape(2.dp)))
                }

                when (activeSheet) {
                    SheetType.BLOCK -> {
                        Text(
                            "Chọn khối học phần",
                            fontWeight = FontWeight.Bold, fontSize = 17.sp,
                            modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
                        )
                        Column(modifier = Modifier.verticalScroll(rememberScrollState()).padding(bottom = 24.dp)) {
                            courseBlocks.forEach { block ->
                                val isCurrent = block == selectedBlock
                                val hasSelection = block in blocksWithSelection
                                Row(
                                    modifier = Modifier.fillMaxWidth()
                                        .clickable(
                                            interactionSource = remember { MutableInteractionSource() },
                                            indication = null
                                        ) {
                                            selectedBlock = block
                                            selectedSubject = ""
                                            scope.launch { sheetState.hide() }
                                        }
                                        .background(
                                            when {
                                                isCurrent -> Success.copy(alpha = 0.08f)
                                                hasSelection -> Success.copy(alpha = 0.04f)
                                                else -> Color.Transparent
                                            }
                                        )
                                        .padding(horizontal = 20.dp, vertical = 14.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        block,
                                        modifier = Modifier.weight(1f),
                                        fontWeight = if (isCurrent || hasSelection) FontWeight.SemiBold else FontWeight.Normal,
                                        color = if (isCurrent || hasSelection) Success else OnSurfaceHigh,
                                        fontSize = 15.sp
                                    )
                                    when {
                                        isCurrent -> Icon(Icons.Default.Check, contentDescription = null,
                                            tint = Success, modifier = Modifier.size(20.dp))
                                        hasSelection -> Box(
                                            modifier = Modifier.size(8.dp)
                                                .background(Success, CircleShape)
                                        )
                                    }
                                }
                                Divider(color = Outline.copy(alpha = 0.4f), modifier = Modifier.padding(horizontal = 20.dp))
                            }
                        }
                    }

                    SheetType.SUBJECT -> {
                        Text(
                            "Chọn môn học",
                            fontWeight = FontWeight.Bold, fontSize = 17.sp,
                            modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
                        )
                        Column(modifier = Modifier.verticalScroll(rememberScrollState()).padding(bottom = 24.dp)) {
                            subjectsForBlock.forEach { subject ->
                                val isCurrent = subject == selectedSubject
                                val hasSelection = subject in subjectsWithSelection
                                Row(
                                    modifier = Modifier.fillMaxWidth()
                                        .clickable(
                                            interactionSource = remember { MutableInteractionSource() },
                                            indication = null
                                        ) {
                                            selectedSubject = subject
                                            scope.launch { sheetState.hide() }
                                        }
                                        .background(
                                            when {
                                                isCurrent -> Success.copy(alpha = 0.08f)
                                                hasSelection -> Success.copy(alpha = 0.04f)
                                                else -> Color.Transparent
                                            }
                                        )
                                        .padding(horizontal = 20.dp, vertical = 14.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            subject,
                                            fontWeight = if (isCurrent || hasSelection) FontWeight.SemiBold else FontWeight.Normal,
                                            color = if (isCurrent || hasSelection) Success else OnSurfaceHigh,
                                            fontSize = 14.sp
                                        )
                                        if (hasSelection) {
                                            val chosenClass = savedClasses.firstOrNull {
                                                baseSubjectName(it.course_name) == subject
                                            }
                                            if (chosenClass != null) {
                                                Text(
                                                    "✓ ${chosenClass.details.course_name}",
                                                    fontSize = 11.sp, color = Success.copy(alpha = 0.85f)
                                                )
                                            }
                                        }
                                    }
                                    when {
                                        isCurrent -> Icon(Icons.Default.Check, contentDescription = null,
                                            tint = Success, modifier = Modifier.size(20.dp))
                                        hasSelection -> Icon(Icons.Default.CheckCircle, contentDescription = null,
                                            tint = Success, modifier = Modifier.size(18.dp))
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
            // Filter card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth().padding(12.dp),
                    elevation = 0.dp,
                    shape = RoundedCornerShape(16.dp),
                    backgroundColor = White
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text("Xếp lịch học", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = OnSurfaceHigh)

                        // Step 1 — Khối học phần
                        PickerButton(
                            label = "Khối học phần",
                            value = selectedBlock.ifEmpty { null },
                            placeholder = "Chọn khối học phần",
                            hasSelection = selectedBlock in blocksWithSelection,
                            onClick = {
                                activeSheet = SheetType.BLOCK
                                scope.launch { sheetState.show() }
                            }
                        )

                        // Step 2 — Môn học
                        if (selectedBlock.isNotEmpty()) {
                            PickerButton(
                                label = "Môn học",
                                value = selectedSubject.ifEmpty { null },
                                placeholder = "Chọn môn học",
                                hasSelection = selectedSubject in subjectsWithSelection,
                                onClick = {
                                    activeSheet = SheetType.SUBJECT
                                    scope.launch { sheetState.show() }
                                }
                            )
                        }

                        // Step 3 — Danh sách lớp
                        if (selectedSubject.isNotEmpty() && classesForSubject.isNotEmpty()) {
                            Divider(color = Outline)
                            Text(
                                "Chọn lớp  •  ${classesForSubject.size} lớp",
                                fontSize = 12.sp, color = OnSurfaceMedium, fontWeight = FontWeight.Medium
                            )
                            classesForSubject.forEach { item ->
                                // So sánh bằng composite key (base_time + teacher) để đảm bảo unique
                                val isSelected = classKey(item) == selectedClassKey
                                val noRipple = remember { MutableInteractionSource() }
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp)
                                        .background(
                                            if (isSelected) Success.copy(alpha = 0.08f) else SurfaceVariant,
                                            RoundedCornerShape(12.dp)
                                        )
                                        .border(
                                            width = if (isSelected) 1.5.dp else 1.dp,
                                            color = if (isSelected) Success else Outline,
                                            shape = RoundedCornerShape(12.dp)
                                        )
                                        .clickable(interactionSource = noRipple, indication = null) {
                                            if (isSelected) {
                                                onSavedChange(savedClasses.filter { classKey(it) != classKey(item) })
                                            } else {
                                                val withoutSameSubject = savedClasses.filter {
                                                    baseSubjectName(it.course_name) != selectedSubject
                                                }
                                                val conflict = checkConflict(item, withoutSameSubject)
                                                if (conflict != null) conflictMsg = conflict
                                                else onSavedChange(withoutSameSubject + item)
                                            }
                                        }
                                        .padding(horizontal = 12.dp, vertical = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        if (isSelected) Icons.Default.RadioButtonChecked else Icons.Default.RadioButtonUnchecked,
                                        contentDescription = null,
                                        tint = if (isSelected) Success else OnSurfaceMedium,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(Modifier.width(10.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            item.details.course_name,
                                            fontSize = 13.sp,
                                            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                                            color = if (isSelected) Success else OnSurfaceHigh
                                        )
                                        Text(item.base_time, fontSize = 11.sp, color = OnSurfaceMedium)
                                        Text("GV: ${item.details.teacher}", fontSize = 11.sp, color = OnSurfaceMedium)
                                    }
                                    if (isSelected) {
                                        Icon(
                                            Icons.Default.CheckCircle, contentDescription = null,
                                            tint = Success, modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Saved classes header
            item {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Lớp đã chọn", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = OnSurfaceHigh)
                    Spacer(Modifier.width(8.dp))
                    if (savedClasses.isNotEmpty()) {
                        Card(shape = RoundedCornerShape(6.dp), backgroundColor = Success, elevation = 0.dp) {
                            Text(
                                "${savedClasses.size}", color = White, fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                            )
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
                    val color = getColorFromSeed(item.course_name)
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 4.dp),
                        elevation = 0.dp,
                        shape = RoundedCornerShape(12.dp),
                        backgroundColor = White
                    ) {
                        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.width(4.dp).height(50.dp).background(color, RoundedCornerShape(2.dp)))
                            Spacer(Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(item.details.course_name, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                                Text(item.base_time, fontSize = 11.sp, color = OnSurfaceMedium)
                                Text("GV: ${item.details.teacher} | ${item.details.study_location}", fontSize = 11.sp, color = OnSurfaceMedium)
                            }
                            IconButton(onClick = {
                                onSavedChange(savedClasses.filter { classKey(it) != classKey(item) })
                            }) {
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
        AlertDialog(
            onDismissRequest = { conflictMsg = null },
            shape = RoundedCornerShape(16.dp),
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Warning, contentDescription = null, tint = Warning, modifier = Modifier.size(22.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Trùng lịch", color = Warning, fontWeight = FontWeight.Bold)
                }
            },
            text = { Text(msg, color = OnSurfaceHigh) },
            confirmButton = {
                Button(
                    onClick = { conflictMsg = null },
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(backgroundColor = Warning),
                    elevation = ButtonDefaults.elevation(0.dp)
                ) { Text("Đóng", color = White) }
            }
        )
    }
}

@Composable
private fun PickerButton(
    label: String,
    value: String?,
    placeholder: String,
    hasSelection: Boolean = false,
    onClick: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(label, fontSize = 12.sp, color = OnSurfaceMedium, fontWeight = FontWeight.Medium)
            if (hasSelection) {
                Spacer(Modifier.width(6.dp))
                Icon(Icons.Default.CheckCircle, contentDescription = null,
                    tint = Success, modifier = Modifier.size(13.dp))
            }
        }
        OutlinedButton(
            onClick = onClick,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(10.dp),
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = if (hasSelection) Success else if (value != null) OnSurfaceHigh else OnSurfaceMedium
            ),
            border = androidx.compose.foundation.BorderStroke(
                width = if (hasSelection) 1.5.dp else 1.dp,
                color = if (hasSelection) Success else Outline
            )
        ) {
            Text(
                value ?: placeholder,
                modifier = Modifier.weight(1f),
                fontWeight = if (value != null) FontWeight.Medium else FontWeight.Normal,
                fontSize = 14.sp,
                maxLines = 1,
                color = if (hasSelection) Success else if (value != null) OnSurfaceHigh else OnSurfaceMedium
            )
            Icon(Icons.Default.KeyboardArrowDown, contentDescription = null,
                tint = if (hasSelection) Success else OnSurfaceMedium)
        }
    }
}

// ── Tab 2: Xem lịch calendar ─────────────────────────────────────────────────

@Composable
private fun ScheduleTab(savedClasses: List<VirtualCalendarItem>) {
    var currentMonth by remember { mutableStateOf(Calendar.getInstance()) }
    var selectedDate by remember { mutableStateOf(Calendar.getInstance()) }
    var selectedEvent by remember { mutableStateOf<VirtualCalendarItem?>(null) }

    // Build map: dateStr → list of items
    val eventMap = remember(savedClasses) {
        val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val map = mutableMapOf<String, MutableList<VirtualCalendarItem>>()
        savedClasses.forEach { item ->
            item.details.study_days.split(" ").forEach { dayStr ->
                val key = dayStr.trim()
                if (key.isNotEmpty()) map.getOrPut(key) { mutableListOf() }.add(item)
            }
        }
        map
    }

    val selectedDateStr = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(selectedDate.time)
    val eventsForSelected = eventMap[selectedDateStr] ?: emptyList()

    Column(modifier = Modifier.fillMaxSize()) {
        // Month navigation
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            IconButton(onClick = {
                currentMonth = (currentMonth.clone() as Calendar).apply { add(Calendar.MONTH, -1) }
            }) { Icon(Icons.Default.ChevronLeft, contentDescription = null) }
            Text(
                SimpleDateFormat("MMMM yyyy", Locale("vi")).format(currentMonth.time).replaceFirstChar { it.uppercase() },
                fontWeight = FontWeight.Bold, fontSize = 16.sp
            )
            IconButton(onClick = {
                currentMonth = (currentMonth.clone() as Calendar).apply { add(Calendar.MONTH, 1) }
            }) { Icon(Icons.Default.ChevronRight, contentDescription = null) }
        }

        // Day of week headers
        Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp)) {
            listOf("CN", "T2", "T3", "T4", "T5", "T6", "T7").forEach { day ->
                Text(day, modifier = Modifier.weight(1f), textAlign = TextAlign.Center,
                    fontSize = 12.sp, fontWeight = FontWeight.Bold,
                    color = if (day == "CN") KmaRed else OnSurfaceMedium)
            }
        }

        // Calendar grid
        val today = Calendar.getInstance()
        val firstDay = (currentMonth.clone() as Calendar).apply { set(Calendar.DAY_OF_MONTH, 1) }
        val startDow = firstDay.get(Calendar.DAY_OF_WEEK) - 1
        val daysInMonth = currentMonth.getActualMaximum(Calendar.DAY_OF_MONTH)
        val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

        Column(modifier = Modifier.padding(horizontal = 8.dp)) {
            val rows = ((startDow + daysInMonth) + 6) / 7
            for (row in 0 until rows) {
                Row(modifier = Modifier.fillMaxWidth()) {
                    for (col in 0..6) {
                        val dayNum = row * 7 + col - startDow + 1
                        if (dayNum < 1 || dayNum > daysInMonth) {
                            Box(modifier = Modifier.weight(1f).height(40.dp))
                        } else {
                            val dayCal = (currentMonth.clone() as Calendar).apply { set(Calendar.DAY_OF_MONTH, dayNum) }
                            val dayStr = sdf.format(dayCal.time)
                            val hasEvent = eventMap.containsKey(dayStr)
                            val isToday = dayNum == today.get(Calendar.DAY_OF_MONTH) &&
                                    currentMonth.get(Calendar.MONTH) == today.get(Calendar.MONTH) &&
                                    currentMonth.get(Calendar.YEAR) == today.get(Calendar.YEAR)
                            val isSelected = dayStr == selectedDateStr

                            Box(
                                modifier = Modifier.weight(1f).height(40.dp).padding(2.dp)
                                    .background(
                                        when { isSelected -> MaterialTheme.colors.primary; isToday -> KmaRed.copy(alpha = 0.15f); else -> Color.Transparent },
                                        CircleShape
                                    )
                                    .clickable(
                                        interactionSource = remember { MutableInteractionSource() },
                                        indication = null
                                    ) { selectedDate = dayCal },
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("$dayNum", fontSize = 13.sp,
                                        color = if (isSelected) White else MaterialTheme.colors.onSurface,
                                        fontWeight = if (isSelected || isToday) FontWeight.Bold else FontWeight.Normal)
                                    if (hasEvent) {
                                        Box(modifier = Modifier.size(4.dp).background(
                                            if (isSelected) White else KmaRed, CircleShape))
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        Divider(modifier = Modifier.padding(vertical = 8.dp))

        // Events for selected date
        Text(
            "Lịch ngày $selectedDateStr",
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
        )

        if (eventsForSelected.isEmpty()) {
            Box(modifier = Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) {
                Text("Không có lịch học", color = OnSurfaceMedium)
            }
        } else {
            LazyColumn(modifier = Modifier.weight(1f)) {
                items(eventsForSelected) { item ->
                    val color = getColorFromSeed(item.course_name)
                    // Find which lesson group corresponds to this date
                    val dayIndex = item.details.study_days.split(" ").indexOfFirst {
                        it.trim() == selectedDateStr
                    }
                    val lessonGroup = item.details.lessons.split(" ").getOrNull(dayIndex) ?: item.details.lessons.split(" ").firstOrNull() ?: ""
                    val time = getLessonTime(lessonGroup)

                    Card(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 4.dp)
                            .clickable { selectedEvent = item },
                        elevation = 2.dp, shape = RoundedCornerShape(8.dp)
                    ) {
                        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.width(4.dp).height(50.dp).background(color, RoundedCornerShape(2.dp)))
                            Spacer(Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(item.details.course_name, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                Text("Phòng: ${item.details.study_location}", fontSize = 11.sp, color = OnSurfaceMedium)
                                Text("GV: ${item.details.teacher}", fontSize = 11.sp, color = OnSurfaceMedium)
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text(time.start, fontSize = 12.sp, color = KmaRed, fontWeight = FontWeight.Bold)
                                Text(time.end, fontSize = 11.sp, color = OnSurfaceMedium)
                            }
                        }
                    }
                }
                item { Spacer(Modifier.height(16.dp)) }
            }
        }
    }

    // Event detail dialog
    selectedEvent?.let { item ->
        AlertDialog(
            onDismissRequest = { selectedEvent = null },
            title = { Text(item.details.course_name, fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    DetailRow("Mã môn", item.details.course_code)
                    DetailRow("Giảng viên", item.details.teacher)
                    DetailRow("Phòng học", item.details.study_location)
                    DetailRow("Lịch học", item.base_time)
                }
            },
            confirmButton = { TextButton(onClick = { selectedEvent = null }) { Text("Đóng") } }
        )
    }
}

@Composable
private fun DetailRow(label: String, value: String) {
    Row {
        Text("$label: ", fontWeight = FontWeight.Medium, fontSize = 13.sp, color = OnSurfaceMedium)
        Text(value, fontSize = 13.sp)
    }
}

@Composable
private fun ImportScheduleDialog(
    onImport: (List<VirtualCalendarItem>) -> Unit,
    onDismiss: () -> Unit,
    parseGroups: (String) -> Map<String, List<VirtualCalendarItem>>
) {
    var rawText by remember { mutableStateOf("") }
    var found by remember { mutableStateOf<List<VirtualCalendarItem>>(emptyList()) }
    var notFound by remember { mutableStateOf<List<String>>(emptyList()) }
    var step by remember { mutableStateOf(1) }

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(20.dp),
        title = {
            Text(
                if (step == 1) "Nhập lịch từ clipboard" else "Xem trước (${found.size} lớp)",
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            if (step == 1) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Card(backgroundColor = InfoSurface, elevation = 0.dp, shape = RoundedCornerShape(10.dp)) {
                        Text(
                            "Paste danh sách lớp đã copy, mỗi dòng 1 lớp.\nVí dụ:\nGiáo dục thể chất 4-2-25 (A21C9D8.10. bóng đá)",
                            fontSize = 12.sp, color = Info, modifier = Modifier.padding(10.dp)
                        )
                    }
                    OutlinedTextField(
                        value = rawText,
                        onValueChange = { rawText = it },
                        modifier = Modifier.fillMaxWidth().height(160.dp),
                        label = { Text("Dán tên lớp vào đây") },
                        shape = RoundedCornerShape(10.dp)
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.heightIn(max = 360.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    if (found.isNotEmpty()) {
                        item {
                            Text("Tìm thấy:", fontSize = 12.sp, color = OnSurfaceMedium, fontWeight = FontWeight.Medium)
                        }
                        items(found) { item ->
                            Row(
                                modifier = Modifier.fillMaxWidth()
                                    .background(SuccessSurface, RoundedCornerShape(8.dp))
                                    .padding(horizontal = 10.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.CheckCircle, contentDescription = null,
                                    tint = Success, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(8.dp))
                                Column {
                                    Text(item.course_name, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = OnSurfaceHigh)
                                    Text("GV: ${item.details.teacher}  •  ${item.base_time}", fontSize = 11.sp, color = OnSurfaceMedium)
                                }
                            }
                        }
                    }
                    if (notFound.isNotEmpty()) {
                        item { Spacer(Modifier.height(4.dp)) }
                        item {
                            Text("Không tìm thấy:", fontSize = 12.sp, color = Error, fontWeight = FontWeight.Medium)
                        }
                        items(notFound) { name ->
                            Text("• $name", fontSize = 12.sp, color = Error)
                        }
                    }
                    if (found.isEmpty() && notFound.isEmpty()) {
                        item { Text("Không có dữ liệu hợp lệ.", color = OnSurfaceMedium) }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (step == 1) {
                        val groups = parseGroups(rawText)
                        found = groups.values.flatten()
                        notFound = rawText.lines()
                            .map { it.trim() }.filter { it.isNotEmpty() }
                            .filter { line -> groups.none { (k, _) -> k == line } }
                        step = 2
                    } else {
                        if (found.isNotEmpty()) onImport(found)
                    }
                },
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(backgroundColor = KmaRed),
                elevation = ButtonDefaults.elevation(0.dp),
                enabled = if (step == 1) rawText.isNotBlank() else found.isNotEmpty()
            ) {
                Text(
                    if (step == 1) "Tiếp theo" else "Import (${found.size})",
                    color = White, fontWeight = FontWeight.SemiBold
                )
            }
        },
        dismissButton = {
            TextButton(onClick = { if (step == 2) step = 1 else onDismiss() }) {
                Text(if (step == 2) "Quay lại" else "Hủy", color = OnSurfaceMedium)
            }
        }
    )
}
