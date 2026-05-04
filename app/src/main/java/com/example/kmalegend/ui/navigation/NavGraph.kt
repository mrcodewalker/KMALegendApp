package com.example.kmalegend.ui.navigation

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavType
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
import com.google.accompanist.navigation.animation.AnimatedNavHost
import com.google.accompanist.navigation.animation.composable
import com.google.accompanist.navigation.animation.rememberAnimatedNavController

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

// ── Shared transition constants ───────────────────────────────────────────────
private val slideEasing = FastOutSlowInEasing
private const val SLIDE_DURATION = 380
private const val FADE_DURATION = 300

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun NavGraph(navController: androidx.navigation.NavHostController = rememberAnimatedNavController()) {
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
        AnimatedNavHost(
            navController = navController,
            startDestination = startDest,
            modifier = Modifier.padding(innerPadding),
            // Default: slide from right
            enterTransition = {
                slideInHorizontally(tween(SLIDE_DURATION, easing = slideEasing)) { it / 3 } +
                        fadeIn(tween(FADE_DURATION))
            },
            exitTransition = {
                slideOutHorizontally(tween(SLIDE_DURATION, easing = slideEasing)) { -it / 3 } +
                        fadeOut(tween(FADE_DURATION))
            },
            popEnterTransition = {
                slideInHorizontally(tween(SLIDE_DURATION, easing = slideEasing)) { -it / 3 } +
                        fadeIn(tween(FADE_DURATION))
            },
            popExitTransition = {
                slideOutHorizontally(tween(SLIDE_DURATION, easing = slideEasing)) { it / 3 } +
                        fadeOut(tween(FADE_DURATION))
            }
        ) {
            // LOGIN → zoom-fade exit, home slides in
            composable(
                route = Routes.LOGIN,
                exitTransition = {
                    scaleOut(tween(450, easing = slideEasing), targetScale = 1.06f) +
                            fadeOut(tween(380))
                },
                popEnterTransition = {
                    scaleIn(tween(450, easing = slideEasing), initialScale = 1.06f) +
                            fadeIn(tween(380))
                }
            ) {
                LoginScreen(navController = navController)
            }

            // HOME – slides in from bottom when coming from login
            composable(
                route = Routes.HOME,
                enterTransition = {
                    if (initialState.destination.route == Routes.LOGIN) {
                        scaleIn(tween(450, easing = slideEasing), initialScale = 0.93f) +
                                fadeIn(tween(380))
                    } else {
                        slideInHorizontally(tween(SLIDE_DURATION, easing = slideEasing)) { it / 3 } +
                                fadeIn(tween(FADE_DURATION))
                    }
                }
            ) {
                HomeScreen(navController = navController)
            }

            composable(Routes.SCHEDULE) { ScheduleScreen(navController = navController) }
            composable(Routes.SCORES) { ScoresScreen(navController = navController) }
            composable(Routes.SCHOLARSHIP) { ScholarshipScreen(navController = navController) }
            composable(Routes.VIRTUAL_CALENDAR) { VirtualCalendarScreen(navController = navController) }
            composable(Routes.ABOUT) { AboutScreen(navController = navController) }
            composable(
                route = Routes.ABOUT_PROGRAM,
                arguments = listOf(navArgument("programCode") { type = NavType.StringType })
            ) { backStackEntry ->
                val code = backStackEntry.arguments?.getString("programCode") ?: "CT"
                AboutScreen(navController = navController, initialProgramCode = code)
            }
            composable(Routes.DONATE) { DonateScreen(navController = navController) }
            composable(Routes.FEEDBACK) { FeedbackScreen(navController = navController) }
            composable(Routes.QA) { QAScreen(navController = navController) }
            composable(Routes.VIRTUAL_SCORES) { VirtualScoresScreen(navController = navController) }
            composable(Routes.PROFILE) { ProfileScreen(navController = navController) }
        }
    }
}

