package com.example.kmalegend.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.navArgument
import com.example.kmalegend.data.PrefsManager
import com.example.kmalegend.ui.about.AboutScreen
import com.example.kmalegend.ui.donate.DonateScreen
import com.example.kmalegend.ui.feedback.FeedbackScreen
import com.example.kmalegend.ui.home.HomeScreen
import com.example.kmalegend.ui.login.LoginScreen
import com.example.kmalegend.ui.profile.ProfileScreen
import com.example.kmalegend.ui.qa.QAScreen
import com.example.kmalegend.ui.schedule.ScheduleScreen
import com.example.kmalegend.ui.scholarship.ScholarshipScreen
import com.example.kmalegend.ui.scores.ScoresScreen
import com.example.kmalegend.ui.scores.VirtualScoresScreen
import com.example.kmalegend.ui.virtualcalendar.VirtualCalendarScreen

object Routes {
    const val LOGIN = "login"
    const val HOME = "home"
    const val SCHEDULE = "schedule"
    const val SCORES = "scores"
    const val SCHOLARSHIP = "scholarship"
    const val VIRTUAL_CALENDAR = "virtual_calendar"
    const val ABOUT = "about"
    const val ABOUT_PROGRAM = "about/{programCode}"
    const val DONATE = "donate"
    const val FEEDBACK = "feedback"
    const val QA = "qa"
    const val VIRTUAL_SCORES = "virtual_scores"
    const val PROFILE = "profile"
    fun aboutProgram(code: String) = "about/$code"
}

@Composable
fun NavGraph(navController: NavHostController) {
    val context = LocalContext.current
    val prefs = PrefsManager(context)
    val startDest = if (prefs.isLoggedIn()) Routes.HOME else Routes.LOGIN

    val backStack by navController.currentBackStackEntryAsState()
    val currentRoute = backStack?.destination?.route
    val showBottomNav = currentRoute in bottomNavRoutes

    Scaffold(
        bottomBar = {
            if (showBottomNav) KmaBottomNav(navController = navController)
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = startDest,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Routes.LOGIN) {
                LoginScreen(navController = navController)
            }
            composable(Routes.HOME) {
                HomeScreen(navController = navController)
            }
            composable(Routes.SCHEDULE) {
                ScheduleScreen(navController = navController)
            }
            composable(Routes.SCORES) {
                ScoresScreen(navController = navController)
            }
            composable(Routes.SCHOLARSHIP) {
                ScholarshipScreen(navController = navController)
            }
            composable(Routes.VIRTUAL_CALENDAR) {
                VirtualCalendarScreen(navController = navController)
            }
            composable(Routes.ABOUT) {
                AboutScreen(navController = navController)
            }
            composable(
                Routes.ABOUT_PROGRAM,
                arguments = listOf(navArgument("programCode") { type = NavType.StringType })
            ) { backStack ->
                val code = backStack.arguments?.getString("programCode") ?: "CT"
                AboutScreen(navController = navController, initialProgramCode = code)
            }
            composable(Routes.DONATE) {
                DonateScreen(navController = navController)
            }
            composable(Routes.FEEDBACK) {
                FeedbackScreen(navController = navController)
            }
            composable(Routes.QA) {
                QAScreen(navController = navController)
            }
            composable(Routes.VIRTUAL_SCORES) {
                VirtualScoresScreen(navController = navController)
            }
            composable(Routes.PROFILE) {
                ProfileScreen(navController = navController)
            }
        }
    }
}
