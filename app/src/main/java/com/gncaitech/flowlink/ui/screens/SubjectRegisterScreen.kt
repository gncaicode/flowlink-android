package com.gncaitech.flowlink.ui.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.FrontHand
import androidx.compose.material.icons.filled.Loop
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Tag
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gncaitech.flowlink.ui.components.AppBar
import com.gncaitech.flowlink.ui.components.FilledButton
import com.gncaitech.flowlink.ui.components.FlTextField
import com.gncaitech.flowlink.ui.components.FormCard
import com.gncaitech.flowlink.ui.components.OutlinedActionButton
import com.gncaitech.flowlink.ui.theme.G200
import com.gncaitech.flowlink.ui.theme.G500
import com.gncaitech.flowlink.ui.theme.G700
import com.gncaitech.flowlink.ui.theme.MedTeal
import com.gncaitech.flowlink.ui.theme.MontserratFamily
import com.gncaitech.flowlink.ui.theme.Navy
import com.gncaitech.flowlink.ui.theme.NavyFaint
import com.gncaitech.flowlink.ui.theme.SnowGray
import com.gncaitech.flowlink.ui.theme.TealLight
import androidx.compose.foundation.layout.imePadding
import androidx.compose.ui.text.input.KeyboardType

// ─── Step metadata ───────────────────────────────────────────────────────────

private data class StepMeta(val label: String, val cta: String)

private val REGISTER_STEPS = listOf(
    StepMeta("기본 정보", "다음: AVF 정보"),
    StepMeta("AVF 정보",  "다음: 운동 처방"),
    StepMeta("운동 처방", "등록 완료"),
)

// ─── Screen ──────────────────────────────────────────────────────────────────

@Composable
fun SubjectRegisterScreen(
    onBack: () -> Unit = {},
    onNext: () -> Unit = {},
) {
    var currentStep by remember { mutableIntStateOf(0) }
    val meta = REGISTER_STEPS[currentStep]

    // Step 1 유효성 검사 에러 상태
    var patientIdError by remember { mutableStateOf(false) }
    var nameError by remember { mutableStateOf(false) }
    var birthDateError by remember { mutableStateOf(false) }

    // Step 1 입력값 (유효성 검사를 위해 상위로 올림)
    var step1PatientId by remember { mutableStateOf("") }
    var step1Name by remember { mutableStateOf("") }
    var step1BirthDate by remember { mutableStateOf<Long?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SnowGray)
            .statusBarsPadding()
    ) {
        // AppBar — dynamic subtitle + step badge
        AppBar(
            title = "대상자 등록",
            subtitle = "STEP ${currentStep + 1} / 3 · ${meta.label}",
            onBack = onBack,
            trailingContent = {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(TealLight)
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Text(
                        "${currentStep + 1}/3",
                        style = TextStyle(
                            fontFamily = MontserratFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            letterSpacing = (0.1f * 11f).sp,
                            color = MedTeal,
                        )
                    )
                }
            }
        )

        // Stepper — clickable tabs
        RegisterStepper(
            currentStep = currentStep,
            onStepClick = { currentStep = it }
        )

        // Scrollable form — key(currentStep) resets scroll on step change
        key(currentStep) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp)
                    .padding(top = 18.dp, bottom = 16.dp),
                verticalArrangement = Arrangement.spacedBy(18.dp)
            ) {
                when (currentStep) {
                    0 -> StepBasicContent(
                        patientId = step1PatientId,
                        onPatientIdChange = { step1PatientId = it; patientIdError = false },
                        patientIdError = patientIdError,
                        name = step1Name,
                        onNameChange = { step1Name = it; nameError = false },
                        nameError = nameError,
                        birthDate = step1BirthDate,
                        onBirthDateChange = { step1BirthDate = it; birthDateError = false },
                        birthDateError = birthDateError,
                    )
                    1 -> StepAVFContent()
                    2 -> StepPrescriptionContent()
                }
            }
        }

        // Bottom CTA bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White)
                .border(0.5.dp, G200, RoundedCornerShape(0.dp))
                .imePadding()
                .navigationBarsPadding()
                .padding(horizontal = 16.dp)
                .padding(top = 12.dp, bottom = 14.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.35f)
                    .alpha(if (currentStep == 0) 0.4f else 1f)
            ) {
                OutlinedActionButton(
                    text = "이전",
                    modifier = Modifier.fillMaxWidth(),
                    height = 52.dp,
                    enabled = currentStep > 0,
                    onClick = { if (currentStep > 0) currentStep-- },
                )
            }
            FilledButton(
                text = meta.cta,
                leadingIcon = if (currentStep == 2) Icons.Default.Check
                              else Icons.AutoMirrored.Filled.ArrowForward,
                modifier = Modifier.weight(1f),
                height = 52.dp,
                onClick = {
                    if (currentStep == 0) {
                        patientIdError = step1PatientId.isBlank()
                        nameError = step1Name.isBlank()
                        birthDateError = step1BirthDate == null
                        if (!patientIdError && !nameError && !birthDateError) currentStep++
                    } else if (currentStep == 2) {
                        onNext()
                    } else {
                        currentStep++
                    }
                },
            )
        }
    }
}

