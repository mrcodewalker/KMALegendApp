package com.example.kmalegend.ui.schedule

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.kmalegend.data.*
import com.example.kmalegend.notification.NotificationScheduler
import com.example.kmalegend.notification.ScheduleNotificationWorker
import com.example.kmalegend.ui.common.*
import com.example.kmalegend.ui.navigation.Routes
import com.example.kmalegend.ui.theme.*
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun ScheduleScreen(navController: NavController) {
    val context = LocalContext.current
    val prefs = remember { PrefsManager(context) }
    val loginData = remember { prefs.getScheduleSecret() }
    val schedule = remember { loginData?.data?.student_schedule ?: emptyList() }
    val studentInfo = remember { loginData?.data?.student_info }

    var currentMonth by remember { mutableStateOf(Calendar.getInstance()) }
    var selectedDate by remember { mutableStateOf(Calendar.getInstance()) }
    var selectedEvent by remember { mutableStateOf<CourseSchedule?>(null) }
    var showLogoutDialog by remember { mutableStateOf(false) }
    var showNotifDialog by remember { mutableStateOf(false) }

    // ── Entrance animation ────────────────────────────────────────────────
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { delay(60); visible = true }

    // Notification permission state
    var notifEnabled by remember {
        mutableStateOf(
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                androidx.core.app.NotificationManagerCompat.from(context).areNotificationsEnabled()
            } else true
        )
    }
    val permLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        notifEnabled = granted
        if (granted) NotificationScheduler.schedule(context)
    }

    val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    val selectedDateStr = sdf.format(selectedDate.time)
    val eventsForDate = remember(selectedDate, schedule) {
        schedule.filter { course -> course.study_days.split(" ").any { it.trim() == selectedDateStr } }
    }
    val eventDates = remember(schedule) {
        schedule.flatMap { c -> c.study_days.split(" ").map { it.trim() } }.toSet()
    }
    // Map: dateStr → list of colors (one per course on that day)
    val eventColorMap = remember(schedule) {
        val map = mutableMapOf<String, MutableList<Color>>()
        schedule.forEach { course ->
            val color = getColorFromSeed(course.course_name)
            course.study_days.split(" ").forEach { day ->
                val key = day.trim()
                if (key.isNotEmpty()) map.getOrPut(key) { mutableListOf() }.add(color)
            }
        }
        map
    }

    Scaffold(
        topBar = {
            KmaTopBar(
                title = "Lịch học",
                navController = navController,
                showBack = true,
                actions = {
                    IconButton(onClick = { showNotifDialog = true }) {
                        Icon(
                            if (notifEnabled) Icons.Default.Notifications else Icons.Default.NotificationsOff,
                            contentDescription = null, tint = White
                        )
                    }
                    IconButton(onClick = { ScheduleNotificationWorker.sendTestNotification(context) }) {
                        Icon(Icons.Default.NotificationsActive, contentDescription = "Test notification", tint = White)
                    }
                    IconButton(onClick = { showLogoutDialog = true }) {
                        Icon(Icons.Default.Logout, contentDescription = null, tint = White)
                    }
                }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
        // Student info banner with slide-down animation
            studentInfo?.let { info ->
                AnimatedVisibility(
                    visible = visible,
                    enter = slideInVertically(tween(450, easing = FastOutSlowInEasing)) { -it } + fadeIn(tween(400))
                ) {
                Box(
                    modifier = Modifier.fillMaxWidth()
                        .background(Brush.linearGradient(listOf(KmaRed, KmaRedDark)))
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier.size(40.dp).background(White.copy(alpha = 0.2f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Person, contentDescription = null, tint = White, modifier = Modifier.size(22.dp))
                        }
                        Spacer(Modifier.width(12.dp))
                        Column {
                            Text(info.display_name, color = White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Text(info.student_code, color = White.copy(alpha = 0.75f), fontSize = 12.sp)
                        }
                        Spacer(Modifier.weight(1f))
                        Card(shape = RoundedCornerShape(8.dp), backgroundColor = White.copy(alpha = 0.2f), elevation = 0.dp) {
                            Text("${schedule.size} môn", color = White, fontSize = 12.sp, fontWeight = FontWeight.Medium,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp))
                        }
                    }
                }
                }
            }

            // Month navigation
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(onClick = { currentMonth = (currentMonth.clone() as Calendar).apply { add(Calendar.MONTH, -1) } }) {
                    Icon(Icons.Default.ChevronLeft, contentDescription = null, tint = OnSurfaceHigh)
                }
                Text(
                    SimpleDateFormat("MMMM yyyy", Locale("vi")).format(currentMonth.time).replaceFirstChar { it.uppercase() },
                    fontWeight = FontWeight.Bold, fontSize = 16.sp, color = OnSurfaceHigh
                )
                IconButton(onClick = { currentMonth = (currentMonth.clone() as Calendar).apply { add(Calendar.MONTH, 1) } }) {
                    Icon(Icons.Default.ChevronRight, contentDescription = null, tint = OnSurfaceHigh)
                }
            }

            // Day headers
            Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp)) {
                listOf("CN", "T2", "T3", "T4", "T5", "T6", "T7").forEach { day ->
                    Text(day, modifier = Modifier.weight(1f), textAlign = TextAlign.Center,
                        fontSize = 12.sp, fontWeight = FontWeight.SemiBold,
                        color = if (day == "CN") KmaRed else OnSurfaceMedium)
                }
            }
            Spacer(Modifier.height(4.dp))

            // Calendar grid
            val today = Calendar.getInstance()
            val firstDay = (currentMonth.clone() as Calendar).apply { set(Calendar.DAY_OF_MONTH, 1) }
            val startDow = firstDay.get(Calendar.DAY_OF_WEEK) - 1
            val daysInMonth = currentMonth.getActualMaximum(Calendar.DAY_OF_MONTH)

            Column(modifier = Modifier.padding(horizontal = 8.dp)) {
                val rows = ((startDow + daysInMonth) + 6) / 7
                for (row in 0 until rows) {
                    Row(modifier = Modifier.fillMaxWidth()) {
                        for (col in 0..6) {
                            val dayNum = row * 7 + col - startDow + 1
                            if (dayNum < 1 || dayNum > daysInMonth) {
                                Box(modifier = Modifier.weight(1f).height(42.dp))
                            } else {
                                val dayCal = (currentMonth.clone() as Calendar).apply { set(Calendar.DAY_OF_MONTH, dayNum) }
                                val dayStr = sdf.format(dayCal.time)
                                val dayColors = eventColorMap[dayStr] ?: emptyList()
                                val isToday = dayNum == today.get(Calendar.DAY_OF_MONTH) &&
                                        currentMonth.get(Calendar.MONTH) == today.get(Calendar.MONTH) &&
                                        currentMonth.get(Calendar.YEAR) == today.get(Calendar.YEAR)
                                val isSelected = dayStr == selectedDateStr

                                Box(
                                    modifier = Modifier.weight(1f).height(42.dp).padding(2.dp)
                                        .background(
                                            when { isSelected -> KmaRed; isToday -> KmaRedSurface; else -> Color.Transparent },
                                            CircleShape
                                        )
                                        .clickable(
                                            indication = null,
                                            interactionSource = remember { MutableInteractionSource() }
                                        ) { selectedDate = dayCal },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text("$dayNum", fontSize = 13.sp,
                                            color = when { isSelected -> White; isToday -> KmaRed; else -> OnSurfaceHigh },
                                            fontWeight = if (isSelected || isToday) FontWeight.Bold else FontWeight.Normal)
                                        if (dayColors.isNotEmpty()) {
                                            Row(
                                                horizontalArrangement = Arrangement.spacedBy(2.dp),
                                                modifier = Modifier.padding(top = 1.dp)
                                            ) {
                                                repeat(dayColors.size.coerceAtMost(4)) {
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
                }
            }

            Divider(modifier = Modifier.padding(vertical = 8.dp), color = Outline)

            // Events for selected date
            Row(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(selectedDateStr, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = OnSurfaceHigh)
                Spacer(Modifier.width(8.dp))
                if (eventsForDate.isNotEmpty()) {
                    Card(shape = RoundedCornerShape(6.dp), backgroundColor = KmaRed, elevation = 0.dp) {
                        Text("${eventsForDate.size}", color = White, fontSize = 11.sp, fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp))
                    }
                }
            }

            // Events list with animated items
            if (eventsForDate.isEmpty()) {
                AnimatedVisibility(
                    visible = visible,
                    enter = fadeIn(tween(400, 300))
                ) {
                Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.EventBusy, contentDescription = null, tint = Outline, modifier = Modifier.size(40.dp))
                        Spacer(Modifier.height(8.dp))
                        Text("Không có lịch học", color = OnSurfaceMedium, style = MaterialTheme.typography.body2)
                    }
                }
                }
            } else {
                LazyColumn(modifier = Modifier.weight(1f).padding(horizontal = 16.dp)) {
                    itemsIndexed(eventsForDate) { index, course ->
                        AnimatedVisibility(
                            visible = visible,
                            enter = slideInVertically(tween(380, index * 70)) { it / 2 } + fadeIn(tween(380, index * 70))
                        ) {
                            EventCard(course = course, onClick = { selectedEvent = course })
                        }
                        Spacer(Modifier.height(8.dp))
                    }
                }
            }
        }
    }

    selectedEvent?.let { course ->
        EventDetailDialog(course = course, selectedDate = selectedDateStr, onDismiss = { selectedEvent = null })
    }

    if (showNotifDialog) NotificationSettingsDialog(
        enabled   = notifEnabled,
        onEnable  = {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                permLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            } else {
                notifEnabled = true
                NotificationScheduler.schedule(context)
            }
        },
        onDisable = {
            notifEnabled = false
            NotificationScheduler.cancel(context)
        },
        onDismiss = { showNotifDialog = false }
    )

    if (showLogoutDialog) ConfirmDialog(
        title = "Đăng xuất",
        message = "Bạn có chắc muốn đăng xuất khỏi lịch học?",
        confirmText = "Đăng xuất",
        onConfirm = { prefs.logout(); navController.navigate(Routes.LOGIN) { popUpTo(0) { inclusive = true } } },
        onDismiss = { showLogoutDialog = false }
    )
}

