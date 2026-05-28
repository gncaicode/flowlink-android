package com.flowlink.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.flowlink.app.ui.components.FLSymbol
import com.flowlink.app.ui.components.FLWordmark
import com.flowlink.app.ui.components.FilledButton
import com.flowlink.app.ui.components.FlTextField
import com.flowlink.app.ui.theme.G500
import com.flowlink.app.ui.theme.G700
import com.flowlink.app.ui.theme.MedTeal
import com.flowlink.app.ui.theme.MontserratFamily
import com.flowlink.app.ui.theme.Navy
import com.flowlink.app.ui.theme.NavyLight
import com.flowlink.app.ui.theme.SnowGray

@Composable
fun LoginScreen(
    onNavigateToForgotPassword: () -> Unit = {},
    onNavigateToSubjectSelect: () -> Unit = {},
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(SnowGray)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp)
                .padding(bottom = 100.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(32.dp))

            FLSymbol(size = 56.dp)

            Spacer(Modifier.height(12.dp))

            FLWordmark(fontSize = 20.sp)

            Spacer(Modifier.height(32.dp))

            Text(
                text = "환영합니다",
                style = TextStyle(
                    fontFamily = MontserratFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 26.sp,
                    letterSpacing = (-0.01f * 26f).sp,
                    color = Navy
                ),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(6.dp))

            Text(
                text = "로그인 후 오늘 운동을 시작하세요.",
                style = TextStyle(
                    fontSize = 13.sp,
                    color = G500,
                    fontWeight = FontWeight.Normal
                ),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(32.dp))

            FlTextField(
                label = "아이디",
                value = "park.minjun@flowlink.kr",
                leadingIcon = Icons.Default.Person,
            )

            Spacer(Modifier.height(16.dp))

            FlTextField(
                label = "비밀번호",
                value = "password",
                isPassword = true,
                focused = true,
                leadingIcon = Icons.Default.Lock,
                trailingIcon = Icons.Default.Visibility,
            )

            Spacer(Modifier.height(18.dp))

            // Auto login + forgot password row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Auto-login checkbox
                Box(
                    modifier = Modifier
                        .size(18.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(MedTeal),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "✓",
                        style = TextStyle(
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
                Spacer(Modifier.width(8.dp))
                Text(
                    "자동 로그인",
                    style = TextStyle(fontSize = 14.sp, color = G700)
                )
                Spacer(Modifier.weight(1f))
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

            Spacer(Modifier.height(32.dp))

            FilledButton(
                text = "로그인",
                leadingIcon = androidx.compose.material.icons.Icons.AutoMirrored.Filled.ArrowForward,
                modifier = Modifier.fillMaxWidth(),
                height = 56.dp,
                onClick = onNavigateToSubjectSelect,
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