// ─── Stepper ─────────────────────────────────────────────────────────────────

@Composable
private fun RegisterStepper(currentStep: Int, onStepClick: (Int) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .border(0.5.dp, G200, RoundedCornerShape(0.dp))
            .padding(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        REGISTER_STEPS.forEachIndexed { i, step ->
            val done   = i < currentStep
            val active = i == currentStep

            val barColor by animateColorAsState(
                targetValue = if (done || active) MedTeal else G200,
                animationSpec = tween(180), label = "bar$i"
            )
            val labelColor by animateColorAsState(
                targetValue = when { done -> MedTeal; active -> Navy; else -> G500 },
                animationSpec = tween(180), label = "label$i"
            )

            Column(
                modifier = Modifier
                    .weight(1f)
                    .clickable { onStepClick(i) }
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(4.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(barColor)
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = if (done) "✓ ${step.label}" else step.label,
                    style = TextStyle(
                        fontFamily = MontserratFamily,
                        fontWeight = if (active) FontWeight.Bold else FontWeight.Medium,
                        fontSize = 11.sp,
                        letterSpacing = (0.06f * 11f).sp,
                        color = labelColor,
                    )
                )
            }
        }
    }
}

// ─── Step 1 · 기본 정보 ──────────────────────────────────────────────────────

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
private fun StepBasicContent(
    patientId: String,
    onPatientIdChange: (String) -> Unit,
    patientIdError: Boolean,
    name: String,
    onNameChange: (String) -> Unit,
    nameError: Boolean,
    birthDate: Long?,
    onBirthDateChange: (Long) -> Unit,
    birthDateError: Boolean,
) {
    var sex by remember { mutableIntStateOf(0) }
    var showDatePicker by remember { mutableStateOf(false) }
    var ageInput by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var guardianPhone by remember { mutableStateOf("") }

    androidx.compose.runtime.LaunchedEffect(birthDate) {
        birthDate?.let {
            val birth = java.util.Calendar.getInstance().apply { timeInMillis = it }
            val today = java.util.Calendar.getInstance()
            var calc = today.get(java.util.Calendar.YEAR) - birth.get(java.util.Calendar.YEAR)
            if (today.get(java.util.Calendar.DAY_OF_YEAR) < birth.get(java.util.Calendar.DAY_OF_YEAR)) calc--
            ageInput = calc.toString()
        }
    }

    val birthDateText = remember(birthDate) {
        birthDate?.let {
            val cal = java.util.Calendar.getInstance().apply { timeInMillis = it }
            "%d.%02d.%02d".format(
                cal.get(java.util.Calendar.YEAR),
                cal.get(java.util.Calendar.MONTH) + 1,
                cal.get(java.util.Calendar.DAY_OF_MONTH)
            )
        } ?: ""
    }

    if (showDatePicker) {
        val datePickerState = androidx.compose.material3.rememberDatePickerState()
        androidx.compose.material3.DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                androidx.compose.material3.TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { onBirthDateChange(it) }
                    showDatePicker = false
                }) {
                    Text("확인")
                }
            },
            dismissButton = {
                androidx.compose.material3.TextButton(onClick = { showDatePicker = false }) {
                    Text("취소")
                }
            }
        ) {
            androidx.compose.material3.DatePicker(state = datePickerState)
        }
    }

    FormCard(caption = "PATIENT IDENTITY", title = "환자 기본 정보") {
        FlTextField(
            label = "환자 ID",
            value = patientId,
            onValueChange = onPatientIdChange,
            leadingIcon = Icons.Default.Tag,
            supportingText = if (patientIdError) "필수 입력 항목입니다." else "병원 환자 번호 입력",
            isError = patientIdError
        )
        Spacer(Modifier.height(16.dp))
        FlTextField(
            label = "성명",
            value = name,
            onValueChange = onNameChange,
            leadingIcon = Icons.Default.Person,
            supportingText = if (nameError) "필수 입력 항목입니다." else null,
            isError = nameError
        )
        Spacer(Modifier.height(16.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Box {
                    FlTextField(
                        label = "생년월일",
                        value = birthDateText,
                        onValueChange = {},
                        trailingIcon = Icons.Default.CalendarMonth,
                        readOnly = true,
                    )
                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .clickable(
                                indication = null,
                                interactionSource = remember {
                                    androidx.compose.foundation.interaction.MutableInteractionSource()
                                }
                            ) { showDatePicker = true }
                    )
                }
                if (birthDateError) {
                    Text(
                        "필수 입력 항목입니다.",
                        style = TextStyle(fontSize = 11.sp, color = com.gncaitech.flowlink.ui.theme.ArtRed),
                        modifier = Modifier.padding(start = 16.dp, top = 4.dp)
                    )
                }
            }
            FlTextField(
                label = "나이",
                value = ageInput,
                onValueChange = { ageInput = it },
                modifier = Modifier.width(104.dp),
                trailingLabel = "세",
                keyboardType = androidx.compose.ui.text.input.KeyboardType.Number,
            )
        }
        Spacer(Modifier.height(20.dp))
        FieldLabel("성별")
        Spacer(Modifier.height(8.dp))
        SegmentedControl(
            options = listOf("남성", "여성"),
            selectedIndex = sex,
            onSelect = { sex = it },
        )
    }

    FormCard(caption = "CONTACT", title = "연락처") {
        FlTextField(
            label = "휴대전화",
            value = phone,
            onValueChange = { phone = it },
            leadingIcon = Icons.Default.Phone,
            keyboardType = KeyboardType.Phone,
        )
        Spacer(Modifier.height(16.dp))
        FlTextField(
            label = "보호자 연락처",
            value = guardianPhone,
            onValueChange = { guardianPhone = it },
            supportingText = "응급 시 연락",
            keyboardType = KeyboardType.Phone,
        )
    }
}