@Composable
private fun EventCard(course: CourseSchedule, onClick: () -> Unit) {
    val color = getColorFromSeed(course.course_name)
    val dayIndex = 0
    val lessonGroup = course.lessons.split(" ").getOrNull(dayIndex) ?: course.lessons.split(" ").firstOrNull() ?: ""
    val time = getLessonTime(lessonGroup)

    var pressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.97f else 1f,
        animationSpec = spring(Spring.DampingRatioMediumBouncy)
    )

    Card(
        modifier = Modifier.fillMaxWidth().scale(scale)
            .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) {
                pressed = true; onClick()
            },
        shape = RoundedCornerShape(14.dp),
        elevation = 0.dp,
        backgroundColor = White
    ) {
        Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            // Time column
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.width(44.dp)) {
                Text(time.start, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = KmaRed)
                Box(modifier = Modifier.width(1.dp).height(16.dp).background(Outline))
                Text(time.end, fontSize = 11.sp, color = OnSurfaceMedium)
            }
            Spacer(Modifier.width(12.dp))
            Box(modifier = Modifier.width(3.dp).height(52.dp).background(color, RoundedCornerShape(2.dp)))
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(course.course_name, fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = OnSurfaceHigh)
                Spacer(Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Room, contentDescription = null, modifier = Modifier.size(12.dp), tint = OnSurfaceMedium)
                    Spacer(Modifier.width(3.dp))
                    Text(course.study_location, fontSize = 11.sp, color = OnSurfaceMedium)
                    Spacer(Modifier.width(10.dp))
                    Icon(Icons.Default.Person, contentDescription = null, modifier = Modifier.size(12.dp), tint = OnSurfaceMedium)
                    Spacer(Modifier.width(3.dp))
                    Text(course.teacher, fontSize = 11.sp, color = OnSurfaceMedium, maxLines = 1)
                }
            }
            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Outline, modifier = Modifier.size(18.dp))
        }
    }
}

