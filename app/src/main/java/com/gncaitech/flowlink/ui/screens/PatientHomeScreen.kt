package com.gncaitech.flowlink.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import com.gncaitech.flowlink.network.PatientMeDto
import com.gncaitech.flowlink.network.SessionDto
import com.gncaitech.flowlink.network.patientApi
import com.gncaitech.flowlink.ui.components.FilledButton
import com.gncaitech.flowlink.ui.components.FLWordmark
import com.gncaitech.flowlink.ui.theme.G200
import com.gncaitech.flowlink.ui.theme.G500
import com.gncaitech.flowlink.ui.theme.MedTeal
import com.gncaitech.flowlink.ui.theme.Navy
import com.gncaitech.flowlink.ui.theme.SnowGray
import androidx.compose.foundation.clickable
import androidx.compose.ui.platform.LocalContext

@Composable
fun PatientHomeScreen(
    patientId: Int,
    onNavigateToSetup: (PatientMeDto) -> Unit,
    onLogout: () -> Unit,
) {
    val context = LocalContext.current
    val versionName = remember {
        context.packageManager.getPackageInfo(context.packageName, 0).versionName
    }
    var patient by remember { mutableStateOf<PatientMeDto?>(null) }
    var sessions by remember { mutableStateOf<List<SessionDto>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        try {
            val meRes = patientApi.getMe()
            if (meRes.isSuccessful) patient = meRes.body()

            val sessRes = patientApi.getSessions(patientId.toString(), limit = 20)
            if (sessRes.isSuccessful) sessions = sessRes.body()?.data ?: emptyList()
        } catch (_: Exception) {
        } finally {
            isLoading = false
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(SnowGray)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // 상단 헤더
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 20.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    FLWordmark(fontSize = 16.sp)
                    Text("v$versionName", style = TextStyle(fontSize = 11.sp, color = G500))
                }
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Logout,
                    contentDescription = "로그아웃",
                    tint = G500,
                    modifier = Modifier
                        .size(22.dp)
                        .clickable { onLogout() }
                )
            }

            if (isLoading) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = MedTeal)
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 24.dp),
                ) {
                    // 환자 정보 카드
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp))
                                .background(Navy)
                                .padding(20.dp)
                        ) {
                            Column {
                                Text(
                                    patient?.name ?: "-",
                                    style = TextStyle(
                                        color = Color.White,
                                        fontSize = 22.sp,
                                        fontWeight = FontWeight.Bold,
                                    )
                                )
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    "환자번호: ${patient?.pid ?: "-"}",
                                    style = TextStyle(color = G200, fontSize = 13.sp)
                                )
                                patient?.program?.let {
                                    Spacer(Modifier.height(2.dp))
                                    Text(
                                        "프로그램: $it",
                                        style = TextStyle(color = G200, fontSize = 13.sp)
                                    )
                                }
                                patient?.scheduled?.let {
                                    Spacer(Modifier.height(2.dp))
                                    Text(
                                        "다음 예약: $it",
                                        style = TextStyle(color = G200, fontSize = 13.sp)
                                    )
                                }
                            }
                        }
                        Spacer(Modifier.height(16.dp))
                    }

                    // 운동 시작 버튼
                    item {
                        FilledButton(
                            text = "운동 시작",
                            leadingIcon = Icons.AutoMirrored.Filled.ArrowForward,
                            modifier = Modifier.fillMaxWidth(),
                            height = 52.dp,
                            onClick = { patient?.let { onNavigateToSetup(it) } },
                        )
                        Spacer(Modifier.height(24.dp))
                    }

                    // 운동 기록 헤더
                    item {
                        Text(
                            "운동 기록",
                            style = TextStyle(
                                color = Navy,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.SemiBold,
                            )
                        )
                        Spacer(Modifier.height(10.dp))
                    }

                    if (sessions.isEmpty()) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 32.dp),
                                contentAlignment = Alignment.Center,
                            ) {
                                Text(
                                    "아직 운동 기록이 없습니다.",
                                    style = TextStyle(color = G500, fontSize = 14.sp)
                                )
                            }
                        }
                    } else {
                        items(sessions) { session ->
                            SessionRow(session)
                            Spacer(Modifier.height(8.dp))
                        }
                    }

                    item { Spacer(Modifier.height(24.dp)) }
                }
            }
        }
    }
}

@Composable
private fun SessionRow(session: SessionDto) {
    val kindLabel = when (session.kind) {
        "grip"          -> "공쥐기"
        "dumbbell"      -> "덤벨컬"
        "wrist_rotation"-> "손목회전"
        else            -> session.kind
    }
    val feedbackColor = when (session.feedback) {
        "perfect" -> MedTeal
        "minor"   -> Color(0xFFFFA000)
        else      -> Color(0xFFE53935)
    }
    val feedbackLabel = when (session.feedback) {
        "perfect" -> "완벽"
        "minor"   -> "보통"
        else      -> "미흡"
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color.White)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Column {
            Text(
                "${session.date}  $kindLabel",
                style = TextStyle(color = Navy, fontSize = 14.sp, fontWeight = FontWeight.Medium)
            )
            Spacer(Modifier.height(2.dp))
            Text(
                "${session.repsCompleted}회 / ${session.repsTarget}회 목표",
                style = TextStyle(color = G500, fontSize = 12.sp)
            )
        }
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(6.dp))
                .background(feedbackColor.copy(alpha = 0.12f))
                .padding(horizontal = 10.dp, vertical = 4.dp)
        ) {
            Text(
                feedbackLabel,
                style = TextStyle(color = feedbackColor, fontSize = 12.sp, fontWeight = FontWeight.Medium)
            )
        }
    }
}
