package com.gncaitech.flowlink.ui.screens

import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CenterFocusWeak
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gncaitech.flowlink.ui.components.FilledButton
import com.gncaitech.flowlink.ui.components.MetricChip
import com.gncaitech.flowlink.ui.components.RightActionBtn
import com.gncaitech.flowlink.ui.theme.ArtRed
import com.gncaitech.flowlink.ui.theme.MedTeal
import com.gncaitech.flowlink.ui.theme.MontserratFamily

private val DarkBg = Color(0xFF0A1422)

@Composable
fun MeasureScreen(
    onClose: () -> Unit = {},
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBg)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        // Background radial gradient
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            Color(0xFF0D2137),
                            Color(0xFF070F1A),
                        )
                    )
                )
        )

        // Hand landmark skeleton on canvas
        HandLandmarkCanvas(
            modifier = Modifier.fillMaxSize()
        )

        // Alignment circles overlay
        AlignmentCirclesOverlay(
            modifier = Modifier.fillMaxSize()
        )

        // Top controls row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 48.dp, start = 12.dp, end = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Close button
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.12f))
                    .clickable(onClick = onClose),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Close",
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(Modifier.weight(1f))

            // Hand toggle
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color.White.copy(alpha = 0.12f))
                    .padding(4.dp)
            ) {
                listOf("왼손" to false, "오른손" to true).forEach { (label, selected) ->
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .background(if (selected) MedTeal else Color.Transparent)
                            .clickable { }
                            .padding(horizontal = 16.dp, vertical = 6.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            label,
                            style = TextStyle(
                                fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                                fontSize = 13.sp,
                                color = Color.White
                            )
                        )
                    }
                }
            }

            Spacer(Modifier.weight(1f))

            // Camera flip button
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.12f))
                    .clickable { },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.CameraAlt,
                    contentDescription = "Flip camera",
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        // Subject chip + REC badge
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 102.dp, start = 12.dp, end = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Subject chip
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color.White.copy(alpha = 0.10f))
                    .border(1.dp, Color.White.copy(alpha = 0.15f), RoundedCornerShape(20.dp))
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Text(
                    "김선영  ·  P-2026-04812",
                    style = TextStyle(
                        fontSize = 12.sp,
                        color = Color.White.copy(alpha = 0.85f)
                    )
                )
            }

            // REC badge
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(ArtRed.copy(alpha = 0.85f))
                    .padding(horizontal = 10.dp, vertical = 5.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(7.dp)
                        .clip(CircleShape)
                        .background(Color.White)
                )
                Spacer(Modifier.width(6.dp))
                Text(
                    "REC",
                    style = TextStyle(
                        fontFamily = MontserratFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        color = Color.White,
                        letterSpacing = 0.12.sp,
                    )
                )
            }
        }

        // Timer + Set panel
        Box(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(top = 152.dp, start = 12.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(Color.White.copy(alpha = 0.08f))
                .border(1.dp, Color.White.copy(alpha = 0.12f), RoundedCornerShape(14.dp))
                .padding(horizontal = 14.dp, vertical = 10.dp)
        ) {
            Column {
                // Clock row
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        "⏱",
                        style = TextStyle(fontSize = 12.sp)
                    )
                    Spacer(Modifier.width(5.dp))
                    Text(
                        "00:42 / 02:00",
                        style = TextStyle(
                            fontFamily = MontserratFamily,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 13.sp,
                            color = Color.White
                        )
                    )
                }
                Spacer(Modifier.height(8.dp))
                // Set boxes
                Row(horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                    SetBox(number = 1, done = true)
                    SetBox(number = 2, current = true)
                    SetBox(number = 3)
                }
                Spacer(Modifier.height(8.dp))
                // Progress bar
                Box(
                    modifier = Modifier
                        .width(120.dp)
                        .height(4.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(Color.White.copy(alpha = 0.2f))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.35f)
                            .height(4.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(ArtRed)
                    )
                }
            }
        }

        // AI Tracking label
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 152.dp, end = 12.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(MedTeal.copy(alpha = 0.18f))
                .border(1.dp, MedTeal.copy(alpha = 0.45f), RoundedCornerShape(20.dp))
                .padding(horizontal = 12.dp, vertical = 7.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(7.dp)
                        .clip(CircleShape)
                        .background(MedTeal)
                )
                Spacer(Modifier.width(6.dp))
                Text(
                    "AI POSE TRACKING · 공쥐기",
                    style = TextStyle(
                        fontFamily = MontserratFamily,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 10.sp,
                        letterSpacing = (0.10f * 10f).sp,
                        color = MedTeal,
                    )
                )
            }
        }

        // Big counter (center)
        Column(
            modifier = Modifier.align(Alignment.Center),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                "탭 수",
                style = TextStyle(
                    fontFamily = MontserratFamily,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 10.sp,
                    letterSpacing = (0.14f * 10f).sp,
                    color = Color.White.copy(alpha = 0.55f),
                )
            )
            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    "08",
                    style = TextStyle(
                        fontFamily = MontserratFamily,
                        fontWeight = FontWeight.Black,
                        fontSize = 108.sp,
                        color = Color.White,
                        lineHeight = 108.sp,
                    )
                )
                Spacer(Modifier.width(6.dp))
                Text(
                    "/ 15",
                    style = TextStyle(
                        fontFamily = MontserratFamily,
                        fontWeight = FontWeight.Normal,
                        fontSize = 28.sp,
                        color = Color.White.copy(alpha = 0.40f),
                    ),
                    modifier = Modifier.padding(bottom = 18.dp)
                )
            }
        }

        // Right-stacked action buttons
        Column(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = 12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            RightActionBtn(
                icon = Icons.Default.CenterFocusWeak,
                label = "기준 설정"
            )
            RightActionBtn(
                icon = Icons.Default.FitnessCenter,
                label = "미션 선택"
            )
        }

        // Bottom controls
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // AI feedback bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(MedTeal.copy(alpha = 0.12f))
                    .border(1.dp, MedTeal.copy(alpha = 0.3f), RoundedCornerShape(16.dp))
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = MedTeal,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(Modifier.width(10.dp))
                Column {
                    Text(
                        "좋아요! 자세가 정확합니다",
                        style = TextStyle(
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = Color.White
                        )
                    )
                    Text(
                        "손목 162° · 그립 78% · 안정 ↑",
                        style = TextStyle(
                            fontSize = 11.sp,
                            color = MedTeal.copy(alpha = 0.85f)
                        )
                    )
                }
            }

            // Metrics row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                MetricChip(label = "그립", value = "78", unit = "%", modifier = Modifier.weight(1f))
                MetricChip(label = "속도", value = "2.1", unit = "s", modifier = Modifier.weight(1f))
                MetricChip(label = "범위", value = "92", unit = "°", modifier = Modifier.weight(1f))
            }

            // Control row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Pause button
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.12f))
                        .border(1.dp, Color.White.copy(alpha = 0.2f), CircleShape)
                        .clickable { },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Pause,
                        contentDescription = "Pause",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }

                FilledButton(
                    text = "세트 완료하기",
                    leadingIcon = Icons.AutoMirrored.Filled.ArrowForward,
                    modifier = Modifier.weight(1f),
                    height = 56.dp,
                )
            }
        }
    }
}