// ─── Step 2 · AVF 정보 ───────────────────────────────────────────────────────

@Composable
private fun StepAVFContent() {
    var historyIdx by remember { mutableIntStateOf(0) }
    var dominantHand by remember { mutableIntStateOf(1) } // 0=왼손, 1=오른손

    FormCard(caption = "VASCULAR ACCESS", title = "AVF 수술 정보") {
        FlTextField(
            label = "수술일",
            value = "2026.05.04",
            leadingIcon = Icons.Default.CalendarMonth,
        )
        Spacer(Modifier.height(16.dp))
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
        Spacer(Modifier.height(16.dp))
        FlTextField(
            label = "담당 외과의",
            value = "김민준 교수 (혈관외과)",
            leadingIcon = Icons.Default.Person,
        )
        Spacer(Modifier.height(16.dp))
        FlTextField(
            label = "합병증 여부",
            value = "없음",
            supportingText = "해당사항이 있으면 모두 기록해주세요",
        )
    }

    FormCard(caption = "BASELINE METRICS", title = "기저 상태") {
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
        Spacer(Modifier.height(20.dp))
        FieldLabel("이전 AVF 수술 이력")
        Spacer(Modifier.height(8.dp))
        SegmentedControl(
            options = listOf("없음", "1회", "2회 이상"),
            selectedIndex = historyIdx,
            onSelect = { historyIdx = it },
        )
        Spacer(Modifier.height(20.dp))
        FieldLabel("운동 손 (Dominant Hand)")
        Spacer(Modifier.height(8.dp))
        RadioCardRow(
            options = listOf("왼손", "오른손"),
            selectedIndex = dominantHand,
            onSelect = { dominantHand = it },
        )
    }
}

// ─── Step 3 · 운동 처방 ──────────────────────────────────────────────────────

private data class ExerciseItem(val name: String, val icon: ImageVector, val sets: String, val reps: String)

