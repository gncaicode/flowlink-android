package com.gncaitech.flowlink.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.statusBarsPadding
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
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import com.gncaitech.flowlink.data.sampleSubjects
import com.gncaitech.flowlink.ui.components.FLCaption
import com.gncaitech.flowlink.ui.components.FLWordmark
import com.gncaitech.flowlink.ui.components.FilledButton
import com.gncaitech.flowlink.ui.components.OutlinedActionButton
import com.gncaitech.flowlink.ui.components.SubjectCard
import com.gncaitech.flowlink.ui.theme.ArtRed
import com.gncaitech.flowlink.ui.theme.G200
import com.gncaitech.flowlink.ui.theme.G500
import com.gncaitech.flowlink.ui.theme.MedTeal
import com.gncaitech.flowlink.ui.theme.MontserratFamily
import com.gncaitech.flowlink.ui.theme.Navy
import com.gncaitech.flowlink.ui.theme.RedLight
import java.util.Calendar
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import com.gncaitech.flowlink.network.PatientDto
import com.gncaitech.flowlink.network.PatientApi
import com.gncaitech.flowlink.network.patientApi

@Composable
fun SubjectSelectScreen(
    onNavigateToRegister: () -> Unit = {},
    onNavigateToMeasure: (PatientDto) -> Unit = {},
    onLogout: () -> Unit = {},
) {
    var selectedPatient by remember { mutableStateOf<PatientDto?>(null) }
    var patients by remember { mutableStateOf<List<PatientDto>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var searchQuery by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        try {
            val res = patientApi.getPatients()
            if (res.isSuccessful) {
                patients = res.body() ?: emptyList()
            }
        } catch (e: Exception) {
            //실패시 빈 리스트 유지
        } finally {
            isLoading = false
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF1A365D))
            .statusBarsPadding()
    ) {
        // Navy top bar area
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Navy)
                .padding(horizontal = 16.dp)
                .padding(top = 8.dp, bottom = 20.dp)
        ) {
            // Top row: wordmark + clinician badge
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                FLWordmark(fontSize = 15.sp, linkColor = Color.White)
                Spacer(Modifier.width(10.dp))
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(MedTeal.copy(alpha = 0.22f))
                        .border(1.dp, MedTeal, RoundedCornerShape(6.dp))
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        "CLINICIAN",
                        style = TextStyle(
                            fontFamily = MontserratFamily,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 9.sp,
                            letterSpacing = (0.16f * 9f).sp,
                            color = MedTeal,
                        )
                    )
                }
                Spacer(Modifier.weight(1f))

                // 로그아웃 버튼 추가
                val context = androidx.compose.ui.platform.LocalContext.current
                androidx.compose.material3.IconButton(onClick = {
                    val prefs = context.getSharedPreferences("fl_prefs", android.content.Context.MODE_PRIVATE)
                    prefs.edit().clear().apply()
                    onLogout()
                }) {
                    Icon(
                        imageVector = androidx.compose.material.icons.Icons.AutoMirrored.Filled.Logout,
                        contentDescription = "로그아웃",
                        tint = Color.White,
                        modifier = androidx.compose.ui.Modifier.size(20.dp)
                    )
                }
            }

            // Title
            Text(
                "대상자 선택",
                style = TextStyle(
                    fontWeight = FontWeight.Normal,
                    fontSize = 26.sp,
                    color = Color.White,
                )
            )

            Spacer(Modifier.height(14.dp))

            // Search bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(Color.White.copy(alpha = 0.12f))
                    .border(1.dp, Color.White.copy(alpha = 0.16f), RoundedCornerShape(24.dp))
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ){
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = null,
                    tint = Color.White.copy(alpha = 0.6f),
                    modifier = Modifier.size(18.dp)
                )
                Spacer(Modifier.width(10.dp))
                androidx.compose.foundation.text.BasicTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    textStyle = TextStyle(fontSize = 14.sp, color = Color.White),
                    decorationBox = { innerTextField ->
                        if (searchQuery.isEmpty()) {
                            Text(
                                "이름·환자번호로 검색",
                                style = TextStyle(fontSize = 14.sp, color = Color.White.copy(alpha = 0.5f))
                            )
                        }
                        innerTextField()
                    }
                )
                Icon(
                    imageVector = Icons.Default.FilterList,
                    contentDescription = "Filter",
                    tint = Color.White.copy(alpha = 0.6f),
                    modifier = Modifier.size(18.dp)
                )
            }

        }

        // Content on SnowGray background
        Column(
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp))
                .background(Color(0xFFF7FAFC))
        ) {
            // Filter chips row
//            Row(
//                modifier = Modifier
//                    .fillMaxWidth()
//                    .horizontalScroll(rememberScrollState())
//                    .padding(horizontal = 16.dp, vertical = 14.dp),
//                horizontalArrangement = Arrangement.spacedBy(8.dp)
//            ) {
//                FilterChip(label = "오늘", count = "12", active = true)
//                FilterChip(label = "관찰필요", count = "3", active = false, tint = ArtRed, bgTint = RedLight)
//                FilterChip(label = "운동 중", count = "18", active = false)
//                FilterChip(label = "미참여", count = "3", active = false)
//            }

            // List header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                val today = remember {
                    val cal = Calendar.getInstance()
                    val dayNames = arrayOf("일","월","화","수","목","금","토")
                    val dow = dayNames[cal.get(Calendar.DAY_OF_WEEK) - 1]
                    "%d.%02d.%02d (%s)".format(
                        cal.get(Calendar.YEAR),
                        cal.get(Calendar.MONTH) + 1,
                        cal.get(Calendar.DAY_OF_MONTH),
                        dow
                    )
                }
                FLCaption("TODAY · $today")
                Spacer(Modifier.weight(1f))
                Text(
                    "예정시간순 ↓",
                    style = TextStyle(fontSize = 12.sp, color = G500)
                )
            }

            Spacer(Modifier.height(8.dp))

            // Subject list
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (isLoading) {
                    Box(
                        Modifier
                            .fillMaxWidth()
                            .padding(24.dp), contentAlignment = Alignment.Center) {
                        androidx.compose.material3.CircularProgressIndicator()
                    }
                } else {

                    val filtered = if (searchQuery.isBlank()) patients
                                    else patients.filter {
                                        it.name.contains(searchQuery, ignoreCase = true) ||
                                                it.pid.contains(searchQuery, ignoreCase = true)
                                    }
                    
                    filtered.forEachIndexed { index, patient ->
                        SubjectCard(
                            name = patient.name ?: "",
                            pid = patient.pid,
                            age = patient.age ?: 0,
                            gender = patient.gender ?: "",
                            surgery = patient.surgeryData ?: "",
                            program = patient.program ?: "",
                            scheduled = patient.scheduled ?: "",
                            status = patient.status ?: "ready",
                            selected = patient == selectedPatient,
                            onClick = { selectedPatient = patient }
                        )
                    }
                }
                Spacer(Modifier.height(16.dp))
            }

            // Bottom action bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .border(1.dp, G200, RoundedCornerShape(0.dp))
                    .navigationBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedActionButton(
                    text = "신규 등록",
                    leadingIcon = Icons.Default.Add,
                    modifier = Modifier.weight(1f),
                    height = 52.dp,
                    onClick = onNavigateToRegister,
                )
                FilledButton(
                    text = "측정 시작",
                    leadingIcon = Icons.AutoMirrored.Filled.ArrowForward,
                    modifier = Modifier.weight(1.4f),
                    height = 52.dp,
                    onClick = { selectedPatient?.let { onNavigateToMeasure(it) } },
                )
            }
        }
    }
}