// ---------------------------------------------------------------------------
// SetBox — small set indicator
// ---------------------------------------------------------------------------

@Composable
private fun SetBox(
    number: Int,
    done: Boolean = false,
    current: Boolean = false,
) {
    val bg = when {
        done -> MedTeal.copy(alpha = 0.85f)
        current -> ArtRed.copy(alpha = 0.85f)
        else -> Color.White.copy(alpha = 0.10f)
    }
    Box(
        modifier = Modifier
            .size(28.dp)
            .clip(RoundedCornerShape(6.dp))
            .background(bg),
        contentAlignment = Alignment.Center
    ) {
        Text(
            if (done) "✓" else number.toString(),
            style = TextStyle(
                fontFamily = MontserratFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp,
                color = if (done || current) Color.White else Color.White.copy(alpha = 0.45f),
            )
        )
    }
}

// ---------------------------------------------------------------------------
// HandLandmarkCanvas
// ---------------------------------------------------------------------------

@Composable
private fun HandLandmarkCanvas(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val sw = size.width
        val sh = size.height
        val sx = sw / 412f
        val sy = sh / 828f

        fun p(x: Float, y: Float) = Offset(x * sx, y * sy)

        // Clenched fist hand landmark positions (viewBox 412x828)
        val wrist = p(206f, 600f)
        val palmBase = p(206f, 510f)

        // Thumb
        val thumb = listOf(wrist, p(170f, 540f), p(148f, 510f), p(138f, 486f))
        // Index
        val index = listOf(palmBase, p(178f, 480f), p(172f, 455f), p(169f, 435f))
        // Middle
        val middle = listOf(palmBase, p(200f, 475f), p(196f, 450f), p(193f, 428f))
        // Ring
        val ring = listOf(palmBase, p(222f, 478f), p(220f, 454f), p(218f, 433f))
        // Pinky
        val pinky = listOf(palmBase, p(244f, 485f), p(248f, 463f), p(250f, 445f))

        val allFingers = listOf(thumb, index, middle, ring, pinky)
        val dotColor = MedTeal
        val lineColor = MedTeal.copy(alpha = 0.75f)

        // Draw wrist to palm line
        drawLine(lineColor, wrist, palmBase, strokeWidth = 1.8f * minOf(sx, sy) * 5f, cap = StrokeCap.Round)

        // Draw finger bones
        for (finger in allFingers) {
            for (i in 0 until finger.size - 1) {
                drawLine(
                    color = lineColor,
                    start = finger[i],
                    end = finger[i + 1],
                    strokeWidth = 1.8f * minOf(sx, sy) * 5f,
                    cap = StrokeCap.Round
                )
            }
        }

        // Draw joint dots
        val allJoints = allFingers.flatten() + wrist
        for (joint in allJoints) {
            // Outer ring
            drawCircle(
                color = dotColor.copy(alpha = 0.22f),
                radius = 6f * minOf(sx, sy) * 3f,
                center = joint
            )
            // Filled dot
            drawCircle(
                color = dotColor,
                radius = 3.5f * minOf(sx, sy) * 3f,
                center = joint
            )
        }

        // AVF marker
        val avfStart = p(155f, 685f)
        val avfEnd = wrist
        val dashPath = Path().apply {
            moveTo(avfStart.x, avfStart.y)
            lineTo(avfEnd.x, avfEnd.y)
        }
        drawPath(
            path = dashPath,
            color = ArtRed.copy(alpha = 0.75f),
            style = Stroke(
                width = 1.5f * minOf(sx, sy) * 5f,
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 5f))
            )
        )
        // Red circle at marker
        drawCircle(
            color = ArtRed.copy(alpha = 0.55f),
            radius = 10f * minOf(sx, sy) * 3f,
            center = avfStart,
            style = Stroke(width = 1.5f * minOf(sx, sy) * 3f)
        )
        drawCircle(
            color = ArtRed,
            radius = 4f * minOf(sx, sy) * 3f,
            center = avfStart
        )
    }
}