@Composable
private fun StepPrescriptionContent() {
    val exercises = remember {
        listOf(
            ExerciseItem("공쥐기 (Ball Squeeze)",      Icons.Default.FrontHand,    "3 sets", "20회"),
            ExerciseItem("덤벨컬 (Dumbbell Curl)",     Icons.Default.FitnessCenter, "3 sets", "15회"),
            ExerciseItem("손목 회전 (Wrist Rotation)", Icons.Default.Loop,          "2 sets", "10회"),
        )
    }
    val exerciseSelected = remember { mutableStateListOf(true, true, false) }
    val chipSelected = remember { mutableStateListOf(true, true, true, false) }
    val chipLabels = listOf("아침 09:00", "오후 14:00", "저녁 19:00", "+ 추가")

    FormCard(caption = "EXERCISE PROTOCOL", title = "처방 운동 종목") {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            exercises.forEachIndexed { i, ex ->
                val isSel = exerciseSelected[i]
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (isSel) NavyFaint else Color.White)
                        .border(
                            width = if (isSel) 2.dp else 1.dp,
                            color = if (isSel) Navy else G200,
                            shape = RoundedCornerShape(12.dp)
                        )
                        .clickable { exerciseSelected[i] = !isSel }
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Leading icon container
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (isSel) Navy else SnowGray),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = ex.icon,
                            contentDescription = null,
                            tint = if (isSel) Color.White else Navy,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    // Name + meta
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            ex.name,
                            style = TextStyle(
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = if (isSel) Navy else G700,
                            )
                        )
                        Text(
                            "${ex.sets} · ${ex.reps}",
                            style = TextStyle(
                                fontFamily = MontserratFamily,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 11.sp,
                                letterSpacing = (0.06f * 11f).sp,
                                color = G500,
                            )
                        )
                    }
                    // Checkbox
                    Box(
                        modifier = Modifier
                            .size(22.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (isSel) Navy else Color.Transparent)
                            .border(
                                width = if (isSel) 0.dp else 1.5.dp,
                                color = G200,
                                shape = RoundedCornerShape(6.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        if (isSel) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }
                }
            }
        }
    }

    FormCard(caption = "SCHEDULE", title = "운동 일정") {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            FlTextField(
                label = "주당 횟수",
                value = "5",
                trailingLabel = "회/주",
                modifier = Modifier.weight(1f),
            )
            FlTextField(
                label = "총 기간",
                value = "12",
                trailingLabel = "주",
                modifier = Modifier.weight(1f),
            )
        }
        Spacer(Modifier.height(16.dp))
        FieldLabel("알림 시간")
        Spacer(Modifier.height(8.dp))
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            chipLabels.forEachIndexed { i, label ->
                val isSel = chipSelected[i]
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(100.dp))
                        .background(if (isSel) TealLight else Color.White)
                        .border(1.dp, if (isSel) MedTeal else G200, RoundedCornerShape(100.dp))
                        .clickable { chipSelected[i] = !isSel }
                        .padding(horizontal = 14.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    if (isSel) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            tint = MedTeal,
                            modifier = Modifier.size(12.dp)
                        )
                    }
                    Text(
                        label,
                        style = TextStyle(
                            fontFamily = MontserratFamily,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 12.sp,
                            letterSpacing = (0.04f * 12f).sp,
                            color = if (isSel) MedTeal else G500,
                        )
                    )
                }
            }
        }
    }
}

// ─── Shared primitives ───────────────────────────────────────────────────────

@Composable
private fun FieldLabel(text: String) {
    Text(
        text,
        style = TextStyle(
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            color = G700,
        )
    )
}

@Composable
private fun SegmentedControl(
    options: List<String>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(44.dp)
            .clip(RoundedCornerShape(100.dp))
            .border(1.dp, G200, RoundedCornerShape(100.dp))
    ) {
        options.forEachIndexed { i, option ->
            val isSel = i == selectedIndex
            if (i > 0) {
                Box(
                    modifier = Modifier
                        .width(1.dp)
                        .fillMaxHeight()
                        .background(G200)
                )
            }
            Row(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .background(if (isSel) Navy else Color.White)
                    .clickable { onSelect(i) },
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                if (isSel) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(Modifier.width(6.dp))
                }
                Text(
                    option,
                    style = TextStyle(
                        fontSize = 14.sp,
                        fontWeight = if (isSel) FontWeight.Medium else FontWeight.Normal,
                        color = if (isSel) Color.White else G700,
                    )
                )
            }
        }
    }
}

@Composable
private fun RadioCardRow(
    options: List<String>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        options.forEachIndexed { i, label ->
            val isSel = i == selectedIndex
            Row(
                modifier = Modifier
                    .weight(1f)
                    .height(56.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(if (isSel) NavyFaint else Color.White)
                    .border(
                        width = if (isSel) 2.dp else 1.dp,
                        color = if (isSel) Navy else G200,
                        shape = RoundedCornerShape(12.dp)
                    )
                    .clickable { onSelect(i) },
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(16.dp)
                        .clip(CircleShape)
                        .border(2.dp, if (isSel) Navy else G200, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    if (isSel) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(Navy)
                        )
                    }
                }
                Spacer(Modifier.width(8.dp))
                Text(
                    label,
                    style = TextStyle(
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = if (isSel) Navy else G700,
                    )
                )
            }
        }
    }
}
