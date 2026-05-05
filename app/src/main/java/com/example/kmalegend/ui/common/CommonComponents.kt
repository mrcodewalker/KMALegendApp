package com.example.kmalegend.ui.common

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.foundation.Image
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import com.example.kmalegend.R
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.navigation.NavController
import com.example.kmalegend.ui.navigation.Routes
import com.example.kmalegend.ui.theme.*
import kotlinx.coroutines.launch

@Composable
fun KmaTopBar(
    title: String,
    navController: NavController,
    showBack: Boolean = false,
    onMenuClick: (() -> Unit)? = null,
    actions: @Composable RowScope.() -> Unit = {}
) {
    TopAppBar(
        title = { Text(title, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.subtitle1, color = White) },
        backgroundColor = KmaRed,
        contentColor = White,
        elevation = 0.dp,
        navigationIcon = {
            if (showBack) {
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(Icons.Default.ArrowBack, contentDescription = null, tint = White)
                }
            } else {
                IconButton(onClick = { onMenuClick?.invoke() }) {
                    Icon(Icons.Default.Menu, contentDescription = null, tint = White)
                }
            }
        },
        actions = actions
    )
}

@Composable
fun KmaDrawerScaffold(
    navController: NavController,
    topBar: @Composable (onMenuClick: () -> Unit) -> Unit,
    content: @Composable (PaddingValues) -> Unit
) {
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    ModalDrawer(
        drawerState = drawerState,
        drawerContent = {
            NavigationDrawerContent(navController = navController, onDismiss = {
                scope.launch { drawerState.close() }
            })
        },
        drawerShape = RoundedCornerShape(topEnd = 20.dp, bottomEnd = 20.dp),
        drawerBackgroundColor = White
    ) {
        Scaffold(
            topBar = { topBar { scope.launch { drawerState.open() } } },
            content = content
        )
    }
}

@Composable
private fun NavigationDrawerContent(navController: NavController, onDismiss: () -> Unit) {
    val context = androidx.compose.ui.platform.LocalContext.current
    var showLogoutDialog by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxHeight()) {
        Column(modifier = Modifier.weight(1f).verticalScroll(rememberScrollState())) {
        // Header
        Box(
            modifier = Modifier.fillMaxWidth()
                .background(KmaRed)
                .padding(20.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Image(
                    painter = painterResource(id = R.drawable.logo),
                    contentDescription = "KMA Logo",
                    modifier = Modifier.size(56.dp)
                )
                Spacer(Modifier.width(12.dp))
                Column {
                    Text("KMA Legend", fontWeight = FontWeight.Bold, color = White, fontSize = 16.sp)
                    Text("Học viện Kỹ thuật Mật mã", color = White.copy(alpha = 0.75f), fontSize = 12.sp)
                }
            }
        }
        // Menu items
        Column(modifier = Modifier.padding(vertical = 8.dp)) {
            val items = listOf(
                Triple(Icons.Default.Home, "Trang chủ", Routes.HOME),
                Triple(Icons.Default.CalendarToday, "Lịch học", Routes.SCHEDULE),
                Triple(Icons.Default.Grade, "Tra cứu điểm", Routes.SCORES),
                Triple(Icons.Default.EditNote, "Bảng điểm ảo", Routes.VIRTUAL_SCORES),
                Triple(Icons.Default.EmojiEvents, "Học bổng", Routes.SCHOLARSHIP),
                Triple(Icons.Default.EventNote, "Lịch ảo", Routes.VIRTUAL_CALENDAR),
                Triple(Icons.Default.School, "Chương trình học", Routes.ABOUT),
                Triple(Icons.Default.Favorite, "Ủng hộ", Routes.DONATE),
                Triple(Icons.Default.Feedback, "Phản hồi", Routes.FEEDBACK),
                Triple(Icons.Default.QuestionAnswer, "Q&A", Routes.QA)
            )
            items.forEach { (icon, label, route) ->
                Row(
                    modifier = Modifier.fillMaxWidth()
                        .clickable { onDismiss(); navController.navigate(route) { launchSingleTop = true } }
                        .padding(horizontal = 20.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier.size(36.dp).background(KmaRedSurface, RoundedCornerShape(10.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(icon, contentDescription = null, tint = KmaRed, modifier = Modifier.size(20.dp))
                    }
                    Spacer(Modifier.width(14.dp))
                    Text(label, style = MaterialTheme.typography.subtitle2, color = OnSurfaceHigh)
                }
            }
        }
        } // end scrollable column

        // Footer logout
        Divider(color = Outline)
        Row(
            modifier = Modifier.fillMaxWidth()
                .clickable { showLogoutDialog = true }
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier.size(36.dp).background(ErrorSurface, RoundedCornerShape(10.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Logout, contentDescription = null, tint = Error, modifier = Modifier.size(20.dp))
            }
            Spacer(Modifier.width(14.dp))
            Text("Đăng xuất", style = MaterialTheme.typography.subtitle2, color = Error, fontWeight = FontWeight.SemiBold)
        }
    } // end outer Column

    if (showLogoutDialog) {
        val prefs = remember { com.example.kmalegend.data.PrefsManager(context) }
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            shape = RoundedCornerShape(20.dp),
            title = { Text("Đăng xuất", fontWeight = FontWeight.Bold) },
            text = { Text("Bạn có chắc muốn đăng xuất?", color = OnSurfaceMedium) },
            confirmButton = {
                Button(
                    onClick = {
                        prefs.logout()
                        onDismiss()
                        navController.navigate(com.example.kmalegend.ui.navigation.Routes.LOGIN) {
                            popUpTo(0) { inclusive = true }
                        }
                    },
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(backgroundColor = Error),
                    elevation = ButtonDefaults.elevation(0.dp)
                ) { Text("Đăng xuất", color = White, fontWeight = FontWeight.SemiBold) }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) {
                    Text("Hủy", color = OnSurfaceMedium)
                }
            }
        )
    }
}

