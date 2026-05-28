package com.gncaitech.flowlink.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gncaitech.flowlink.ui.components.AppBar
import com.gncaitech.flowlink.ui.components.FLCaption
import com.gncaitech.flowlink.ui.components.FilledButton
import com.gncaitech.flowlink.ui.components.FlTextField
import com.gncaitech.flowlink.ui.theme.G200
import com.gncaitech.flowlink.ui.theme.G500
import com.gncaitech.flowlink.ui.theme.G700
import com.gncaitech.flowlink.ui.theme.MedTeal
import com.gncaitech.flowlink.ui.theme.MontserratFamily
import com.gncaitech.flowlink.ui.theme.Navy
import com.gncaitech.flowlink.ui.theme.NavyFaint
import com.gncaitech.flowlink.ui.theme.NavyLight
import com.gncaitech.flowlink.ui.theme.SnowGray

@Composable
fun ForgotPasswordScreen(
    onBack: () -> Unit = {},
    onNavigateToLogin: () -> Unit = {},
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SnowGray)
    ) {
        // AppBar
        AppBar(
            title = "비밀번호 찾기",
            onBack = onBack,
        )

        // Stepper
        StepperBar(currentStep = 0)

        // Scrollable content
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 24.dp)
        ) {
            FLCaption("STEP 1 · IDENTITY")

            Spacer(Modifier.height(8.dp))

            Text(
                "가입하신 아이디를 입력해주세요",
                style = TextStyle(
                    fontFamily = MontserratFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 24.sp,
                    color = Navy
                )
            )

            Spacer(Modifier.height(8.dp))

            Text(
                "입력하신 아이디로 등록된 휴대폰 번호에 인증번호를 전송해드립니다.",
                style = TextStyle(
                    fontSize = 13.sp,
                    color = G500,
                    lineHeight = (13f * 1.55f).sp,
                )
            )

            Spacer(Modifier.height(32.dp))

            FlTextField(
                label = "아이디",
                value = "park.minjun@flowlink.kr",
                focused = true,
                leadingIcon = Icons.Default.Person,
                supportingText = "가입 시 등록한 이메일 또는 아이디",
            )

            Spacer(Modifier.height(18.dp))

            FlTextField(
                label = "휴대폰 번호 (선택)",
                value = "010-1234-5678",
                leadingIcon = Icons.Default.Phone,
                supportingText = "아이디 인증에 실패한 경우에만 사용됩니다",
            )

            Spacer(Modifier.height(28.dp))

            FilledButton(
                text = "인증번호 받기",
                leadingIcon = Icons.AutoMirrored.Filled.ArrowForward,
                modifier = Modifier.fillMaxWidth(),
                height = 56.dp,
            )

            Spacer(Modifier.height(14.dp))

            // Help card
            HelpCard()

            Spacer(Modifier.height(32.dp))
        }

        // Bottom bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White)
                .border(1.dp, G200, RoundedCornerShape(topStart = 0.dp, topEnd = 0.dp))
                .padding(horizontal = 20.dp, vertical = 20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "비밀번호가 기억나셨나요?",
                style = TextStyle(fontSize = 13.sp, color = G500)
            )
            Spacer(Modifier.width(8.dp))
            Text(
                "로그인으로 돌아가기",
                style = TextStyle(
                    fontSize = 13.sp,
                    color = NavyLight,
                    fontWeight = FontWeight.Bold
                ),
                modifier = Modifier.clickable { onNavigateToLogin() }
            )
        }
    }
}

@Composable
private fun StepperBar(currentStep: Int) {
    val steps = listOf("아이디 확인", "본인 인증", "새 비밀번호")
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .border(1.dp, G200, RoundedCornerShape(0.dp))
            .padding(horizontal = 20.dp, vertical = 12.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth()) {
            steps.forEachIndexed { index, stepName ->
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(4.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(if (index <= currentStep) MedTeal else G200)
                    )
                    Spacer(Modifier.height(6.dp))
                    Text(
                        stepName,
                        style = TextStyle(
                            fontFamily = MontserratFamily,
                            fontWeight = if (index == currentStep) FontWeight.Bold else FontWeight.Normal,
                            fontSize = 10.sp,
                            color = if (index == currentStep) Navy else G500,
                        )
                    )
                }
                if (index < steps.size - 1) {
                    Spacer(Modifier.width(8.dp))
                }
            }
        }
    }
}

@Composable
private fun HelpCard() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(NavyFaint)
            .border(1.dp, G200, RoundedCornerShape(14.dp))
            .padding(16.dp),
        verticalAlignment = Alignment.Top
    ) {
        // Icon circle
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .background(Color.White),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.MedicalServices,
                contentDescription = null,
                tint = Navy,
                modifier = Modifier.size(18.dp)
            )
        }

        Spacer(Modifier.width(12.dp))

        Column {
            Text(
                "아이디가 기억나지 않으세요?",
                style = TextStyle(
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = Navy
                )
            )
            Spacer(Modifier.height(4.dp))
            Text(
                "병원 의료진에게 문의하시면 빠르게 도움을 받으실 수 있습니다.",
                style = TextStyle(
                    fontSize = 12.sp,
                    color = G700,
                    lineHeight = (12f * 1.5f).sp
                )
            )
            Spacer(Modifier.height(8.dp))
            Text(
                buildAnnotatedString {
                    withStyle(SpanStyle(color = NavyLight, fontWeight = FontWeight.Bold)) {
                        append("FlowLink 고객센터")
                    }
                    append("  ")
                    withStyle(SpanStyle(color = Navy, fontWeight = FontWeight.Bold)) {
                        append("1670-0000")
                    }
                },
                style = TextStyle(fontSize = 12.sp)
            )
        }
    }
}
