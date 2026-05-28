package com.gncaitech.flowlink.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import com.gncaitech.flowlink.ui.components.AppBar
import com.gncaitech.flowlink.ui.components.FLCaption
import com.gncaitech.flowlink.ui.components.FilledButton
import com.gncaitech.flowlink.ui.components.FlTextField
import com.gncaitech.flowlink.ui.components.FormCard
import com.gncaitech.flowlink.ui.components.OutlinedActionButton
import com.gncaitech.flowlink.ui.theme.G200
import com.gncaitech.flowlink.ui.theme.G500
import com.gncaitech.flowlink.ui.theme.MedTeal
import com.gncaitech.flowlink.ui.theme.MontserratFamily
import com.gncaitech.flowlink.ui.theme.Navy
import com.gncaitech.flowlink.ui.theme.SnowGray
import com.gncaitech.flowlink.ui.theme.TealLight

@Composable
fun SubjectRegisterScreen(
    onBack: () -> Unit = {},
    onNext: () -> Unit = {},
) {
    var selectedHistoryOption by remember { mutableIntStateOf(0) }
    var selectedHand by remember { mutableStateOf("right") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SnowGray)
            .statusBarsPadding()
    ) {
        // AppBar
        AppBar(
            title = "대상자 등록",
            subtitle = "STEP 2 / 3 · AVF 정보",
            onBack = onBack,
            trailingContent = {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(TealLight)
                        .padding(horizontal = 10.dp, vertical = 5.dp)
                ) {
                    Text(
                        "2/3",
                        style = TextStyle(
                            fontFamily = MontserratFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = MedTeal,
                        )
                    )
                }
            }
        )

        // Stepper
        RegisterStepperBar(currentStep = 1)

        // Scrollable content
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Vascular access card
            FormCard(
                caption = "VASCULAR ACCESS",
                title = "AVF 수술 정보",
            ) {
                FlTextField(
                    label = "수술일",
                    value = "2026.05.04",
                    leadingIcon = Icons.Default.CalendarMonth,
                )
                Spacer(Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    FlTextField(
                        label = "수술 부위",
                        value = "좌측 요골동맥",
                        modifier = Modifier.weight(1f),
                    )
                    FlTextField(
                        label = "문합 유형",
                        value = "단단 문합",
                        modifier = Modifier.weight(1f),
                    )
                }
                Spacer(Modifier.height(12.dp))
                FlTextField(
                    label = "담당 외과의",
                    value = "김민준 교수 (혈관외과)",
                    leadingIcon = Icons.Default.Person,
                )
                Spacer(Modifier.height(12.dp))
                FlTextField(
                    label = "합병증 여부",
                    value = "없음",
                    supportingText = "해당사항이 있으면 모두 기록해주세요",
                )
            }

            // Baseline metrics card
            FormCard(
                caption = "BASELINE METRICS",
                title = "기저 상태",
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    FlTextField(
                        label = "혈관 직경",
                        value = "3.2",
                        trailingLabel = "mm",
                        modifier = Modifier.weight(1f),
                    )
                    FlTextField(
                        label = "혈류량",
                        value = "412",
                        trailingLabel = "mL/min",
                        modifier = Modifier.weight(1f),
                    )
                }

                Spacer(Modifier.height(16.dp))

                // Previous AVF surgery history
                FLCaption("이전 AVF 수술 이력", color = G500)
                Spacer(Modifier.height(8.dp))

                val historyOptions = listOf("없음", "1회", "2회 이상")
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .border(1.dp, G200, RoundedCornerShape(12.dp)),
                ) {
                    historyOptions.forEachIndexed { index, option ->
                        val isSelected = index == selectedHistoryOption
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(40.dp)
                                .background(if (isSelected) Navy else Color.White)
                                .clickable { selectedHistoryOption = index }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                option,
                                style = TextStyle(
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    fontSize = 13.sp,
                                    color = if (isSelected) Color.White else G500,
                                )
                            )
                        }
                        if (index < historyOptions.size - 1) {
                            Box(
                                modifier = Modifier
                                    .width(1.dp)
                                    .height(40.dp)
                                    .background(G200)
                            )
                        }
                    }
                }

                Spacer(Modifier.height(16.dp))

                // Dominant hand selection
                FLCaption("운동 손 (Dominant Hand)", color = G500)
                Spacer(Modifier.height(8.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    listOf("left" to "왼손", "right" to "오른손").forEach { (key, label) ->
                        val isSelected = selectedHand == key
                        Row(
                            modifier = Modifier.clickable { selectedHand = key },
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(20.dp)
                                    .clip(CircleShape)
                                    .background(if (isSelected) Navy else Color.White)
                                    .border(2.dp, if (isSelected) Navy else G200, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                if (isSelected) {
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .clip(CircleShape)
                                            .background(Color.White)
                                    )
                                }
                            }
                            Spacer(Modifier.width(8.dp))
                            Text(
                                label,
                                style = TextStyle(
                                    fontSize = 14.sp,
                                    color = if (isSelected) Navy else G500,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(16.dp))
        }

        // Bottom CTA
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White)
                .border(1.dp, G200, RoundedCornerShape(0.dp))
                .navigationBarsPadding()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedActionButton(
                text = "이전",
                leadingIcon = Icons.AutoMirrored.Filled.ArrowBack,
                modifier = Modifier.fillMaxWidth(0.35f),
                height = 52.dp,
                onClick = onBack,
            )
            FilledButton(
                text = "다음: 운동 처방",
                leadingIcon = Icons.AutoMirrored.Filled.ArrowForward,
                modifier = Modifier.weight(1f),
                height = 52.dp,
                onClick = onNext,
            )
        }
    }
}

@Composable
private fun RegisterStepperBar(currentStep: Int) {
    val steps = listOf("기본 정보", "AVF 정보", "운동 처방")
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
                    val displayName = if (index < currentStep) "✓ $stepName" else stepName
                    Text(
                        displayName,
                        style = TextStyle(
                            fontFamily = MontserratFamily,
                            fontWeight = if (index == currentStep) FontWeight.Bold else FontWeight.Normal,
                            fontSize = 10.sp,
                            color = if (index <= currentStep) Navy else G500,
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
