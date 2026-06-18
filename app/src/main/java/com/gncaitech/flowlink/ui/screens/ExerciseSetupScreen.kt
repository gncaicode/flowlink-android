package com.gncaitech.flowlink.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gncaitech.flowlink.network.PatientDto
import com.gncaitech.flowlink.ui.theme.ArtRed
import com.gncaitech.flowlink.ui.theme.MedTeal
import com.gncaitech.flowlink.ui.theme.MontserratFamily

private val SetupDarkBg    = Color(0xFF0A1422)
private val SetupGlassFill = Color(0x66000000)
private val SetupGlassHair = Color(0x29FFFFFF)
private val SetupFgDim     = Color.White.copy(alpha = 0.60f)
private val SetupFgLabel   = Color.White.copy(alpha = 0.55f)

data class ExerciseConfig(
    val totalSets: Int  = 3,
    val targetReps: Int = 15,
    val setSeconds: Int = 150,
    val kind: String    = "grip",
    val restSeconds: Int = 60
)

@Composable
fun ExerciseSetupScreen(
    patient: PatientDto? = null,
    config: ExerciseConfig = ExerciseConfig(),
    onConfigChange: (ExerciseConfig) -> Unit = {},
    onBack: () -> Unit = {},
    onStart: () -> Unit = {},
) {
    val durationOptions = listOf(60 to "1분", 90 to "1분 30초", 120 to "2분", 150 to "2분 30초", 180 to "3분")

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(SetupDarkBg)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp)
                .padding(bottom = 80.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Spacer(Modifier.height(8.dp))

            // 상단 바
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(SetupGlassFill)
                        .border(1.dp, Color.White.copy(alpha = 0.18f), CircleShape)
                        .clickable(
                            indication = null,
                            interactionSource = remember { MutableInteractionSource() }
                        ) { onBack() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "뒤로",
                        tint = Color.White, modifier = Modifier.size(20.dp))
                }
                Text(
                    "운동 설정",
                    style = TextStyle(
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = Color.White
                    )
                )
            }

            // 환자 칩
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(SetupGlassFill)
                    .border(1.dp, SetupGlassHair, RoundedCornerShape(12.dp))
                    .padding(horizontal = 14.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    patient?.name ?: "-",
                    style = TextStyle(fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = Color.White)
                )
                Text(
                    patient?.pid ?: "-",
                    style = TextStyle(fontFamily = MontserratFamily, fontSize = 10.sp, color = SetupFgLabel)
                )

