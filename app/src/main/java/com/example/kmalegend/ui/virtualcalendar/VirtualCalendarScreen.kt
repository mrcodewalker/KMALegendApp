package com.example.kmalegend.ui.virtualcalendar

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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

    Scaffold(
        topBar = {
            KmaTopBar(
                title = "Lịch ảo",
                navController = navController,
                showBack = true,
                actions = {
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
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
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
}

// ── Tab 1: Chọn lớp ──────────────────────────────────────────────────────────

@Composable
private fun PickerTab(
    allItems: List<VirtualCalendarItem>,
    savedClasses: List<VirtualCalendarItem>,
    onSavedChange: (List<VirtualCalendarItem>) -> Unit
) {
    val courseBlocks = remember(allItems) { allItems.map { it.course }.distinct().sorted() }
    var selectedBlock by remember { mutableStateOf(courseBlocks.firstOrNull() ?: "") }
    val subjectsForBlock = remember(selectedBlock, allItems) {
        allItems.filter { it.course == selectedBlock }.map { it.course_name }.distinct().sorted()
    }
    var selectedSubject by remember { mutableStateOf("") }
    val classesForSubject = remember(selectedSubject, allItems) {
        allItems.filter { it.course == selectedBlock && it.course_name == selectedSubject }
    }
    var blockExpanded by remember { mutableStateOf(false) }
    var subjectExpanded by remember { mutableStateOf(false) }
    var conflictMsg by remember { mutableStateOf<String?>(null) }

    fun checkConflict(item: VirtualCalendarItem): String? {
        val newDays = item.details.study_days.split(" ").map { it.trim() }.toSet()
        val newTime = getLessonTime(item.details.lessons.split(" ").firstOrNull() ?: "")
        for (saved in savedClasses) {
            val savedDays = saved.details.study_days.split(" ").map { it.trim() }.toSet()
            val savedTime = getLessonTime(saved.details.lessons.split(" ").firstOrNull() ?: "")
            if (newDays.intersect(savedDays).isNotEmpty() && newTime.start == savedTime.start)
                return "Trùng lịch với: ${saved.details.course_name}"
        }
        return null
    }

    LazyColumn(modifier = Modifier.fillMaxSize()) {
        // Filter card
        item {
            Card(modifier = Modifier.fillMaxWidth().padding(12.dp), elevation = 4.dp, shape = RoundedCornerShape(12.dp)) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Chọn lớp học", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    // Block dropdown
                    Box {
                        OutlinedButton(onClick = { blockExpanded = true }, modifier = Modifier.fillMaxWidth()) {
                            Text(if (selectedBlock.isEmpty()) "Chọn khối học phần" else selectedBlock)
                            Spacer(Modifier.weight(1f))
                            Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                        }
                        DropdownMenu(expanded = blockExpanded, onDismissRequest = { blockExpanded = false }) {
                            courseBlocks.forEach { block ->
                                DropdownMenuItem(onClick = { selectedBlock = block; selectedSubject = ""; blockExpanded = false }) {
                                    Text(block)
                                }
                            }
                        }
                    }
                    // Subject dropdown
                    if (selectedBlock.isNotEmpty()) {
                        Box {
                            OutlinedButton(onClick = { subjectExpanded = true }, modifier = Modifier.fillMaxWidth()) {
                                Text(if (selectedSubject.isEmpty()) "Chọn môn học" else selectedSubject, maxLines = 1)
                                Spacer(Modifier.weight(1f))
                                Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                            }
                            DropdownMenu(expanded = subjectExpanded, onDismissRequest = { subjectExpanded = false }) {
                                subjectsForBlock.forEach { subject ->
                                    DropdownMenuItem(onClick = { selectedSubject = subject; subjectExpanded = false }) {
                                        Text(subject)
                                    }
                                }
                            }
                        }
                    }
                    // Classes list
                    if (selectedSubject.isNotEmpty()) {
                        Text("Chọn lớp:", fontSize = 13.sp, color = OnSurfaceMedium)
                        classesForSubject.forEach { item ->
                            val added = savedClasses.any { it.details.course_code == item.details.course_code }
                            Row(
                                modifier = Modifier.fillMaxWidth()
                                    .background(if (added) Success.copy(alpha = 0.08f) else Color.Transparent, RoundedCornerShape(8.dp))
                                    .clickable {
                                        if (!added) {
                                            val conflict = checkConflict(item)
                                            if (conflict != null) conflictMsg = conflict
                                            else onSavedChange(savedClasses + item)
                                        } else {
                                            onSavedChange(savedClasses.filter { it.details.course_code != item.details.course_code })
                                        }
                                    }
                                    .padding(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    if (added) Icons.Default.CheckCircle else Icons.Default.AddCircleOutline,
                                    contentDescription = null,
                                    tint = if (added) Success else KmaRed,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(Modifier.width(8.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(item.details.course_name, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                                    Text(item.base_time, fontSize = 11.sp, color = OnSurfaceMedium)
                                    Text("GV: ${item.details.teacher}", fontSize = 11.sp, color = OnSurfaceMedium)
                                }
                            }
                        }
                    }
                }
            }
        }
        // Saved classes
        item {
            Text("Lớp đã chọn (${savedClasses.size})", fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp))
        }
        if (savedClasses.isEmpty()) {
            item {
                Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                    Text("Chưa chọn lớp nào", color = OnSurfaceMedium)
                }
            }
        } else {
            items(savedClasses) { item ->
                val color = getColorFromSeed(item.course_name)
                Card(modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 4.dp),
                    elevation = 2.dp, shape = RoundedCornerShape(8.dp)) {
                    Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.width(4.dp).height(50.dp).background(color, RoundedCornerShape(2.dp)))
                        Spacer(Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(item.details.course_name, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Text(item.base_time, fontSize = 11.sp, color = OnSurfaceMedium)
                            Text("GV: ${item.details.teacher} | ${item.details.study_location}", fontSize = 11.sp, color = OnSurfaceMedium)
                        }
                        IconButton(onClick = { onSavedChange(savedClasses.filter { it.details.course_code != item.details.course_code }) }) {
                            Icon(Icons.Default.Close, contentDescription = null, tint = Error)
                        }
                    }
                }
            }
        }
        item { Spacer(Modifier.height(16.dp)) }
    }

    conflictMsg?.let { msg ->
        AlertDialog(
            onDismissRequest = { conflictMsg = null },
            title = { Text("Cảnh báo trùng lịch", color = Warning, fontWeight = FontWeight.Bold) },
            text = { Text(msg) },
            confirmButton = { TextButton(onClick = { conflictMsg = null }) { Text("Đóng") } }
        )
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
                                    .clickable { selectedDate = dayCal },
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
