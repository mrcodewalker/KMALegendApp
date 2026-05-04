package com.example.kmalegend.ui.home

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import com.example.kmalegend.data.PrefsManager
import com.example.kmalegend.ui.common.KmaTopBar
import com.example.kmalegend.ui.common.KmaDrawerScaffold
import com.example.kmalegend.ui.navigation.Routes
import com.example.kmalegend.ui.theme.*
import kotlinx.coroutines.delay

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun HomeScreen(navController: NavController) {
    val context = LocalContext.current
    val prefs = remember { PrefsManager(context) }
    val studentInfo = remember { prefs.getScheduleSecret()?.data?.student_info }
    val slides = listOf(
        Triple("Môi trường học tập hiện đại", "Cơ sở vật chất tiên tiến, phòng lab đầy đủ thiết bị", Icons.Default.School),
        Triple("Đội ngũ giảng viên chất lượng", "200+ giảng viên trình độ cao, nhiều kinh nghiệm thực tiễn", Icons.Default.People),
        Triple("Cơ hội nghề nghiệp rộng mở", "95% sinh viên có việc làm ngay sau tốt nghiệp", Icons.Default.Work)
    )
    val pagerState = rememberPagerState(initialPage = 0)

    // Auto-scroll carousel
    LaunchedEffect(Unit) {
        while (true) {
            delay(5000)
            pagerState.animateScrollToPage((pagerState.currentPage + 1) % slides.size)
        }
    }

    // Entrance animation
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        delay(80)
        visible = true
    }

    val features = listOf(
        QuickFeature(Icons.Default.CalendarToday, "Lịch học", KmaRed, Routes.SCHEDULE),
        QuickFeature(Icons.Default.Grade, "Tra cứu điểm", KmaRed, Routes.SCORES),
        QuickFeature(Icons.Default.EditNote, "Điểm ảo", KmaRed, Routes.VIRTUAL_SCORES),
        QuickFeature(Icons.Default.EmojiEvents, "Học bổng", KmaRed, Routes.SCHOLARSHIP),
        QuickFeature(Icons.Default.EventNote, "Lịch ảo", KmaRed, Routes.VIRTUAL_CALENDAR),
        QuickFeature(Icons.Default.School, "Chương trình", KmaRed, Routes.ABOUT)
    )

    KmaDrawerScaffold(
        navController = navController,
        topBar = { onMenuClick ->
            KmaTopBar(title = "KMA Legend", navController = navController, onMenuClick = onMenuClick)
        }
    ) { padding ->
        LazyColumn(modifier = Modifier.fillMaxSize().padding(padding)) {

            // ── Welcome banner with animated shimmer ───────────────────────
            item {
                AnimatedVisibility(
                    visible = visible,
                    enter = slideInVertically(
                        tween(500, easing = FastOutSlowInEasing)
                    ) { -it / 2 } + fadeIn(tween(500))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .drawBehind {
                                // Subtle radial glow top-right
                                drawCircle(
                                    brush = Brush.radialGradient(
                                        colors = listOf(Color.White.copy(0.1f), Color.Transparent),
                                        center = Offset(size.width * 0.85f, 0f),
                                        radius = size.height * 1.5f
                                    ),
                                    radius = size.height * 1.5f,
                                    center = Offset(size.width * 0.85f, 0f)
                                )
                            }
                            .background(Brush.linearGradient(listOf(KmaRed, KmaRedDark, Color(0xFF770000))))
                            .padding(horizontal = 20.dp, vertical = 24.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            // Avatar circle with pulse ring
                            Box(contentAlignment = Alignment.Center) {
                                // Outer pulse ring
                                val pulse = rememberInfiniteTransition()
                                val pulseScale by pulse.animateFloat(
                                    initialValue = 1f,
                                    targetValue = 1.25f,
                                    animationSpec = infiniteRepeatable(
                                        tween(1800, easing = FastOutSlowInEasing),
                                        repeatMode = RepeatMode.Reverse
                                    )
                                )
                                Box(
                                    modifier = Modifier
                                        .size(52.dp)
                                        .scale(pulseScale)
                                        .background(Color.White.copy(alpha = 0.1f), CircleShape)
                                )
                                Box(
                                    modifier = Modifier
                                        .size(52.dp)
                                        .background(Color.White.copy(alpha = 0.2f), CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.Person, contentDescription = null, tint = Color.White, modifier = Modifier.size(30.dp))
                                }
                            }
                            Spacer(Modifier.width(14.dp))
                            Column {
                                Text(
                                    if (studentInfo != null)
                                        "Xin chào, ${studentInfo.display_name.split(" ").lastOrNull() ?: studentInfo.display_name}! 👋"
                                    else "Xin chào, sinh viên KMA! 👋",
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 17.sp
                                )
                                Spacer(Modifier.height(2.dp))
                                Text(
                                    studentInfo?.student_code ?: "Học viện Kỹ thuật Mật mã",
                                    color = Color.White.copy(alpha = 0.72f),
                                    fontSize = 13.sp
                                )
                            }
                        }
                    }
                }
            }

            // ── Notice if no schedule data ─────────────────────────────────
            if (studentInfo == null) {
                item {
                    AnimatedVisibility(
                        visible = visible,
                        enter = slideInVertically(tween(500, 100)) { it / 2 } + fadeIn(tween(500, 100))
                    ) {
                        Card(
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                            shape = RoundedCornerShape(16.dp),
                            elevation = 0.dp,
                            backgroundColor = InfoSurface
                        ) {
                            Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Info, contentDescription = null, tint = Info, modifier = Modifier.size(22.dp))
                                Spacer(Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("Chưa có dữ liệu lịch học", fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = Info)
                                    Text("Vào Lịch học để đăng nhập và tải dữ liệu", fontSize = 12.sp, color = Info.copy(alpha = 0.8f))
                                }
                                Spacer(Modifier.width(8.dp))
                                TextButton(
                                    onClick = { navController.navigate(Routes.SCHEDULE) },
                                    colors = ButtonDefaults.textButtonColors(contentColor = Info)
                                ) { Text("Vào ngay", fontWeight = FontWeight.SemiBold, fontSize = 12.sp) }
                            }
                        }
                    }
                }
            }

            // ── Quick access grid with staggered animation ─────────────────
            item {
                AnimatedVisibility(
                    visible = visible,
                    enter = slideInVertically(tween(520, 180)) { it / 2 } + fadeIn(tween(520, 180))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        SectionHeader(title = "Truy cập nhanh", icon = Icons.Default.Apps)
                        Spacer(Modifier.height(12.dp))
                        features.chunked(3).forEachIndexed { rowIdx, row ->
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                row.forEachIndexed { colIdx, feature ->
                                    val itemDelay = (rowIdx * 3 + colIdx) * 60
                                    AnimatedQuickAccessCard(
                                        feature = feature,
                                        modifier = Modifier.weight(1f),
                                        delayMs = itemDelay,
                                        visible = visible
                                    ) {
                                        navController.navigate(feature.route)
                                    }
                                }
                                repeat(3 - row.size) { Spacer(Modifier.weight(1f)) }
                            }
                            Spacer(Modifier.height(10.dp))
                        }
                    }
                }
            }

            // ── Carousel ────────────────────────────────────────────────────
            item {
                AnimatedVisibility(
                    visible = visible,
                    enter = slideInVertically(tween(550, 300)) { it / 2 } + fadeIn(tween(550, 300))
                ) {
                    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                        SectionHeader(title = "Về KMA", icon = Icons.Default.Info)
                        Spacer(Modifier.height(12.dp))
                        Box {
                            HorizontalPager(pageCount = slides.size, state = pagerState, modifier = Modifier.fillMaxWidth()) { page ->
                                val pageOffset = (pagerState.currentPage - page) + pagerState.currentPageOffsetFraction
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(148.dp)
                                        .graphicsLayer {
                                            // Parallax-like card scale
                                            val absOffset = kotlin.math.abs(pageOffset)
                                            scaleY = 1f - absOffset * 0.06f
                                            alpha = 1f - absOffset * 0.4f
                                        },
                                    shape = RoundedCornerShape(20.dp),
                                    elevation = 0.dp
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .background(
                                                Brush.linearGradient(
                                                    listOf(
                                                        KmaRed.copy(alpha = 0.88f + page * 0.04f),
                                                        KmaRedDark,
                                                        Color(0xFF660000)
                                                    )
                                                )
                                            )
                                            .drawBehind {
                                                drawCircle(
                                                    color = Color.White.copy(alpha = 0.06f),
                                                    radius = size.height,
                                                    center = Offset(size.width * 0.85f, size.height * 0.3f)
                                                )
                                            },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(20.dp)) {
                                            Box(
                                                modifier = Modifier
                                                    .size(44.dp)
                                                    .background(Color.White.copy(alpha = 0.15f), CircleShape),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(slides[page].third, null, tint = Color.White, modifier = Modifier.size(24.dp))
                                            }
                                            Spacer(Modifier.height(10.dp))
                                            Text(slides[page].first, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp, textAlign = TextAlign.Center)
                                            Spacer(Modifier.height(4.dp))
                                            Text(slides[page].second, color = Color.White.copy(0.8f), fontSize = 12.sp, textAlign = TextAlign.Center)
                                        }
                                    }
                                }
                            }
                            // Animated dots indicator
                            Row(
                                modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 10.dp),
                                horizontalArrangement = Arrangement.spacedBy(5.dp)
                            ) {
                                repeat(slides.size) { i ->
                                    val selected = pagerState.currentPage == i
                                    val dotWidth by animateDpAsState(if (selected) 20.dp else 6.dp, tween(300))
                                    Box(
                                        modifier = Modifier
                                            .width(dotWidth)
                                            .height(6.dp)
                                            .clip(RoundedCornerShape(3.dp))
                                            .background(if (selected) Color.White else Color.White.copy(0.35f))
                                    )
                                }
                            }
                        }
                    }
                    Spacer(Modifier.height(16.dp))
                }
            }

            // ── Stats ────────────────────────────────────────────────────────
            item {
                AnimatedVisibility(
                    visible = visible,
                    enter = slideInVertically(tween(560, 400)) { it / 2 } + fadeIn(tween(560, 400))
                ) {
                    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                        SectionHeader(title = "Thống kê trường", icon = Icons.Default.BarChart)
                        Spacer(Modifier.height(12.dp))
                        Card(shape = RoundedCornerShape(20.dp), elevation = 0.dp, backgroundColor = SurfaceVariant) {
                            Row(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                                AnimatedStatItem("5000+", "Sinh viên", Modifier.weight(1f))
                                StatDivider()
                                AnimatedStatItem("200+", "Giảng viên", Modifier.weight(1f))
                                StatDivider()
                                AnimatedStatItem("50+", "Phòng học", Modifier.weight(1f))
                                StatDivider()
                                AnimatedStatItem("95%", "Việc làm", Modifier.weight(1f))
                            }
                        }
                    }
                    Spacer(Modifier.height(16.dp))
                }
            }

            // ── Departments ─────────────────────────────────────────────────
            item {
                AnimatedVisibility(
                    visible = visible,
                    enter = slideInVertically(tween(580, 500)) { it / 2 } + fadeIn(tween(580, 500))
                ) {
                    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                        SectionHeader(title = "Các khoa", icon = Icons.Default.AccountBalance)
                        Spacer(Modifier.height(12.dp))
                        val depts = listOf(
                            Triple("An toàn thông tin", Icons.Default.Security, "AT"),
                            Triple("Công nghệ thông tin", Icons.Default.Computer, "CT"),
                            Triple("Điện tử viễn thông", Icons.Default.Router, "DT"),
                            Triple("An toàn mạng", Icons.Default.Shield, "AT")
                        )
                        depts.forEachIndexed { idx, (name, icon, code) ->
                            AnimatedDeptCard(name = name, icon = icon, code = code, delayMs = idx * 80, visible = visible) {
                                navController.navigate(Routes.aboutProgram(code))
                            }
                        }
                    }
                    Spacer(Modifier.height(24.dp))
                }
            }

            // ── Footer ───────────────────────────────────────────────────────
            item {
                AnimatedVisibility(
                    visible = visible,
                    enter = fadeIn(tween(600, 600))
                ) {
                    Box(modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp), contentAlignment = Alignment.Center) {
                        Text("Mr.CodeWalker × Hải Code Dạo", style = MaterialTheme.typography.caption, color = OnSurfaceMedium)
                    }
                }
            }
        }
    }
}