//                Spacer(Modifier.weight(1f))
//                Box(
//                    modifier = Modifier
//                        .clip(RoundedCornerShape(6.dp))
//                        .background(MedTeal.copy(alpha = 0.18f))
//                        .border(1.dp, MedTeal.copy(alpha = 0.40f), RoundedCornerShape(6.dp))
//                        .padding(horizontal = 8.dp, vertical = 3.dp)
//                ) {
//                    Text(
//                        "공쥐기",
//                        style = TextStyle(
//                            fontFamily = MontserratFamily,
//                            fontWeight = FontWeight.Bold,
//                            fontSize = 11.sp,
//                            color = MedTeal
//                        )
//                    )
//                }

            }

            val kindOptions = listOf(
                "grip"              to "공쥐기",
                "dumbbell"          to "덤벨컬",
                "wrist_rotation"    to "손목회전",
            )

            //운동종류
            SetupCard(title = "운동 종류") {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ){
                    kindOptions.forEach { (key,label) ->
                        val selected = config.kind == key
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(40.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (selected) MedTeal else Color.White.copy(alpha = 0.08f))
                                .border(
                                    1.dp,
                                    if (selected) MedTeal else Color.White.copy(alpha = 0.14f),
                                    RoundedCornerShape(10.dp)
                                )
                                .clickable(
                                    indication = null,
                                    interactionSource = remember { MutableInteractionSource() }
                                ) { onConfigChange(config.copy(kind = key)) },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                label,
                                style = TextStyle(
                                    fontFamily = MontserratFamily,
                                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                                    fontSize = 12.sp,
                                    color = if (selected) Color.White else SetupFgDim
                                )
                            )
                        }
                    }
                }
            }


            // 세트 수
            SetupCard(title = "세트 수") {
                StepperRow(
                    value = config.totalSets,
                    unit = "세트",
                    min = 1,
                    max = 5,
                    onMinus = { onConfigChange(config.copy(totalSets = config.totalSets - 1)) },
                    onPlus  = { onConfigChange(config.copy(totalSets = config.totalSets + 1)) }
                )
            }

            // 목표 횟수
            SetupCard(title = "세트당 목표 횟수") {
                StepperRow(
                    value = config.targetReps,
                    unit = "회",
                    min = 5,
                    max = 30,
                    onMinus = { onConfigChange(config.copy(targetReps = config.targetReps - 5)) },
                    onPlus  = { onConfigChange(config.copy(targetReps = config.targetReps + 5)) }
                )
            }

            // 세트 시간
            SetupCard(title = "세트 시간") {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    durationOptions.chunked(3).forEach { row ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            row.forEach { (secs, label) ->
                                val selected = config.setSeconds == secs
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(40.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(if (selected) MedTeal else Color.White.copy(alpha = 0.08f))
                                        .border(
                                            1.dp,
                                            if (selected) MedTeal else Color.White.copy(alpha = 0.14f),
                                            RoundedCornerShape(10.dp)
                                        )
                                        .clickable(
                                            indication = null,
                                            interactionSource = remember { MutableInteractionSource() }
                                        ) { onConfigChange(config.copy(setSeconds = secs)) },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        label,
                                        style = TextStyle(
                                            fontFamily = MontserratFamily,
                                            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                                            fontSize = 11.sp,
                                            color = if (selected) Color.White else SetupFgDim
                                        )
                                    )
                                }
                            }
                        }
                    }
                }
            }

            SetupCard(title = "세트 간 휴식 시간") {
                val restOptions = listOf(30 to "30초", 45 to "45초", 60 to "1분", 90 to "1분 30초")

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ){
                    restOptions.forEach {(secs, label) ->
                        val selected = config.restSeconds == secs
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(40.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (selected) MedTeal else Color.White.copy(alpha = 0.08f))
                                .border(
                                    1.dp,
                                    if (selected) MedTeal else Color.White.copy(alpha = 0.14f),
                                    RoundedCornerShape(10.dp)
                                )
                                .clickable(
                                    indication = null,
                                    interactionSource = remember {
                                        MutableInteractionSource()
                                    }
                                ) { onConfigChange(config.copy(restSeconds = secs)) },
                            contentAlignment = Alignment.Center
                        ){
                            Text(
                                label,
                                style = TextStyle(
                                    fontFamily = MontserratFamily,
                                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                                    fontSize = 11.sp,
                                    color = if (selected) Color.White else SetupFgDim
                                )
                            )
                        }
                    }
                }
            }

            // 요약
            val totalSec = config.totalSets * config.setSeconds
            val m = totalSec / 60
            val s = totalSec % 60
            val timeLabel = when {
                m == 0       -> "${s}초"
                s == 0       -> "${m}분"
                else         -> "${m}분 ${s}초"
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(MedTeal.copy(alpha = 0.10f))
                    .border(1.dp, MedTeal.copy(alpha = 0.30f), RoundedCornerShape(12.dp))
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    "총 목표",
                    style = TextStyle(
                        fontFamily = MontserratFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.sp,
                        letterSpacing = (0.14f * 10f).sp,
                        color = MedTeal
                    )
                )
                Row(horizontalArrangement = Arrangement.spacedBy(20.dp)) {
                    SummaryItem("종류", kindOptions.find { it.first == config.kind }?.second ?: "공쥐기" )
                    SummaryItem("횟수", "${config.totalSets * config.targetReps}회")
                    SummaryItem("시간", timeLabel)
                    SummaryItem("세트", "${config.totalSets}세트")
                }
            }

            Spacer(Modifier.height(8.dp))
        }

        // 측정 시작 버튼 — 하단 고정
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp)
                .height(56.dp)
                .clip(RoundedCornerShape(28.dp))
                .background(ArtRed)
                .clickable(
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() }
                ) { onStart() },
            contentAlignment = Alignment.Center
        ) {
            Text(
                "측정 시작",
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
private fun SetupCard(title: String, content: @Composable () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(SetupGlassFill)
            .border(1.dp, SetupGlassHair, RoundedCornerShape(14.dp))
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(title, style = TextStyle(fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = SetupFgDim))
        content()
    }
}

@Composable
private fun StepperRow(
    value: Int,
    unit: String,
    min: Int,
    max: Int,
    onMinus: () -> Unit,
    onPlus: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(
                    if (value > min) Color.White.copy(alpha = 0.12f) else Color.White.copy(
                        alpha = 0.05f
                    )
                )
                .border(1.dp, Color.White.copy(alpha = 0.14f), CircleShape)
                .clickable(
                    enabled = value > min,
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() }
                ) { onMinus() },
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Default.Remove, contentDescription = "감소",
                tint = if (value > min) Color.White else Color.White.copy(alpha = 0.30f),
                modifier = Modifier.size(18.dp))
        }

        Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                value.toString(),
                style = TextStyle(
                    fontFamily = MontserratFamily,
                    fontWeight = FontWeight.Black,
                    fontSize = 36.sp,
                    color = Color.White
                )
            )
            Text(
                unit,
                style = TextStyle(fontSize = 14.sp, color = SetupFgDim),
                modifier = Modifier.padding(bottom = 5.dp)
            )
        }

        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(if (value < max) MedTeal.copy(alpha = 0.20f) else Color.White.copy(alpha = 0.05f))
                .border(
                    1.dp,
                    if (value < max) MedTeal.copy(alpha = 0.50f) else Color.White.copy(alpha = 0.14f),
                    CircleShape
                )
                .clickable(
                    enabled = value < max,
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() }
                ) { onPlus() },
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Default.Add, contentDescription = "증가",
                tint = if (value < max) MedTeal else Color.White.copy(alpha = 0.30f),
                modifier = Modifier.size(18.dp))
        }
    }
}

@Composable
private fun SummaryItem(label: String, value: String) {
    Column {
        Text(label, style = TextStyle(fontSize = 10.sp, color = MedTeal.copy(alpha = 0.70f)))
        Text(value, style = TextStyle(
            fontFamily = MontserratFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp,
            color = Color.White
        ))
    }
}
