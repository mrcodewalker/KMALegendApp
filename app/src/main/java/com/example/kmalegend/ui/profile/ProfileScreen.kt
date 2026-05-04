package com.example.kmalegend.ui.profile

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.kmalegend.data.PrefsManager
import com.example.kmalegend.ui.common.ConfirmDialog
import com.example.kmalegend.ui.navigation.Routes
import com.example.kmalegend.ui.theme.*

@Composable
fun ProfileScreen(navController: NavController) {
    val context = LocalContext.current
    val prefs = remember { PrefsManager(context) }
    val studentInfo = remember { prefs.getScheduleSecret()?.data?.student_info }

    var avatarUri by remember { mutableStateOf(prefs.getAvatarUri()?.let { Uri.parse(it) }) }
    var showLogoutDialog by remember { mutableStateOf(false) }

    // ── Staggered entrance ────────────────────────────────────────────────────
    var headerVisible by remember { mutableStateOf(false) }
    var card1Visible  by remember { mutableStateOf(false) }
    var card2Visible  by remember { mutableStateOf(false) }
    var btnVisible    by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        kotlinx.coroutines.delay(60);  headerVisible = true
        kotlinx.coroutines.delay(130); card1Visible  = true
        kotlinx.coroutines.delay(100); card2Visible  = true
        kotlinx.coroutines.delay(80);  btnVisible    = true
    }

    val avatarPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            context.contentResolver.takePersistableUriPermission(
                it, android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION
            )
            avatarUri = it
            prefs.saveAvatarUri(it.toString())
        }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize().background(Surface),
        contentPadding = PaddingValues(bottom = 32.dp)
    ) {
        // ── Header ────────────────────────────────────────────────────────────
        item {
            AnimatedVisibility(
                visible = headerVisible,
                enter = slideInVertically(tween(500, easing = FastOutSlowInEasing)) { -it } + fadeIn(tween(400))
            ) {
                Box(
                    modifier = Modifier.fillMaxWidth().height(220.dp)
                        .background(Brush.verticalGradient(listOf(KmaRed, KmaRedDark)))
                ) {
                    Column(
                        modifier = Modifier.fillMaxSize().padding(bottom = 24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Bottom
                    ) {
                        Box(contentAlignment = Alignment.BottomEnd) {
                            Box(
                                modifier = Modifier.size(96.dp)
                                    .clip(CircleShape)
                                    .border(3.dp, White, CircleShape)
                                    .background(White.copy(alpha = 0.2f), CircleShape)
                                    .clickable(
                                        interactionSource = remember { MutableInteractionSource() },
                                        indication = null
                                    ) { avatarPicker.launch("image/*") },
                                contentAlignment = Alignment.Center
                            ) {
                                if (avatarUri != null) {
                                    AsyncImage(
                                        model = avatarUri,
                                        contentDescription = "Avatar",
                                        modifier = Modifier.fillMaxSize().clip(CircleShape),
                                        contentScale = ContentScale.Crop
                                    )
                                } else {
                                    val initials = studentInfo?.display_name
                                        ?.split(" ")?.takeLast(2)
                                        ?.mapNotNull { it.firstOrNull()?.uppercaseChar() }
                                        ?.joinToString("") ?: "KM"
                                    Text(initials, color = White, fontSize = 28.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                            // Camera badge
                            Box(
                                modifier = Modifier.size(28.dp)
                                    .background(White, CircleShape)
                                    .border(1.5.dp, KmaRed, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.CameraAlt, contentDescription = null,
                                    tint = KmaRed, modifier = Modifier.size(16.dp))
                            }
                        }
                        Spacer(Modifier.height(10.dp))
                        Text(
                            studentInfo?.display_name ?: "Sinh viên KMA",
                            color = White, fontWeight = FontWeight.Bold, fontSize = 18.sp
                        )
                        Text(
                            studentInfo?.student_code ?: "",
                            color = White.copy(alpha = 0.8f), fontSize = 13.sp
                        )
                    }
                }
            }
        }

        // ── Info card ─────────────────────────────────────────────────────────
        item {
            AnimatedVisibility(
                visible = card1Visible,
                enter = slideInVertically(tween(450, 60, FastOutSlowInEasing)) { it / 2 } + fadeIn(tween(400, 60))
            ) {
                Card(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    shape = RoundedCornerShape(16.dp),
                    elevation = 0.dp,
                    backgroundColor = White
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Thông tin cá nhân", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = OnSurfaceHigh)
                        Spacer(Modifier.height(12.dp))
                        if (studentInfo != null) {
                            ProfileInfoRow(Icons.Default.Badge, "Mã sinh viên", studentInfo.student_code)
                            ProfileInfoRow(Icons.Default.Person, "Họ và tên", studentInfo.display_name)
                            if (studentInfo.gender.isNotEmpty())
                                ProfileInfoRow(Icons.Default.Wc, "Giới tính", studentInfo.gender)
                            if (studentInfo.birthday.isNotEmpty())
                                ProfileInfoRow(Icons.Default.Cake, "Ngày sinh", studentInfo.birthday)
                            if (studentInfo.birth_place.isNotEmpty())
                                ProfileInfoRow(Icons.Default.LocationOn, "Quê quán", studentInfo.birth_place)
                            if (studentInfo.phone.isNotEmpty())
                                ProfileInfoRow(Icons.Default.Phone, "Điện thoại", studentInfo.phone)
                            if (studentInfo.email.isNotEmpty())
                                ProfileInfoRow(Icons.Default.Email, "Email", studentInfo.email)
                            if (studentInfo.enroll_semester.isNotEmpty())
                                ProfileInfoRow(Icons.Default.School, "Nhập học", studentInfo.enroll_semester)
                        } else {
                            Box(modifier = Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) {
                                Text("Chưa có dữ liệu. Vào Lịch học để đăng nhập.", color = OnSurfaceMedium, fontSize = 13.sp)
                            }
                        }
                    }
                }
            }
        }

        // ── Quick links ───────────────────────────────────────────────────────
        item {
            AnimatedVisibility(
                visible = card2Visible,
                enter = slideInVertically(tween(450, 120, FastOutSlowInEasing)) { it / 2 } + fadeIn(tween(400, 120))
            ) {
                Card(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                    shape = RoundedCornerShape(16.dp),
                    elevation = 0.dp,
                    backgroundColor = White
                ) {
                    Column(modifier = Modifier.padding(vertical = 8.dp)) {
                        Text("Tiện ích", fontWeight = FontWeight.Bold, fontSize = 15.sp,
                            color = OnSurfaceHigh, modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp))
                        ProfileMenuRow(Icons.Default.Favorite, "Ủng hộ dự án", Color(0xFFE91E63)) {
                            navController.navigate(Routes.DONATE)
                        }
                        ProfileMenuRow(Icons.Default.Feedback, "Gửi phản hồi", KmaBlue) {
                            navController.navigate(Routes.FEEDBACK)
                        }
                        ProfileMenuRow(Icons.Default.QuestionAnswer, "Q&A", Color(0xFF00695C)) {
                            navController.navigate(Routes.QA)
                        }
                        if (avatarUri != null) {
                            ProfileMenuRow(Icons.Default.DeleteOutline, "Xóa ảnh đại diện", OnSurfaceMedium) {
                                avatarUri = null
                                prefs.clearAvatarUri()
                            }
                        }
                    }
                }
            }
        }

        // ── Logout ────────────────────────────────────────────────────────────
        item {
            AnimatedVisibility(
                visible = btnVisible,
                enter = fadeIn(tween(350, 200)) + slideInVertically(tween(350, 200)) { it / 3 }
            ) {
                Column {
                    Spacer(Modifier.height(16.dp))
                    Button(
                        onClick = { showLogoutDialog = true },
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp).height(52.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(backgroundColor = ErrorSurface),
                        elevation = ButtonDefaults.elevation(0.dp)
                    ) {
                        Icon(Icons.Default.Logout, contentDescription = null, tint = Error, modifier = Modifier.size(20.dp))
                        Spacer(Modifier.width(10.dp))
                        Text("Đăng xuất", color = Error, fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
                    }
                }
            }
        }

        // ── Footer ────────────────────────────────────────────────────────────
        item {
            Spacer(Modifier.height(16.dp))
            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                Text("Mr.CodeWalker × Hải Code Dạo", style = MaterialTheme.typography.caption, color = OnSurfaceMedium)
            }
        }
    }

    if (showLogoutDialog) {
        ConfirmDialog(
            title = "Đăng xuất",
            message = "Bạn có chắc muốn đăng xuất?",
            confirmText = "Đăng xuất",
            onConfirm = {
                prefs.logout()
                navController.navigate(Routes.LOGIN) { popUpTo(0) { inclusive = true } }
            },
            onDismiss = { showLogoutDialog = false }
        )
    }
}

@Composable
private fun ProfileInfoRow(icon: ImageVector, label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 7.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier.size(34.dp).background(KmaRedSurface, RoundedCornerShape(9.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = KmaRed, modifier = Modifier.size(18.dp))
        }
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(label, fontSize = 11.sp, color = OnSurfaceMedium, fontWeight = FontWeight.Medium)
            Text(value, fontSize = 13.sp, color = OnSurfaceHigh, fontWeight = FontWeight.Medium)
        }
    }
    Divider(color = Outline.copy(alpha = 0.5f))
}

@Composable
private fun ProfileMenuRow(icon: ImageVector, label: String, color: Color, onClick: () -> Unit) {
    var pressed by remember { mutableStateOf(false) }
    val bgAlpha by animateFloatAsState(if (pressed) 0.08f else 0f, tween(150))
    Row(
        modifier = Modifier.fillMaxWidth()
            .background(color.copy(alpha = bgAlpha))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) { pressed = true; onClick() }
            .padding(horizontal = 16.dp, vertical = 13.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier.size(36.dp).background(color.copy(alpha = 0.1f), RoundedCornerShape(10.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(20.dp))
        }
        Spacer(Modifier.width(14.dp))
        Text(label, style = MaterialTheme.typography.subtitle2, color = OnSurfaceHigh, modifier = Modifier.weight(1f))
        Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Outline, modifier = Modifier.size(18.dp))
    }
}