// ── Reusable components ──────────────────────────────────────────────────────

@Composable
private fun SectionHeader(title: String, icon: ImageVector) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier.size(28.dp).background(KmaRedSurface, RoundedCornerShape(8.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = KmaRed, modifier = Modifier.size(16.dp))
        }
        Spacer(Modifier.width(10.dp))
        Text(title, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = OnSurfaceHigh)
        Spacer(Modifier.weight(1f))
        // Gradient line
        Box(
            modifier = Modifier
                .weight(1f)
                .height(1.dp)
                .background(Brush.horizontalGradient(listOf(Outline, Color.Transparent)))
        )
    }
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
private fun AnimatedQuickAccessCard(
    feature: QuickFeature,
    modifier: Modifier = Modifier,
    delayMs: Int = 0,
    visible: Boolean,
    onClick: () -> Unit
) {
    var pressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        if (pressed) 0.93f else 1f,
        spring(dampingRatio = Spring.DampingRatioMediumBouncy)
    )

    AnimatedVisibility(
        visible = visible,
        enter = scaleIn(tween(400, delayMs), initialScale = 0.7f) + fadeIn(tween(400, delayMs)),
        modifier = modifier
    ) {
        Card(
            modifier = Modifier
                .aspectRatio(1f)
                .scale(scale)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) {
                    pressed = true
                    onClick()
                },
            shape = RoundedCornerShape(18.dp),
            elevation = 0.dp,
            backgroundColor = feature.color.copy(alpha = 0.07f)
        ) {
            Column(
                modifier = Modifier.fillMaxSize().padding(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .background(
                            Brush.radialGradient(
                                listOf(
                                    feature.color.copy(alpha = 0.2f),
                                    feature.color.copy(alpha = 0.06f)
                                )
                            ),
                            RoundedCornerShape(14.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(feature.icon, contentDescription = null, tint = feature.color, modifier = Modifier.size(24.dp))
                }
                Spacer(Modifier.height(8.dp))
                Text(feature.label, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = feature.color, textAlign = TextAlign.Center, maxLines = 2)
            }
        }
    }
}