@Composable
fun LoadingOverlay() {
    Box(
        modifier = Modifier.fillMaxSize().background(Black.copy(alpha = 0.35f)),
        contentAlignment = Alignment.Center
    ) {
        Card(shape = RoundedCornerShape(16.dp), elevation = 8.dp) {
            Column(
                modifier = Modifier.padding(28.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                CircularProgressIndicator(color = KmaRed, strokeWidth = 3.dp)
                Spacer(Modifier.height(12.dp))
                Text("Đang tải...", style = MaterialTheme.typography.caption, color = OnSurfaceMedium)
            }
        }
    }
}

enum class ToastType { SUCCESS, ERROR, WARNING, INFO }

@Composable
fun ToastMessage(message: String, type: ToastType, onDismiss: () -> Unit) {
    val (bgColor, icon) = when (type) {
        ToastType.SUCCESS -> Success to Icons.Default.CheckCircle
        ToastType.ERROR -> Error to Icons.Default.Error
        ToastType.WARNING -> Warning to Icons.Default.Warning
        ToastType.INFO -> Info to Icons.Default.Info
    }
    LaunchedEffect(message) { kotlinx.coroutines.delay(3500); onDismiss() }

    Box(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
        Card(
            shape = RoundedCornerShape(12.dp),
            backgroundColor = OnSurfaceHigh,
            elevation = 6.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier.size(32.dp).background(bgColor.copy(alpha = 0.2f), RoundedCornerShape(8.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(icon, contentDescription = null, tint = bgColor, modifier = Modifier.size(18.dp))
                }
                Spacer(Modifier.width(12.dp))
                Text(message, color = White, style = MaterialTheme.typography.body2, modifier = Modifier.weight(1f))
                IconButton(onClick = onDismiss, modifier = Modifier.size(24.dp)) {
                    Icon(Icons.Default.Close, contentDescription = null, tint = White.copy(alpha = 0.6f), modifier = Modifier.size(16.dp))
                }
            }
        }
    }
}

@Composable
fun ConfirmDialog(
    title: String,
    message: String,
    confirmText: String = "Xác nhận",
    cancelText: String = "Hủy",
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(20.dp),
        title = { Text(title, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.h6) },
        text = { Text(message, style = MaterialTheme.typography.body2, color = OnSurfaceMedium) },
        confirmButton = {
            Button(
                onClick = onConfirm,
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(backgroundColor = KmaRed),
                elevation = ButtonDefaults.elevation(0.dp)
            ) { Text(confirmText, color = White, fontWeight = FontWeight.SemiBold) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(cancelText, color = OnSurfaceMedium, fontWeight = FontWeight.Medium)
            }
        }
    )
}

// Reusable stat chip
@Composable
fun StatChip(label: String, value: String, color: Color, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        backgroundColor = color.copy(alpha = 0.08f),
        elevation = 0.dp
    ) {
        Column(
            modifier = Modifier.padding(vertical = 10.dp, horizontal = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(value, color = color, fontWeight = FontWeight.Bold, fontSize = 17.sp)
            Text(label, color = color.copy(alpha = 0.75f), fontSize = 11.sp, fontWeight = FontWeight.Medium)
        }
    }
}

// Section header
@Composable
fun SectionHeader(title: String, modifier: Modifier = Modifier) {
    Text(
        title,
        style = MaterialTheme.typography.subtitle1,
        fontWeight = FontWeight.Bold,
        color = OnSurfaceHigh,
        modifier = modifier.padding(horizontal = 16.dp, vertical = 10.dp)
    )
}