@Composable
private fun FilterChip(
    label: String,
    count: String,
    active: Boolean,
    tint: Color = Navy,
    bgTint: Color = Color.Transparent,
) {
    Row(
        modifier = Modifier
            .height(32.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(if (active) tint else if (bgTint != Color.Transparent) bgTint else Color.White)
            .border(
                1.dp,
                if (active) tint else G200,
                RoundedCornerShape(16.dp)
            )
            .clickable { }
            .padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        if (active) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(12.dp)
            )
        }
        Text(
            label,
            style = TextStyle(
                fontWeight = FontWeight.SemiBold,
                fontSize = 12.sp,
                color = when {
                    active -> Color.White
                    bgTint != Color.Transparent -> tint
                    else -> G500
                }
            )
        )
        // Count badge
        Box(
            modifier = Modifier
                .clip(CircleShape)
                .background(
                    when {
                        active -> Color.White.copy(alpha = 0.25f)
                        bgTint != Color.Transparent -> tint.copy(alpha = 0.15f)
                        else -> G200
                    }
                )
                .padding(horizontal = 5.dp, vertical = 1.dp)
        ) {
            Text(
                count,
                style = TextStyle(
                    fontFamily = MontserratFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 10.sp,
                    color = when {
                        active -> Color.White
                        bgTint != Color.Transparent -> tint
                        else -> G500
                    }
                )
            )
        }
    }
}
