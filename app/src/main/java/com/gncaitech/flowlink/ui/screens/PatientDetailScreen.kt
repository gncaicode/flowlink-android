package com.gncaitech.flowlink.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gncaitech.flowlink.network.PatientDto
import com.gncaitech.flowlink.network.SessionDto
import com.gncaitech.flowlink.network.patientApi
import com.gncaitech.flowlink.ui.theme.ArtRed
import com.gncaitech.flowlink.ui.theme.G200
import com.gncaitech.flowlink.ui.theme.G500
import com.gncaitech.flowlink.ui.theme.MedTeal
import com.gncaitech.flowlink.ui.theme.MontserratFamily
import com.gncaitech.flowlink.ui.theme.Navy
import com.gncaitech.flowlink.ui.theme.NavyFaint

@Composable
fun PatientDetailScreen(
    patient: PatientDto,
    onBack: () -> Unit = {},
    onStartMeasure: () -> Unit = {},
) {
    var sessions by remember { mutableStateOf<List<SessionDto>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(patient.id) {
        try {
            val res = patientApi.getSessions(patient.id)
            if (res.isSuccessful) sessions = res.body() ?: emptyList()
        } catch (_: Exception) {
        } finally {
            isLoading = false
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF7FAFC))
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        // 상단 네이비 헤더
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Navy)
                .padding(horizontal = 16.dp)
                .padding(top = 8.dp, bottom = 20.dp)
        ) {
            // 뒤로가기 + 제목
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.padding(vertical = 8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.12f))
                        .clickable(
                            indication = null,
                            interactionSource = remember { MutableInteractionSource() }
                        ) { onBack() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "뒤로",
                        tint = Color.White, modifier = Modifier.size(18.dp))
                }
                Text(
                    "대상자 상세",
                    style = TextStyle(fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color.White)
                )
            }

            // 환자 정보 카드
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color.White.copy(alpha = 0.10f))
                    .border(1.dp, Color.White.copy(alpha = 0.18f), RoundedCornerShape(14.dp))
                    .padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // 아바타
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(NavyFaint),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            patient.name.take(1),
                            style = TextStyle(
                                fontFamily = MontserratFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp,
                                color = Navy
                            )
                        )
                    }
                    Column {
                        Text(
                            patient.name,
                            style = TextStyle(fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.White)
                        )
                        Text(
                            patient.pid,
                            style = TextStyle(
                                fontFamily = MontserratFamily, fontSize = 11.sp,
                                color = Color.White.copy(alpha = 0.60f)
                            )
                        )
                    }
                    Spacer(Modifier.weight(1f))
                    // 상태 뱃지
                    val statusColor = if (patient.status == "active") MedTeal else G500
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(statusColor.copy(alpha = 0.20f))
                            .border(1.dp, statusColor.copy(alpha = 0.50f), RoundedCornerShape(6.dp))
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(
                            patient.status ?: "ready",
                            style = TextStyle(
                                fontFamily = MontserratFamily, fontWeight = FontWeight.Bold,
                                fontSize = 10.sp, color = statusColor
                            )
                        )
                    }
                }

                // 상세 정보 행
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    InfoItem("나이", "${patient.age ?: "-"}세")
                    InfoItem("성별", patient.gender ?: "-")
                    InfoItem("프로그램", patient.program ?: "-")
                }
            }
        }

        // 세션 이력 리스트
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
        ) {
            Spacer(Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "세션 이력",
                    style = TextStyle(fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Navy)
                )
                Spacer(Modifier.width(8.dp))
                if (!isLoading) {
                    Box(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(Navy.copy(alpha = 0.10f))
                            .padding(horizontal = 7.dp, vertical = 2.dp)
                    ) {
                        Text(
                            "${sessions.size}",
                            style = TextStyle(
                                fontFamily = MontserratFamily, fontWeight = FontWeight.Bold,
                                fontSize = 11.sp, color = Navy
                            )
                        )
                    }
                }
            }

            Spacer(Modifier.height(10.dp))

            if (isLoading) {
                Box(Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = MedTeal)
                }
            } else if (sessions.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.White)
                        .border(1.dp, G200, RoundedCornerShape(12.dp))
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "아직 세션 기록이 없습니다",
                        style = TextStyle(fontSize = 14.sp, color = G500)
                    )
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    sessions.sortedByDescending { it.date }.forEach { session ->
                        SessionHistoryCard(session)
                    }
                }
            }

            Spacer(Modifier.height(16.dp))
        }

        // 하단 측정 시작 버튼
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White)
                .border(1.dp, G200, RoundedCornerShape(0.dp))
                .padding(horizontal = 16.dp, vertical = 12.dp),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .clip(RoundedCornerShape(26.dp))
                    .background(ArtRed)
                    .clickable(
                        indication = null,
                        interactionSource = remember { MutableInteractionSource() }
                    ) { onStartMeasure() },
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        "측정 시작",
                        style = TextStyle(fontWeight = FontWeight.SemiBold, fontSize = 16.sp, color = Color.White)
                    )
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null,
                        tint = Color.White, modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun SessionHistoryCard(session: SessionDto) {
    val feedbackColor = when (session.feedback) {
        "perfect" -> MedTeal
        "minor"   -> Color(0xFFF6AD55)
        else      -> ArtRed
    }
    val feedbackLabel = when (session.feedback) {
        "perfect" -> "완벽"
        "minor"   -> "양호"
        else      -> "미흡"
    }
    val mmss = { s: Int -> "%02d:%02d".format(s / 60, s % 60) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color.White)
            .border(1.dp, G200, RoundedCornerShape(12.dp))
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // 날짜
        Column(modifier = Modifier.width(72.dp)) {
            Text(
                session.date.take(10),
                style = TextStyle(
                    fontFamily = MontserratFamily, fontWeight = FontWeight.Bold,
                    fontSize = 11.sp, color = Navy
                )
            )
            Text(
                session.kind,
                style = TextStyle(fontSize = 11.sp, color = G500)
            )
        }

        // 횟수 / 목표
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                Text(
                    "${session.repsCompleted}",
                    style = TextStyle(
                        fontFamily = MontserratFamily, fontWeight = FontWeight.Bold,
                        fontSize = 20.sp, color = Navy
                    )
                )
                Text(
                    "/ ${session.repsTarget}회",
                    style = TextStyle(fontSize = 12.sp, color = G500),
                    modifier = Modifier.padding(bottom = 2.dp)
                )
            }
            Text(
                mmss(session.durationSec),
                style = TextStyle(fontFamily = MontserratFamily, fontSize = 11.sp, color = G500)
            )
        }

        // 피드백 뱃지
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .background(feedbackColor.copy(alpha = 0.12f))
                .border(1.dp, feedbackColor.copy(alpha = 0.40f), RoundedCornerShape(8.dp))
                .padding(horizontal = 10.dp, vertical = 5.dp)
        ) {
            Text(
                feedbackLabel,
                style = TextStyle(
                    fontFamily = MontserratFamily, fontWeight = FontWeight.Bold,
                    fontSize = 12.sp, color = feedbackColor
                )
            )
        }
    }
}

@Composable
private fun InfoItem(label: String, value: String) {
    Column {
        Text(label, style = TextStyle(fontSize = 10.sp, color = Color.White.copy(alpha = 0.55f)))
        Text(value, style = TextStyle(
            fontFamily = MontserratFamily, fontWeight = FontWeight.SemiBold,
            fontSize = 12.sp, color = Color.White
        ))
    }
}