@Composable
private fun AnimatedDeptCard(name: String, icon: ImageVector, code: String, delayMs: Int, visible: Boolean, onClick: () -> Unit) {
    var pressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(if (pressed) 0.97f else 1f, spring(Spring.DampingRatioMediumBouncy))

    AnimatedVisibility(
        visible = visible,
        enter = slideInHorizontally(tween(400, delayMs)) { -it / 3 } + fadeIn(tween(400, delayMs))
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp)
                .scale(scale)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) {
                    pressed = true
                    onClick()
                },
            shape = RoundedCornerShape(14.dp),
            elevation = 0.dp,
            backgroundColor = Color.White
        ) {
            Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(
                            Brush.radialGradient(listOf(KmaRedSurface, KmaRedSurface.copy(alpha = 0.4f))),
                            RoundedCornerShape(12.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(icon, contentDescription = null, tint = KmaRed, modifier = Modifier.size(22.dp))
                }
                Spacer(Modifier.width(14.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(name, fontWeight = FontWeight.Medium, style = MaterialTheme.typography.body2)
                    Text("Xem chương trình học", fontSize = 11.sp, color = OnSurfaceMedium)
                }
                Box(
                    modifier = Modifier.size(28.dp).background(KmaRedSurface, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.ChevronRight, contentDescription = null, tint = KmaRed, modifier = Modifier.size(16.dp))
                }
            }
        }
    }
}

@Composable
private fun AnimatedStatItem(value: String, label: String, modifier: Modifier = Modifier) {
    val transition = rememberInfiniteTransition()
    val pulse by transition.animateFloat(
        initialValue = 1f,
        targetValue = 1.04f,
        animationSpec = infiniteRepeatable(tween(1500, easing = FastOutSlowInEasing), RepeatMode.Reverse)
    )
    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, fontWeight = FontWeight.ExtraBold, fontSize = 18.sp, color = KmaRed, modifier = Modifier.scale(pulse))
        Text(label, fontSize = 11.sp, color = OnSurfaceMedium)
    }
}

private data class QuickFeature(val icon: ImageVector, val label: String, val color: Color, val route: String)

@Composable
private fun StatDivider() {
    Box(modifier = Modifier.width(1.dp).height(36.dp).background(Outline))
}
