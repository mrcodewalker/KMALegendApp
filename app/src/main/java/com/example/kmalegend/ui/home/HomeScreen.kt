package com.example.kmalegend.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.google.accompanist.pager.*import com.example.kmalegend.data.PrefsManager
import com.example.kmalegend.ui.common.KmaTopBar
import com.example.kmalegend.ui.navigation.Routes
import com.example.kmalegend.ui.theme.*
import kotlinx.coroutines.delay

@OptIn(ExperimentalPagerApi::class)
@Composable
fun HomeScreen(navController: NavController) {
    val context = LocalContext.current
    val prefs = remember { PrefsManager(context) }
    val studentInfo = remember { prefs.getScheduleSecret()?.data?.student_info }
    val pagerState = rememberPagerState()

    val slides = listOf(
        Triple("Môi trường học tập hiện đại", "Cơ sở vật chất tiên tiến, phòng lab đầy đủ thiết bị", Icons.Default.School),
        Triple("Đội ngũ giảng viên chất lượng", "200+ giảng viên trình độ cao, nhiều kinh nghiệm thực tiễn", Icons.Default.People),
        Triple("Cơ hội nghề nghiệp rộng mở", "95% sinh viên có việc làm ngay sau tốt nghiệp", Icons.Default.Work)
    )

                    LaunchedEffect(Unit) {
                        while (true) { delay(5000); pagerState.animateScrollToPage((pagerState.currentPage + 1) % slides.size) }
                    }

    Scaffold(topBar = { KmaTopBar(title = "KMA Legend", navController = navController) }) { padding ->
        LazyColumn(modifier = Modifier.fillMaxSize().padding(padding)) {

            // Welcome banner
            item {
                Box(
                    modifier = Modifier.fillMaxWidth()
                        .background(Brush.verticalGradient(listOf(KmaRed, KmaRedDark)))
                        .padding(horizontal = 20.dp, vertical = 24.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier.size(52.dp).background(White.copy(alpha = 0.2f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Person, contentDescription = null, tint = White, modifier = Modifier.size(30.dp))
                        }
                        Spacer(Modifier.width(14.dp))
                        Column {
                            Text(
                                if (studentInfo != null) "Xin chào, ${studentInfo.display_name.split(" ").lastOrNull() ?: studentInfo.display_name}!"
                                else "Xin chào, sinh viên KMA!",
                                color = White, fontWeight = FontWeight.Bold, fontSize = 18.sp
                            )
                            Text(
                                studentInfo?.student_code ?: "Học viện Kỹ thuật Mật mã",
                                color = White.copy(alpha = 0.75f), fontSize = 13.sp
                            )
                        }
                    }
                }
            }

            // Quick access grid
            item {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Truy cập nhanh", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = OnSurfaceHigh)
                    Spacer(Modifier.height(12.dp))
                    val features = listOf(
                        QuickFeature(Icons.Default.CalendarToday, "Lịch học", KmaRed, Routes.SCHEDULE),
                        QuickFeature(Icons.Default.Grade, "Tra cứu điểm", Color(0xFF1565C0), Routes.SCORES),
                        QuickFeature(Icons.Default.EditNote, "Điểm ảo", Color(0xFF6200EE), Routes.VIRTUAL_SCORES),
                        QuickFeature(Icons.Default.EmojiEvents, "Học bổng", Color(0xFFF57F17), Routes.SCHOLARSHIP),
                        QuickFeature(Icons.Default.EventNote, "Lịch ảo", Color(0xFF00695C), Routes.VIRTUAL_CALENDAR),
                        QuickFeature(Icons.Default.School, "Chương trình", Color(0xFF37474F), Routes.ABOUT)
                    )
                    // 3 columns grid
                    features.chunked(3).forEach { row ->
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            row.forEach { feature ->
                                QuickAccessCard(feature = feature, modifier = Modifier.weight(1f)) {
                                    navController.navigate(feature.route)
                                }
                            }
                            repeat(3 - row.size) { Spacer(Modifier.weight(1f)) }
                        }
                        Spacer(Modifier.height(10.dp))
                    }
                }
            }

            // Carousel
            item {
                Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                    Text("Về KMA", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = OnSurfaceHigh)
                    Spacer(Modifier.height(12.dp))
                    Box {
                        HorizontalPager(count = slides.size, state = pagerState, modifier = Modifier.fillMaxWidth()) { page ->
                            Card(
                                modifier = Modifier.fillMaxWidth().height(140.dp),
                                shape = RoundedCornerShape(16.dp),
                                elevation = 0.dp
                            ) {
                                Box(
                                    modifier = Modifier.fillMaxSize()
                                        .background(Brush.linearGradient(listOf(KmaRed.copy(alpha = 0.85f + page * 0.05f), KmaRedDark))),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(20.dp)) {
                                        Icon(slides[page].third, contentDescription = null, tint = White.copy(alpha = 0.9f), modifier = Modifier.size(32.dp))
                                        Spacer(Modifier.height(8.dp))
                                        Text(slides[page].first, color = White, fontWeight = FontWeight.Bold, fontSize = 15.sp, textAlign = TextAlign.Center)
                                        Text(slides[page].second, color = White.copy(alpha = 0.8f), fontSize = 12.sp, textAlign = TextAlign.Center)
                                    }
                                }
                            }
                        }
                        // Dots indicator
                        Row(
                            modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 10.dp),
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            repeat(slides.size) { i ->
                                Box(modifier = Modifier.size(if (pagerState.currentPage == i) 8.dp else 5.dp)
                                    .background(if (pagerState.currentPage == i) White else White.copy(alpha = 0.4f), CircleShape))
                            }
                        }
                    }
                }
                Spacer(Modifier.height(16.dp))
            }

            // Stats
            item {
                Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                    Text("Thống kê trường", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = OnSurfaceHigh)
                    Spacer(Modifier.height(12.dp))
                    Card(shape = RoundedCornerShape(16.dp), elevation = 0.dp, backgroundColor = SurfaceVariant) {
                        Row(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                            StatItem("5000+", "Sinh viên", Modifier.weight(1f))
                            StatDivider()
                            StatItem("200+", "Giảng viên", Modifier.weight(1f))
                            StatDivider()
                            StatItem("50+", "Phòng học", Modifier.weight(1f))
                            StatDivider()
                            StatItem("95%", "Việc làm", Modifier.weight(1f))
                        }
                    }
                }
                Spacer(Modifier.height(16.dp))
            }

            // Departments
            item {
                Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                    Text("Các khoa", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = OnSurfaceHigh)
                    Spacer(Modifier.height(12.dp))
                    val depts = listOf(
                        "An toàn thông tin" to Icons.Default.Security,
                        "Công nghệ thông tin" to Icons.Default.Computer,
                        "Điện tử viễn thông" to Icons.Default.Router,
                        "An toàn mạng" to Icons.Default.Shield
                    )
                    depts.forEach { (name, icon) ->
                        Card(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                            shape = RoundedCornerShape(12.dp),
                            elevation = 0.dp,
                            backgroundColor = White
                        ) {
                            Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier.size(38.dp).background(KmaRedSurface, RoundedCornerShape(10.dp)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(icon, contentDescription = null, tint = KmaRed, modifier = Modifier.size(20.dp))
                                }
                                Spacer(Modifier.width(14.dp))
                                Text(name, fontWeight = FontWeight.Medium, style = MaterialTheme.typography.body2)
                                Spacer(Modifier.weight(1f))
                                Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Outline, modifier = Modifier.size(18.dp))
                            }
                        }
                    }
                }
                Spacer(Modifier.height(24.dp))
            }

            // Footer
            item {
                Box(modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp), contentAlignment = Alignment.Center) {
                    Text("Mr.CodeWalker × Hải Code Dạo", style = MaterialTheme.typography.caption, color = OnSurfaceMedium)
                }
            }
        }
    }
}

private data class QuickFeature(val icon: ImageVector, val label: String, val color: Color, val route: String)

@Composable
private fun QuickAccessCard(feature: QuickFeature, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Card(
        modifier = modifier.aspectRatio(1f).clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        elevation = 0.dp,
        backgroundColor = feature.color.copy(alpha = 0.08f)
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier.size(40.dp).background(feature.color.copy(alpha = 0.15f), RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(feature.icon, contentDescription = null, tint = feature.color, modifier = Modifier.size(22.dp))
            }
            Spacer(Modifier.height(8.dp))
            Text(feature.label, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = feature.color, textAlign = TextAlign.Center, maxLines = 2)
        }
    }
}

@Composable
private fun StatItem(value: String, label: String, modifier: Modifier = Modifier) {
    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, fontWeight = FontWeight.Bold, fontSize = 18.sp, color = KmaRed)
        Text(label, fontSize = 11.sp, color = OnSurfaceMedium)
    }
}

@Composable
private fun StatDivider() {
    Box(modifier = Modifier.width(1.dp).height(32.dp).background(Outline))
}