@Composable
private fun EventDetailDialog(course: CourseSchedule, selectedDate: String, onDismiss: () -> Unit) {
    val color = getColorFromSeed(course.course_name)
    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(20.dp),
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(10.dp).background(color, CircleShape))
                Spacer(Modifier.width(8.dp))
                Text(course.course_name, fontWeight = FontWeight.Bold, fontSize = 15.sp)
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                DetailItem(Icons.Default.Tag, "Mã môn", course.course_code)
                DetailItem(Icons.Default.Person, "Giảng viên", course.teacher)
                DetailItem(Icons.Default.Room, "Phòng học", course.study_location)
                val dayIndex = course.study_days.split(" ").indexOfFirst { it.trim() == selectedDate }
                val lessonGroup = course.lessons.split(" ").getOrNull(dayIndex) ?: course.lessons.split(" ").firstOrNull() ?: ""
                val time = getLessonTime(lessonGroup)
                DetailItem(Icons.Default.Schedule, "Giờ học", "${time.start} – ${time.end}")
                DetailItem(Icons.Default.CalendarToday, "Tiết học", lessonGroup)
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text("Đóng", color = KmaRed, fontWeight = FontWeight.SemiBold) } }
    )
}

@Composable
private fun DetailItem(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, value: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(modifier = Modifier.size(32.dp).background(KmaRedSurface, RoundedCornerShape(8.dp)), contentAlignment = Alignment.Center) {
            Icon(icon, contentDescription = null, tint = KmaRed, modifier = Modifier.size(16.dp))
        }
        Spacer(Modifier.width(10.dp))
        Column {
            Text(label, fontSize = 10.sp, color = OnSurfaceMedium, fontWeight = FontWeight.Medium)
            Text(value, fontSize = 13.sp, color = OnSurfaceHigh, fontWeight = FontWeight.Medium)
        }
    }
}

