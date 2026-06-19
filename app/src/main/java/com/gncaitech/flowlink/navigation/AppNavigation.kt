package com.gncaitech.flowlink.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.gncaitech.flowlink.network.AuthTokenHolder
import com.gncaitech.flowlink.network.PatientDto
import com.gncaitech.flowlink.network.PatientLoginRequest
import com.gncaitech.flowlink.network.PatientMeDto
import com.gncaitech.flowlink.network.authApi
import com.gncaitech.flowlink.ui.screens.ForgotPasswordScreen
import com.gncaitech.flowlink.ui.screens.LoginScreen
import com.gncaitech.flowlink.ui.screens.MeasureScreen
import com.gncaitech.flowlink.ui.screens.PatientHomeScreen
import com.gncaitech.flowlink.ui.screens.SplashScreen
import com.gncaitech.flowlink.ui.screens.SubjectRegisterScreen
import com.gncaitech.flowlink.ui.screens.SubjectSelectScreen
import kotlinx.coroutines.delay
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.setValue
import com.gncaitech.flowlink.ui.screens.ExerciseConfig
import com.gncaitech.flowlink.ui.screens.ExerciseSetupScreen
import com.gncaitech.flowlink.ui.screens.GuideVideoScreen
import com.gncaitech.flowlink.ui.screens.PatientDetailScreen
import com.gncaitech.flowlink.ui.screens.ResultScreen
import com.gncaitech.flowlink.ui.screens.WithCameraPermission


@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    //NavHost 바로 위에 추가
    var selectedPatient     by remember { mutableStateOf<PatientDto?>(null) }
    var loggedInPatientId   by remember { mutableIntStateOf(-1) }
    var exerciseConfig      by remember { mutableStateOf(ExerciseConfig()) }
    var resultTotalReps     by remember { mutableIntStateOf(0) }
    var resultTotalSeconds  by remember { mutableIntStateOf(0) }

    NavHost(
        navController = navController,
        startDestination = "splash"
    ) {
        composable("splash") {
            val context = androidx.compose.ui.platform.LocalContext.current

            LaunchedEffect(Unit) {
                delay(2000)
                val prefs = context.getSharedPreferences("fl_prefs", android.content.Context.MODE_PRIVATE)
                val autoLogin    = prefs.getBoolean("auto_login", false)
                val savedPid     = prefs.getString("save_pid", null)
                val savedPassword = prefs.getString("save_password", null)

                if (autoLogin && savedPid != null && savedPassword != null) {
                    try {
                        val res = authApi.patientLogin(PatientLoginRequest(savedPid, savedPassword))
                        if (res.isSuccessful) {
                            val body = res.body()
                            AuthTokenHolder.token = body?.token
                            val patientId = body?.patientId ?: -1
                            loggedInPatientId = patientId
                            prefs.edit()
                                .putInt("saved_patient_id", patientId)
                                .putString("saved_patient_name", body?.name)
                                .putString("saved_patient_pid", body?.pid)
                                .apply()
                            navController.navigate("patient_home") {
                                popUpTo("splash") { inclusive = true }
                            }
                        } else {
                            prefs.edit().clear().apply()
                            navController.navigate("login") {
                                popUpTo("splash") { inclusive = true }
                            }
                        }
                    } catch (e: Exception) {
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
            val context = androidx.compose.ui.platform.LocalContext.current
            LoginScreen(
                onNavigateToForgotPassword = {
                    navController.navigate("forgot_password")
                },
                onNavigateToPatientHome = {
                    val prefs = context.getSharedPreferences("fl_prefs", android.content.Context.MODE_PRIVATE)
                    loggedInPatientId = prefs.getInt("saved_patient_id", -1)
                    navController.navigate("patient_home") {
                        popUpTo("login") { inclusive = true }
                    }
                },
            )
        }

        composable("patient_home") {
            val context = androidx.compose.ui.platform.LocalContext.current
            PatientHomeScreen(
                patientId = loggedInPatientId,
                onNavigateToSetup = { patientMe ->
                    selectedPatient = PatientDto(
                        id       = patientMe.id.toString(),
                        pid      = patientMe.pid,
                        name     = patientMe.name,
                        age      = patientMe.age,
                        gender   = patientMe.gender,
                        surgeryData = null,
                        program  = patientMe.program,
                        scheduled = patientMe.scheduled,
                        status   = null,
                    )
                    navController.navigate("setup")
                },
                onLogout = {
                    val prefs = context.getSharedPreferences("fl_prefs", android.content.Context.MODE_PRIVATE)
                    prefs.edit().clear().apply()
                    AuthTokenHolder.token = null
                    navController.navigate("login") {
                        popUpTo("patient_home") { inclusive = true }
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
                onNavigateToDetail = { patient ->
                    selectedPatient = patient
                    navController.navigate("detail")
                },
                onNavigateToMeasure = { patient ->
                    selectedPatient = patient
                    navController.navigate("setup")
                },
                onLogout = {
                    navController.navigate("login") {
                        popUpTo("subject_select") { inclusive = true }
                    }
                }
            )
        }

        composable("detail") {
            selectedPatient?.let { patient ->
                PatientDetailScreen(
                    patient = patient,
                    onBack = { navController.popBackStack() }
                )
            }
        }

        composable("subject_register") {
            SubjectRegisterScreen(
                onBack = { navController.popBackStack() },
                onNext = { navController.popBackStack() },
            )
        }

        composable("setup") {
            ExerciseSetupScreen(
                patient = selectedPatient,
                config = exerciseConfig,
                onConfigChange = { exerciseConfig = it },
                onBack = { navController.popBackStack() },
                onStart = { navController.navigate("measure") },
                onGuideVideo = { kind -> navController.navigate("guide/$kind") }
            )
        }

        composable("guide/{kind}") { backStackEntry ->
            val kind = backStackEntry.arguments?.getString("kind") ?: "grip"
            GuideVideoScreen(
                kind = kind,
                onBack = { navController.popBackStack() }
            )
        }

        composable("measure") {
            WithCameraPermission {
                MeasureScreen(
                    patient = selectedPatient,
                    config = exerciseConfig,
                    onClose = { navController.popBackStack() },
                    onFinish = { totalReps, totalSecs ->
                        resultTotalReps     = totalReps
                        resultTotalSeconds  = totalSecs
                        navController.navigate("result") {
                            popUpTo("measure") { inclusive = true }
                        }
                    }
                )
            }
        }

        composable("result") {
            ResultScreen(
                patient         = selectedPatient,
                totalReps       = resultTotalReps,
                repsTarget      = exerciseConfig.targetReps,
                setsCompleted   = exerciseConfig.totalSets,
                totalSets       = exerciseConfig.totalSets,
                totalSeconds    = resultTotalSeconds,
                kind            = exerciseConfig.kind,
                onBack = {
                    navController.navigate("patient_home") {
                        popUpTo("result") { inclusive = true }
                    }
                }
            )
        }

    }
}
