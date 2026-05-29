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

@Composable
fun SubjectSelectScreen(
    onNavigateToRegister: () -> Unit = {},
    onNavigateToMeasure: () -> Unit = {},
) {
    var selectedSubjectIndex by remember { mutableIntStateOf(0) }

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
            ) {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = null,
                    tint = Color.White.copy(alpha = 0.6f),
                    modifier = Modifier.size(18.dp)
                )
                Spacer(Modifier.width(10.dp))
                Text(
                    "이름·환자번호로 검색",
                    style = TextStyle(
                        fontSize = 14.sp,
                        color = Color.White.copy(alpha = 0.5f),
                    ),
                    modifier = Modifier.weight(1f)
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
                sampleSubjects.forEachIndexed { index, subject ->
                    SubjectCard(
                        name = subject.name,
                        pid = subject.pid,
                        age = subject.age,
                        gender = subject.gender,
                        surgery = subject.surgery,
                        maturity = subject.maturity,
                        program = subject.program,
                        scheduled = subject.scheduled,
                        status = subject.status,
                        selected = index == selectedSubjectIndex,
                        onClick = { selectedSubjectIndex = index }
                    )
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
                    onClick = onNavigateToMeasure,
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
