package com.flowlink.app.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.flowlink.app.ui.screens.ForgotPasswordScreen
import com.flowlink.app.ui.screens.LoginScreen
import com.flowlink.app.ui.screens.MeasureScreen
import com.flowlink.app.ui.screens.SplashScreen
import com.flowlink.app.ui.screens.SubjectRegisterScreen
import com.flowlink.app.ui.screens.SubjectSelectScreen
import kotlinx.coroutines.delay

@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = "splash"
    ) {
        composable("splash") {
            LaunchedEffect(Unit) {
                delay(2000)
                navController.navigate("login") {
                    popUpTo("splash") { inclusive = true }
                }
            }
            SplashScreen()
        }

        composable("login") {
            LoginScreen(
                onNavigateToForgotPassword = {
                    navController.navigate("forgot_password")
                },
                onNavigateToSubjectSelect = {
                    navController.navigate("subject_select")
                },
            )
        }

        composable("forgot_password") {
            ForgotPasswordScreen(
                onBack = { navController.popBackStack() },
                onNavigateToLogin = {
                    navController.navigate("login") {
                        popUpTo("login") { inclusive = false }
                    }
                }
            )
        }

        composable("subject_select") {
            SubjectSelectScreen(
                onNavigateToRegister = {
                    navController.navigate("subject_register")
                },
                onNavigateToMeasure = {
                    navController.navigate("measure")
                },
            )
        }

        composable("subject_register") {
            SubjectRegisterScreen(
                onBack = { navController.popBackStack() },
                onNext = { navController.popBackStack() },
            )
        }

        composable("measure") {
            MeasureScreen(
                onClose = { navController.popBackStack() }
            )
        }
    }
}
