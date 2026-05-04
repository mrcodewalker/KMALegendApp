package com.example.kmalegend.ui.navigation

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
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
        elevation = 16.dp,
        color = Color.White
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
                    FabNavItem(item = item, selected = selected) {
                        navController.navigate(item.route) {
                            popUpTo(Routes.HOME) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                } else {
                    RegularNavItem(item = item, selected = selected) {
                        if (currentRoute != item.route) {
                            navController.navigate(item.route) {
                                popUpTo(Routes.HOME) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun RowScope.FabNavItem(item: BottomNavItem, selected: Boolean, onClick: () -> Unit) {
    // Bounce scale on select
    val fabScale by animateFloatAsState(
        targetValue = if (selected) 1.1f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium)
    )

    // Subtle pulse when selected
    val pulse by rememberInfiniteTransition().animateFloat(
        initialValue = 1f,
        targetValue = if (selected) 1.08f else 1f,
        animationSpec = infiniteRepeatable(
            tween(900, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    Box(
        modifier = Modifier
            .weight(1f)
            .fillMaxHeight(),
        contentAlignment = Alignment.Center
    ) {
        // Glow ring behind FAB (only when selected)
        if (selected) {
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .scale(pulse)
                    .background(
                        Brush.radialGradient(
                            listOf(KmaRed.copy(alpha = 0.22f), Color.Transparent)
                        ),
                        CircleShape
                    )
            )
        }

        // FAB circle button — fixed 44dp, no offset so it stays inside bar
        Box(
            modifier = Modifier
                .size(46.dp)
                .scale(fabScale)
                .shadow(if (selected) 10.dp else 4.dp, CircleShape)
                .background(
                    if (selected)
                        Brush.linearGradient(listOf(KmaRedLight, KmaRed, KmaRedDark))
                    else
                        Brush.linearGradient(listOf(KmaRed, KmaRedDark)),
                    CircleShape
                )
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onClick
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                item.icon,
                contentDescription = item.label,
                tint = Color.White,
                modifier = Modifier.size(22.dp)
            )
        }
    }
}

@Composable
private fun RowScope.RegularNavItem(item: BottomNavItem, selected: Boolean, onClick: () -> Unit) {
    // Bounce on selection
    val iconScale by animateFloatAsState(
        targetValue = if (selected) 1.18f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium)
    )
    // Animated pill width
    val pillWidth by animateDpAsState(
        targetValue = if (selected) 32.dp else 0.dp,
        animationSpec = tween(280, easing = FastOutSlowInEasing)
    )
    // Text alpha
    val textAlpha by animateFloatAsState(
        targetValue = if (selected) 1f else 0.55f,
        animationSpec = tween(200)
    )
    // Vertical icon shift up when selected
    val iconOffsetY by animateDpAsState(
        targetValue = if (selected) (-1).dp else 0.dp,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy)
    )

    Column(
        modifier = Modifier
            .weight(1f)
            .fillMaxHeight()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            ),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Pill chip behind icon
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.offset(y = iconOffsetY)
        ) {
            if (selected) {
                Box(
                    modifier = Modifier
                        .size(width = 44.dp, height = 26.dp)
                        .clip(RoundedCornerShape(13.dp))
                        .background(KmaRedSurface)
                )
            }
            Icon(
                item.icon,
                contentDescription = item.label,
                tint = if (selected) KmaRed else OnSurfaceMedium,
                modifier = Modifier
                    .size(22.dp)
                    .scale(iconScale)
            )
        }
        Spacer(Modifier.height(3.dp))
        Text(
            item.label,
            fontSize = 10.sp,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
            color = if (selected) KmaRed else OnSurfaceMedium,
            maxLines = 1,
            modifier = Modifier.graphicsLayer(alpha = textAlpha)
        )
        Spacer(Modifier.height(2.dp))
        // Animated gradient indicator bar
        Box(
            modifier = Modifier
                .width(pillWidth)
                .height(3.dp)
                .clip(RoundedCornerShape(1.5.dp))
                .background(Brush.horizontalGradient(listOf(KmaRed, KmaRedLight)))
        )
    }
}
