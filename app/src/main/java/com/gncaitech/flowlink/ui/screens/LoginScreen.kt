package com.gncaitech.flowlink.ui.screens

import android.graphics.Paint.Align
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gncaitech.flowlink.ui.components.FLSymbol
import com.gncaitech.flowlink.ui.components.FLWordmark
import com.gncaitech.flowlink.ui.components.FilledButton
import com.gncaitech.flowlink.ui.components.FlTextField
import com.gncaitech.flowlink.ui.theme.G500
import com.gncaitech.flowlink.ui.theme.G700
import com.gncaitech.flowlink.ui.theme.MedTeal
import com.gncaitech.flowlink.ui.theme.Navy
import com.gncaitech.flowlink.ui.theme.NavyLight
import androidx.compose.ui.text.input.KeyboardType
import com.gncaitech.flowlink.ui.theme.SnowGray
import androidx.compose.foundation.border
import androidx.compose.runtime.rememberCoroutineScope
import com.gncaitech.flowlink.network.AuthTokenHolder
import com.gncaitech.flowlink.network.PatientLoginRequest
import com.gncaitech.flowlink.network.authApi
import com.gncaitech.flowlink.ui.theme.ArtRed
import android.content.Intent
import android.net.Uri
import kotlinx.coroutines.launch

@Composable
fun LoginScreen(
    onNavigateToForgotPassword: () -> Unit = {},
    onNavigateToPatientHome: () -> Unit = {},
) {
    val context = androidx.compose.ui.platform.LocalContext.current

    var userId by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var autoLogin by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(SnowGray)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp)
                .padding(bottom = 100.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(80.dp))

            FLSymbol(size = 56.dp)

            Spacer(Modifier.height(12.dp))

            FLWordmark(fontSize = 20.sp)

            Spacer(Modifier.height(72.dp))

            FlTextField(
                label = "아이디",
                value = userId,
                onValueChange = { userId = it },
                leadingIcon = Icons.Default.Person,
                keyboardType = KeyboardType.Text,
            )

            Spacer(Modifier.height(16.dp))

            FlTextField(
                label = "비밀번호",
                value = password,
                onValueChange = {password = it},
                isPassword = !passwordVisible,
                leadingIcon = Icons.Default.Lock,
                trailingIcon = if (passwordVisible)
                    Icons.Default.Visibility
                else
                    Icons.Default.VisibilityOff,
                onTrailingIconClick = { passwordVisible = !passwordVisible },
            )

            Spacer(Modifier.height(18.dp))

            // Auto login + forgot password row
            Row(
                modifier = Modifier
                        .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
            ) {
                // Auto-login checkbox
                Box(
                    modifier = Modifier
                        .size(18.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .clickable { autoLogin = !autoLogin }
                        .background(if (autoLogin) MedTeal else Color.Transparent)
                        .border(1.5.dp, if (autoLogin) MedTeal else G500, RoundedCornerShape(4.dp)),

                    contentAlignment = Alignment.Center
                ) {
                    if (autoLogin) {
                        Text(
                            "✓",
                            style = TextStyle(
                                color = Color.White,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }
                }
                Spacer(Modifier.width(8.dp))
                Text(
                    "자동 로그인",
                    modifier = Modifier.clickable { autoLogin = !autoLogin },
                    style = TextStyle(fontSize = 14.sp, color = G700)
                )
            }

            Spacer(Modifier.height(32.dp))

            FilledButton(
                text = if (isLoading) "로그인 중..." else "로그인",
                leadingIcon = if (isLoading) null else Icons.AutoMirrored.Filled.ArrowForward,
                modifier = Modifier.fillMaxWidth(),
                height = 56.dp,
                onClick = {
                    if (!isLoading) {
                        scope.launch {
                            isLoading = true
                            errorMessage = null
                            try {
                                val res = authApi.patientLogin(PatientLoginRequest(userId, password))
                                if (res.isSuccessful) {
                                    val body = res.body()
                                    AuthTokenHolder.token = body?.token
                                    val prefs = context.getSharedPreferences("fl_prefs", android.content.Context.MODE_PRIVATE)
                                    prefs.edit()
                                        .putInt("saved_patient_id", body?.patientId ?: -1)
                                        .putString("saved_patient_name", body?.name)
                                        .putString("saved_patient_pid", body?.pid)
                                        .apply()
                                    if (autoLogin) {
                                        prefs.edit()
                                            .putString("save_pid", userId)
                                            .putString("save_password", password)
                                            .putBoolean("auto_login", true)
                                            .apply()
                                    }
                                    onNavigateToPatientHome()
                                } else {
                                    errorMessage = "아이디 또는 비밀번호가 올바르지 않습니다."
                                }
                            } catch (e: Exception) {
                                errorMessage = "서버에 연결할 수 없습니다."
                            } finally {
                                isLoading = false
                            }
                        }
                    }
                },
            )

            errorMessage?.let {
                Spacer(Modifier.height(8.dp))
                Text(
                    it,
                    style = TextStyle(fontSize = 13.sp, color = ArtRed),
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }

            Spacer(Modifier.height(32.dp))

            Text(
                "비밀번호 찾기",
                style = TextStyle(
                    fontSize = 13.sp,
                    color = NavyLight,
                    fontWeight = FontWeight.Medium
                ),
                modifier = Modifier.clickable { onNavigateToForgotPassword() }
            )
        }

        // Bottom: 관리자 로그인 + copyright
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                "관리자 로그인",
                style = TextStyle(
                    fontSize = 12.sp,
                    color = NavyLight,
                    fontWeight = FontWeight.Medium,
                ),
                modifier = Modifier.clickable {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse("http://flowlink.gncaitech.com/"))
                    context.startActivity(intent)
                }
            )
        }
    }
}
