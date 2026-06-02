package com.gncaitech.flowlink.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.gncaitech.flowlink.network.LoginRequest
import com.gncaitech.flowlink.network.PatientDto
import com.gncaitech.flowlink.network.authApi
import com.gncaitech.flowlink.ui.screens.ForgotPasswordScreen
import com.gncaitech.flowlink.ui.screens.LoginScreen
import com.gncaitech.flowlink.ui.screens.MeasureScreen
import com.gncaitech.flowlink.ui.screens.SplashScreen
import com.gncaitech.flowlink.ui.screens.SubjectRegisterScreen
import com.gncaitech.flowlink.ui.screens.SubjectSelectScreen
import kotlinx.coroutines.delay
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue


@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    //NavHost 바로 위에 추가
    var selectedPatient by remember { mutableStateOf<PatientDto?>(null) }

    NavHost(
        navController = navController,
        startDestination = "splash"
    ) {
        composable("splash") {
            val context = androidx.compose.ui.platform.LocalContext.current

            LaunchedEffect(Unit) {
                delay(2000)
                val prefs = context.getSharedPreferences("fl_prefs",android.content.Context.MODE_PRIVATE)
                val autoLogin = prefs.getBoolean("auto_login", false)
                val savedEmail = prefs.getString("save_email", null)
                val savedPassword = prefs.getString("save_password", null)

                if (autoLogin && savedEmail != null && savedPassword != null) {
                    try{
                        val res = authApi.login(LoginRequest(savedEmail,savedPassword))
                        if(res.isSuccessful) {
                            navController.navigate("subject_select") {
                                popUpTo("splash") { inclusive = true }
                            }
                        } else {
                            // 로그인 실패 (비밀번호 변경 등 ) -> 저장 정보 삭제 후 로그인으로
                            prefs.edit().clear().apply()
                            navController.navigate("login") {
                                popUpTo("splash") {inclusive = true}
                            }
                        }
                    } catch (e: Exception) {
                        //서버 오류
                        navController.navigate("login") {
                            popUpTo("splash") { inclusive = true }
                        }
                    }
                } else {
                    navController.navigate("login") {
                        popUpTo("splash") { inclusive = true }
                    }
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
                    navController.navigate("subject_select"){
                        popUpTo("login") { inclusive = true }
                    }
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
                onNavigateToMeasure = { patient ->
                    selectedPatient = patient
                    navController.navigate("measure")
                },
                onLogout = {
                    navController.navigate("login") {
                        popUpTo("subject_select") { inclusive = true }
                    }
                }
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
                patient = selectedPatient,
                onClose = { navController.popBackStack() }
            )
        }
    }
}
