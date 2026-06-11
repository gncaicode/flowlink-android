package com.gncaitech.flowlink.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import com.gncaitech.flowlink.network.PatientDto
import com.gncaitech.flowlink.ui.theme.ArtRed
import com.gncaitech.flowlink.ui.theme.MedTeal
import com.gncaitech.flowlink.ui.theme.MontserratFamily
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.ui.unit.sp

private val DarkBg   = Color(0xFF0A1422)
private val GlassFill = Color(0x66000000)
private val GlassHair = Color(0x29FFFFFF)
private val FgDim    = Color.White.copy(alpha = 0.60f)
private val FgFaint  = Color.White.copy(alpha = 0.40f)
private val FgLabel  = Color.White.copy(alpha = 0.55f)

@Composable
fun ResultScreen(
    patient: PatientDto? = null,
    totalReps: Int = 0,
    repsTarget: Int = 15,
    setsCompleted: Int = 3,
    totalSets: Int = 3,
    totalSeconds: Int = 0,
    kind:String = "grip",
    onBack: () -> Unit = {},
) {
    val mmss = { s: Int -> "%02d:%02d".format(s / 60, s % 60)}
    val overallFeedback = when {
        totalReps >= repsTarget * setsCompleted -> "perfect"
        totalReps >= (repsTarget * setsCompleted) / 2 -> "minor"
        else -> "major"
    }
    val feedbackLabel = when (overallFeedback) {
        "perfect"   -> "완벽해요"
        "minor"     -> "양호해요"
        else        -> "더 노력해요"
    }
    val feedbackColor = when (overallFeedback) {
        "perfect"   -> MedTeal
        "minor"     -> Color(0xFFF6AD55)
        else        -> ArtRed
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBg)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        // 스크롤 가능한 콘텐츠 영역 (버튼 높이만큼 하단 여백 확보)
        Column (
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp)
                .padding(bottom = 88.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Spacer(Modifier.height(24.dp))

            //환자 칩
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(GlassFill)
                    .border(1.dp, GlassHair, RoundedCornerShape(12.dp))
                    .padding(horizontal = 14.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ){
                Text(
                    patient?.name ?: "-",
                    style = TextStyle(
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 13.sp,
                        color = Color.White
                    )
                )
                Text(
                    patient?.pid ?: "-",
                    style = TextStyle(
                        fontFamily = MontserratFamily,
                        fontSize = 10.sp,
                        color = FgLabel
                    )
                )
            }

            Spacer(Modifier.height(8.dp))

            //완료 아이콘
            Box(
                modifier = Modifier
                    .size(96.dp)
                    .clip(CircleShape)
                    .background(MedTeal.copy(alpha = 0.18f))
                    .border(2.dp, MedTeal.copy(alpha = 0.60f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    tint = MedTeal,
                    modifier = Modifier.size(52.dp)
                )
            }

            //타이틀
            Column (horizontalAlignment = Alignment.CenterHorizontally){
                Text(
                    "세션 완료",
                    style = TextStyle(
                        fontWeight = FontWeight.Bold,
                        fontSize = 26.sp,
                        color = Color.White
                    )
                )
                Spacer(Modifier.height(4.dp))
                //피드백 뱃지
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(999.dp))
                        .background(feedbackColor.copy(alpha = 0.18f))
                        .border(1.dp, feedbackColor.copy(alpha = 0.50f), RoundedCornerShape(999.dp))
                        .padding(horizontal = 14.dp, vertical = 5.dp)
                ) {
                    Text(
                        feedbackLabel,
                        style = TextStyle(
                            fontFamily = MontserratFamily,
                            fontWeight =  FontWeight.Bold,
                            fontSize = 12.sp,
                            color = feedbackColor
                        )
                    )
                }
            }

            //통계 카드
            Column (
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(GlassFill)
                    .border(1.dp, GlassHair, RoundedCornerShape(16.dp))
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                val kindLabel = when (kind) {
                    "dumbbell"          -> "덤벨컬"
                    "wrist_rotation"    -> "손목회전"
                    else                -> "공쥐기"
                }
                ResultStatRow(label = "운동 종류", value = kindLabel, unit = "")
                ResultDivider()
                ResultStatRow(label = "총 횟수",    value = "$totalReps", unit = "회")
                ResultDivider()
                ResultStatRow(label = "목표 횟수",  value = "${repsTarget * setsCompleted}", unit = "회")
                ResultDivider()
                ResultStatRow(label = "완료 세트",  value = "$setsCompleted", unit = "/ $totalSets 세트")
                ResultDivider()
                ResultStatRow(label = "총 시간",    value = mmss(totalSeconds), unit = "")
            }
        }

        // 돌아가기 버튼 — 하단 고정
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 16.dp)
                .height(56.dp)
                .clip(RoundedCornerShape(28.dp))
                .background(MedTeal)
                .clickableNoRipple { onBack() },
            contentAlignment = Alignment.Center
        ) {
            Text(
                "대상자 목록으로",
                style = TextStyle(
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 16.sp,
                    color = Color.White
                )
            )
        }
    }
}

@Composable
private fun ResultStatRow(label: String, value: String, unit: String) {
    Row (
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            label,
            style = TextStyle(fontSize = 14.sp, color = FgDim)
        )
        Row (
            verticalAlignment = Alignment.Bottom,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ){
            Text(
                value,
                style = TextStyle(
                    fontFamily = MontserratFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 22.sp,
                    color = Color.White
                )
            )
            if (unit.isNotEmpty()) {
                Text(
                    unit,
                    style = TextStyle(
                        fontSize = 13.sp,
                        color = FgFaint
                    ),
                    modifier = Modifier.padding(bottom = 3.dp)
                )
            }
        }
    }
}

@Composable
private fun ResultDivider() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(1.dp)
            .background(Color.White.copy(alpha = 0.08f))
    )
}

@Composable
private fun Modifier.clickableNoRipple(onClick: () -> Unit): Modifier =
    this.clickable(
        indication = null,
        interactionSource = remember { MutableInteractionSource() },
        onClick = onClick
    )
