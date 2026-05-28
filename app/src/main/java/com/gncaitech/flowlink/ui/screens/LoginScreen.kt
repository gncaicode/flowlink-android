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
import com.gncaitech.flowlink.ui.theme.MontserratFamily
import com.gncaitech.flowlink.ui.theme.Navy
import com.gncaitech.flowlink.ui.theme.NavyLight
import androidx.compose.ui.text.input.KeyboardType
import com.gncaitech.flowlink.ui.theme.SnowGray
import androidx.compose.foundation.border

@Composable
fun LoginScreen(
    onNavigateToForgotPassword: () -> Unit = {},
    onNavigateToSubjectSelect: () -> Unit = {},
) {
    var userId by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var autoLogin by remember {
        mutableStateOf(false)
    }

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
                keyboardType = KeyboardType.Email,
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
                        .fillMaxWidth()
                        .clickable { autoLogin = !autoLogin },
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
            ) {
                // Auto-login checkbox
                Box(
                    modifier = Modifier
                        .size(18.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(if(autoLogin) MedTeal else Color.Transparent)
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
                    style = TextStyle(fontSize = 14.sp, color = G700)
                )
            }

            Spacer(Modifier.height(32.dp))

            FilledButton(
                text = "로그인",
                leadingIcon = Icons.AutoMirrored.Filled.ArrowForward,
                modifier = Modifier.fillMaxWidth(),
                height = 56.dp,
                onClick = onNavigateToSubjectSelect,
            )

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

        // Bottom security badge
        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 36.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Lock,
                contentDescription = null,
                tint = G500,
                modifier = Modifier.size(14.dp)
            )
            Spacer(Modifier.width(6.dp))
            Text(
                "HIPAA · ISO 27001 보안 인증",
                style = TextStyle(
                    fontFamily = MontserratFamily,
                    fontWeight = FontWeight.Medium,
                    fontSize = 11.sp,
                    color = G500,
                    letterSpacing = 0.04.sp,
                )
            )
        }
    }
}