// ---------------------------------------------------------------------------
// AlignmentCirclesOverlay
// ---------------------------------------------------------------------------

@Composable
private fun AlignmentCirclesOverlay(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val cx = size.width / 2f
        val cy = size.height * 0.48f
        val outerR = size.width * 0.38f
        val innerR = size.width * 0.24f

        // Dashed outer circle
        drawCircle(
            color = MedTeal.copy(alpha = 0.22f),
            radius = outerR,
            center = Offset(cx, cy),
            style = Stroke(
                width = 1.5f,
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 8f))
            )
        )

        // Solid inner teal circle
        drawCircle(
            color = MedTeal.copy(alpha = 0.12f),
            radius = innerR,
            center = Offset(cx, cy),
            style = Stroke(width = 2f)
        )

        // 4 corner brackets in teal
        val bracketLen = 28f
        val bracketThick = 3f
        val corners = listOf(
            Offset(cx - outerR * 0.72f, cy - outerR * 0.72f),
            Offset(cx + outerR * 0.72f, cy - outerR * 0.72f),
            Offset(cx - outerR * 0.72f, cy + outerR * 0.72f),
            Offset(cx + outerR * 0.72f, cy + outerR * 0.72f),
        )
        val cornerDirs = listOf(
            Pair(1f, 1f), Pair(-1f, 1f), Pair(1f, -1f), Pair(-1f, -1f)
        )
        for ((corner, dir) in corners.zip(cornerDirs)) {
            val (dx, dy) = dir
            drawLine(
                MedTeal.copy(alpha = 0.6f),
                corner,
                Offset(corner.x + bracketLen * dx, corner.y),
                strokeWidth = bracketThick,
                cap = StrokeCap.Round
            )
            drawLine(
                MedTeal.copy(alpha = 0.6f),
                corner,
                Offset(corner.x, corner.y + bracketLen * dy),
                strokeWidth = bracketThick,
                cap = StrokeCap.Round
            )
        }
    }
}
