package com.lydia.medrem

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.navigation.navDeepLink
import com.lydia.MetricsScreen
import com.lydia.medrem.domain.repository.StoreThemeRepository
import com.lydia.medrem.ui.screens.auth.AuthScreen
import com.lydia.medrem.ui.screens.create_reminder.CreateReminderScreen
import com.lydia.medrem.ui.screens.edit_reminder.EditReminderScreen
import com.lydia.medrem.ui.screens.reminders.RemindersListScreen
import com.lydia.medrem.ui.screens.settings.SettingsScreen
import com.lydia.medrem.ui.utils.Routes
import com.lydia.medrem.ui.utils.enums.Theme
import com.google.accompanist.navigation.animation.AnimatedNavHost
import com.google.accompanist.navigation.animation.composable
import com.google.accompanist.navigation.animation.rememberAnimatedNavController
import com.google.accompanist.systemuicontroller.rememberSystemUiController

@RequiresApi(Build.VERSION_CODES.O)
@ExperimentalMaterialApi
@ExperimentalFoundationApi
@ExperimentalComposeUiApi
@ExperimentalAnimationApi
@Composable
fun Navigation(
    repository: StoreThemeRepository,
) {
    val currentTheme = repository.getTheme.collectAsState(initial = Theme.AUTO).value

    val systemTheme = isSystemInDarkTheme()
    val background = MaterialTheme.colors.background


    val navController = rememberAnimatedNavController()
    val systemUiController = rememberSystemUiController()
    val uri = "https://example.com"

    SideEffect {
        systemUiController.setSystemBarsColor(
            color = background,
            darkIcons = when (currentTheme) {
                Theme.AUTO -> !systemTheme
                Theme.LIGHT -> true
                Theme.DARK -> false
            }
        )
    }

    Scaffold(
        backgroundColor = MaterialTheme.colors.background
    ) {
        AnimatedNavHost(
            navController = navController,
            startDestination = Routes.AUTH
        ) {
            composable(
                route = Routes.AUTH,
                enterTransition = {
                    slideInHorizontally(
                        initialOffsetX = { 1000 },
                        animationSpec = tween(700)
                    ) + fadeIn()
                },
                exitTransition = {
                    slideOutHorizontally(
                        targetOffsetX = { 1000 },
                        animationSpec = tween(700)
                    ) + fadeOut()
                }
            ) {
                AuthScreen(
                    onNavigate = {
                        navController.navigate(it.route) {
                            popUpTo(Routes.AUTH) { inclusive = true }
                            launchSingleTop = true
                        }
                    },
                    onPopBackStack = {
                        navController.popBackStack()
                    }
                )
            }

            composable(route = Routes.REMINDERS) {
                RemindersListScreen(onNavigate = {
                    navController.navigate(it.route) {
                        launchSingleTop = true
                    }
                })
            }

            composable(
                route = Routes.CREATE_REMINDER,
                enterTransition = {
                    slideInHorizontally(
                        initialOffsetX = { 1000 },
                        animationSpec = tween(700)
                    ) + fadeIn()
                },
                popEnterTransition = {
                    slideInHorizontally(
                        initialOffsetX = { 1000 },
                        animationSpec = tween(700)
                    ) + fadeIn()
                },
                exitTransition = {
                    slideOutHorizontally(
                        targetOffsetX = { 1000 },
                        animationSpec = tween(700)
                    ) + fadeOut()
                },
                popExitTransition = {
                    slideOutHorizontally(
                        targetOffsetX = { 1000 },
                        animationSpec = tween(700)
                    ) + fadeOut()
                }
            ) {
                CreateReminderScreen(onPopBackStack = {
                    navController.popBackStack()
                })
            }

            composable(
                route = Routes.EDIT_REMINDER,
                deepLinks = listOf(
                    navDeepLink { uriPattern = "$uri/reminderId={reminderId}" }
                ),
                enterTransition = {
                    slideInHorizontally(
                        initialOffsetX = { 1000 },
                        animationSpec = tween(700)
                    ) + fadeIn()
                },
                popEnterTransition = {
                    slideInHorizontally(
                        initialOffsetX = { 1000 },
                        animationSpec = tween(700)
                    ) + fadeIn()
                },
                exitTransition = {
                    slideOutHorizontally(
                        targetOffsetX = { 1000 },
                        animationSpec = tween(700)
                    ) + fadeOut()
                },
                popExitTransition = {
                    slideOutHorizontally(
                        targetOffsetX = { 1000 },
                        animationSpec = tween(700)
                    ) + fadeOut()
                }
            ) {
                EditReminderScreen(onPopBackStack = {
                    navController.popBackStack()
                })
            }

            composable(
                route = Routes.SETTINGS,
                enterTransition = {
                    slideInHorizontally(
                        initialOffsetX = { 1000 },
                        animationSpec = tween(700)
                    ) + fadeIn()
                },
                popEnterTransition = {
                    slideInHorizontally(
                        initialOffsetX = { 1000 },
                        animationSpec = tween(700)
                    ) + fadeIn()
                },
                exitTransition = {
                    slideOutHorizontally(
                        targetOffsetX = { 1000 },
                        animationSpec = tween(700)
                    ) + fadeOut()
                },
                popExitTransition = {
                    slideOutHorizontally(
                        targetOffsetX = { 1000 },
                        animationSpec = tween(700)
                    ) + fadeOut()
                }
            ) {
                SettingsScreen(onPopBackStack = {
                    navController.popBackStack()
                })
            }
            composable(route = Routes.METRICS, enterTransition = {
                slideInHorizontally(
                    initialOffsetX = { 1000 },
                    animationSpec = tween(700)
                ) + fadeIn()
            },
                popEnterTransition = {
                    slideInHorizontally(
                        initialOffsetX = { 1000 },
                        animationSpec = tween(700)
                    ) + fadeIn()
                },
                exitTransition = {
                    slideOutHorizontally(
                        targetOffsetX = { 1000 },
                        animationSpec = tween(700)
                    ) + fadeOut()
                },
                popExitTransition = {
                    slideOutHorizontally(
                        targetOffsetX = { 1000 },
                        animationSpec = tween(700)
                    ) + fadeOut()
                }) {
                MetricsScreen()
            }
        }
    }
}