fun getColorFromSeed(seed: String): Color {
    var hash = 0
    for (char in seed) hash = char.code + ((hash shl 5) - hash)
    val hue = Math.abs(hash % 360).toFloat()
    val argb = android.graphics.Color.HSVToColor(floatArrayOf(hue, 0.65f, 0.55f))
    return Color(argb)
}

@Composable
fun NotificationSettingsDialog(
    enabled: Boolean,
    onEnable: () -> Unit,
    onDisable: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(20.dp),
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier.size(40.dp).background(KmaRedSurface, RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Notifications, contentDescription = null, tint = KmaRed, modifier = Modifier.size(22.dp))
                }
                Spacer(Modifier.width(12.dp))
                Text("Thông báo lịch học", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    "KMA Legend sẽ nhắc bạn trước mỗi buổi học:",
                    fontSize = 13.sp, color = OnSurfaceMedium
                )
                listOf(
                    Triple(Icons.Default.AccessTime,            "12 giờ trước",  "Nhắc nhở từ sớm"),
                    Triple(Icons.Default.Alarm,                 "6 giờ trước",   "Chuẩn bị cho buổi học"),
                    Triple(Icons.Default.NotificationImportant, "1 giờ trước",   "Sắp đến giờ học!"),
                    Triple(Icons.Default.Timer,                 "30 phút trước", "Chuẩn bị đến lớp"),
                    Triple(Icons.Default.Timer,                 "15 phút trước", "Lên đường thôi!"),
                    Triple(Icons.Default.FlashOn,               "5 phút trước",  "Vào lớp ngay!")
                ).forEach { (icon, time, desc) ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(KmaRedSurface, RoundedCornerShape(10.dp))
                            .padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(icon, contentDescription = null, tint = KmaRed, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(10.dp))
                        Column {
                            Text(time, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = OnSurfaceHigh)
                            Text(desc, fontSize = 11.sp, color = OnSurfaceMedium)
                        }
                    }
                }
                if (enabled) {
                    Card(
                        backgroundColor = SuccessSurface,
                        shape = RoundedCornerShape(10.dp),
                        elevation = 0.dp,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null,
                                tint = Success, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("Thông báo đang bật", fontSize = 12.sp, color = Success, fontWeight = FontWeight.Medium)
                        }
                    }
                }
            }
        },
        confirmButton = {
            if (!enabled) {
                Button(
                    onClick = { onEnable(); onDismiss() },
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(backgroundColor = KmaRed),
                    elevation = ButtonDefaults.elevation(0.dp)
                ) {
                    Icon(Icons.Default.Notifications, contentDescription = null, tint = White, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Bật thông báo", color = White, fontWeight = FontWeight.SemiBold)
                }
            } else {
                TextButton(onClick = onDismiss) {
                    Text("Đóng", color = KmaRed, fontWeight = FontWeight.SemiBold)
                }
            }
        },
        dismissButton = {
            if (enabled) {
                TextButton(onClick = { onDisable(); onDismiss() }) {
                    Text("Tắt thông báo", color = OnSurfaceMedium)
                }
            }
        }
    )
}
