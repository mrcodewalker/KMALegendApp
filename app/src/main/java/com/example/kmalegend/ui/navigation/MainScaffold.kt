package com.example.kmalegend.ui.navigation

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.kmalegend.ui.theme.*

data class BottomNavItem(
    val route: String,
    val icon: ImageVector,
    val label: String,
    val isFab: Boolean = false
)

val bottomNavItems = listOf(
    BottomNavItem(Routes.HOME, Icons.Default.Home, "Trang chủ"),
    BottomNavItem(Routes.SCHEDULE, Icons.Default.CalendarToday, "Lịch học"),
    BottomNavItem(Routes.VIRTUAL_SCORES, Icons.Default.EditNote, "Điểm ảo", isFab = true),
    BottomNavItem(Routes.VIRTUAL_CALENDAR, Icons.Default.EventNote, "Lịch ảo"),
    BottomNavItem(Routes.PROFILE, Icons.Default.Person, "Hồ sơ")
)

val bottomNavRoutes = bottomNavItems.map { it.route }.toSet()

@Composable
fun KmaBottomNav(navController: NavController) {
    val backStack by navController.currentBackStackEntryAsState()
    val currentRoute = backStack?.destination?.route

    Surface(
        modifier = Modifier.fillMaxWidth(),
        elevation = 12.dp,
        color = White
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .height(64.dp)
                .padding(horizontal = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            bottomNavItems.forEach { item ->
                val selected = currentRoute == item.route
                if (item.isFab) {
                    // Center FAB button
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight(),
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(52.dp)
                                .shadow(8.dp, CircleShape)
                                .background(if (selected) KmaRedDark else KmaRed, CircleShape)
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = null
                                ) {
                                    navController.navigate(item.route) {
                                        popUpTo(Routes.HOME) {
                                            saveState = true
                                        }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(item.icon, contentDescription = item.label,
                                tint = White, modifier = Modifier.size(26.dp))
                        }
                    }
                } else {
                    // Regular tab
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null
                            ) {
                                if (currentRoute != item.route) {
                                    navController.navigate(item.route) {
                                        popUpTo(Routes.HOME) {
                                            saveState = true
                                        }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                            },
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            item.icon,
                            contentDescription = item.label,
                            tint = if (selected) KmaRed else OnSurfaceMedium,
                            modifier = Modifier.size(if (selected) 26.dp else 24.dp)
                        )
                        Spacer(Modifier.height(2.dp))
                        Text(
                            item.label,
                            fontSize = 10.sp,
                            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                            color = if (selected) KmaRed else OnSurfaceMedium,
                            maxLines = 1
                        )
                        // Active indicator dot
                        Spacer(Modifier.height(2.dp))
                        Box(
                            modifier = Modifier
                                .size(if (selected) 4.dp else 0.dp)
                                .background(KmaRed, CircleShape)
                        )
                    }
                }
            }
        }
    }
}
