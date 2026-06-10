package com.gncaitech.flowlink.ui.screens

import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CenterFocusWeak
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.gncaitech.flowlink.network.PatientDto
import com.gncaitech.flowlink.ui.components.MetricChip
import com.gncaitech.flowlink.ui.components.RightActionBtn
import com.gncaitech.flowlink.ui.theme.ArtRed
import com.gncaitech.flowlink.ui.theme.MedTeal
import com.gncaitech.flowlink.ui.theme.MontserratFamily
import com.gncaitech.flowlink.ui.theme.Navy
import com.gncaitech.flowlink.ui.theme.NavyFaint
import kotlinx.coroutines.delay
import androidx.camera.core.ImageAnalysis
import androidx.compose.foundation.horizontalScroll
import androidx.compose.runtime.DisposableEffect
import com.gncaitech.flowlink.ml.HandLandmarkDetector
import java.util.concurrent.Executors
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.TextButton
import com.gncaitech.flowlink.network.patientApi
import androidx.compose.runtime.rememberCoroutineScope
import com.gncaitech.flowlink.network.SessionRequest
import kotlinx.coroutines.launch
import com.gncaitech.flowlink.ml.PoseLandmarkDetector

// Glass HUD tokens
private val GlassFill   = Color(0x66000000)  // rgba(0,0,0,0.40)
private val GlassHair   = Color(0x29FFFFFF)  // rgba(255,255,255,0.16)
private val DarkBg      = Color(0xFF0A1422)
private val FgDim       = Color.White.copy(alpha = 0.60f)
private val FgFaint     = Color.White.copy(alpha = 0.40f)
private val FgLabel     = Color.White.copy(alpha = 0.55f)

@Composable
fun MeasureScreen(
    patient: PatientDto? = null,
    config: ExerciseConfig = ExerciseConfig(),
    onClose: () -> Unit = {},
    onFinish: (totalReps: Int, totalSeconds: Int) -> Unit = {_, _ -> },
) {
    val context = LocalContext.current

    val target     = config.targetReps
    val totalSets  = config.totalSets
    val setSeconds = config.setSeconds

    var reps            by remember { mutableIntStateOf(0) }
    var currentSet      by remember { mutableIntStateOf(1) }
    var seconds         by remember { mutableIntStateOf(0) }
    var paused          by remember { mutableStateOf(false) }
    var hand            by remember { mutableStateOf("right") }
    var totalRepsAcc    by remember { mutableStateOf(0) }
    var totalSecsAcc    by remember { mutableStateOf(0) }

    var lensFacing by remember { mutableIntStateOf(CameraSelector.LENS_FACING_BACK) }
    val landmarksState = remember { mutableStateOf<List<Pair<Float, Float>>>(emptyList()) }
    var landmarks by landmarksState
    val wasOpenState = remember { mutableStateOf(true) }
    var wasOpen by wasOpenState
    val gripPercentState = remember { mutableStateOf(0) }
    var gripPercent by gripPercentState
    var repSpeedSec by remember { mutableStateOf(0.0f) }
    var lastGripOpenTimeState = remember { mutableStateOf(0L) }
    val landmarks3DState = remember { mutableStateOf<List<Triple<Float,Float,Float>>>(emptyList()) }
    var showExitDialog by remember { mutableStateOf(false) }
    var isResting by remember { mutableStateOf(false) }
    var setCompleted by remember { mutableStateOf(false) }
    var restRemaining by remember { mutableStateOf(0) }
    var isWaitingToStart by remember { mutableStateOf(true) }
    var isCountingDown by remember { mutableStateOf(false) }
    var countdownValue by remember { mutableIntStateOf(3) }

    val executor = remember { Executors.newSingleThreadExecutor() }

    val detector = remember(context) {
        HandLandmarkDetector(
            context = context,
            onResult = { handList ->
                landmarksState.value = handList.firstOrNull() ?: emptyList()
            },
            onGrip = { isClosed ->
                if (isClosed) {
                    wasOpenState.value = false
                } else {
                    if (!wasOpenState.value) {
                        reps++
                        //사이클 소요 시간 계산
                        val now = System.currentTimeMillis()
                        val last = lastGripOpenTimeState.value
                        if (last > 0) {
                            repSpeedSec = (now - last) / 1000f
                        }
                        lastGripOpenTimeState.value = now
                    }
                    wasOpenState.value = true
                }
            },
            onGripPercent = { percent ->
                gripPercentState.value = percent
            },
            onLandmarks3D = { pts ->
                landmarks3DState.value = pts
            },
            onCurlRep = {
                reps++
                val now = System.currentTimeMillis()
                val last = lastGripOpenTimeState.value
                if (last > 0) repSpeedSec = (now - last) / 1000f
                lastGripOpenTimeState.value = now
            },
            onWristRep = {
                reps++
                val now = System.currentTimeMillis()
                val last = lastGripOpenTimeState.value
                if (last > 0) repSpeedSec = (now - last) / 1000f
                lastGripOpenTimeState.value = now
            },
            exerciseKind = config.kind,
        )
    }

    val poseDetector = remember(context) {
        PoseLandmarkDetector(
            context = context,
            onCurlRep = {
                reps++
                val now = System.currentTimeMillis()
                val last = lastGripOpenTimeState.value
                if (last > 0) repSpeedSec = (now - last) / 1000f
                lastGripOpenTimeState.value = now
            },
            onLandmarks = { pts ->
                landmarksState.value = pts  // 오버레이 재활용
            }
        )
    }

    val scope = rememberCoroutineScope()

    DisposableEffect(Unit) {
        onDispose {
            detector.close()
            poseDetector.close()
            executor.shutdown()
        }
    }

    val repDone  = reps >= target
    val progress = seconds.toFloat() / setSeconds
    val mmss     = { s: Int -> "%02d:%02d".format(s / 60, s % 60) }

    val patientName = patient?.name ?: "-"
    val patientPid = patient?.pid ?: "-"
    val initial = patientName.take(1)

    // Running timer — cancelled automatically when paused, isResting, isCountingDown, or isWaitingToStart changes
    LaunchedEffect(paused, isResting, isCountingDown, isWaitingToStart) {
        if (!paused && !isResting && !isCountingDown && !isWaitingToStart) {
            while (seconds < setSeconds) {
                delay(1000L)
                seconds = minOf(setSeconds, seconds + 1)
            }
            //시간 초과 -> 자동 세트 완료
            if (setCompleted) return@LaunchedEffect
            setCompleted = true
            val feedback = when {
                reps >= target      -> "perfect"
                reps >= target / 2  -> "minor"
                else                -> "major"
            }
            val today = java.time.LocalDate.now().toString()
            val lm = landmarks3DState.value
            val landmarksJson = if (lm.isEmpty()) "" else lm.joinToString(",","[","]"){
                "[${it.first},${it.second},${it.third}]"
            }
            val req = SessionRequest(
                id = "${patient?.id ?:
                "unknown"}-set${currentSet}-${System.currentTimeMillis()}",
                patientId = patient?.id ?: "unknown",
                date = today,
                kind = config.kind,
                repsCompleted = reps,
                repsTarget = target,
                postureScore = 0,
                durationSec = seconds,
                feedback = feedback,
                landmarks = landmarksJson
            )
            scope.launch {
                try { patientApi.saveSession(req) } catch (_: Exception) {}
            }
            val accReps = totalRepsAcc + reps
            val accSecs = totalSecsAcc + seconds
            if (currentSet < totalSets) {
                totalRepsAcc = accReps
                totalSecsAcc = accSecs
                isResting = true
            } else {
                onFinish(accReps, accSecs)
            }
        }
    }

    //휴식 타이머 LaunchedEffect
    LaunchedEffect(isResting) {
        if (isResting) {
            restRemaining = config.restSeconds
            while (restRemaining > 0) {
                delay(1000L)
                restRemaining--
            }
            isResting = false
            currentSet++
            reps = 0
            seconds = 0
            setCompleted = false
            isWaitingToStart = true
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBg)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {

        if (showExitDialog) {
            AlertDialog(
                onDismissRequest = { showExitDialog = false },
                title = { Text("측정 종료") },
                text = { Text("측정을 종료하시겠습니까?\n현재 세션 데이터는 저장되지 않습니다.") },
                confirmButton = {
                    TextButton(onClick = { showExitDialog = false; onClose() }) {
                        Text("종료", color = ArtRed)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showExitDialog = false }) {
                        Text("계속")
                    }
                }
            )
        }

        // z0 · 카메라 프리뷰
        val context = LocalContext.current
        val lifecycleOwner = LocalLifecycleOwner.current
        val previewView = remember { PreviewView(context) }

        LaunchedEffect(lensFacing) {
            val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
            cameraProviderFuture.addListener({
                val cameraProvider = cameraProviderFuture.get()
                val preview = Preview.Builder().build().also {
                    it.setSurfaceProvider(previewView.surfaceProvider)
                }
                val cameraSelector = CameraSelector.Builder()
                    .requireLensFacing(lensFacing)
                    .build()
                val imageAnalysis = ImageAnalysis.Builder()
                    .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                    .build()
                    .also {
                        it.setAnalyzer(executor) { imageProxy ->
                            if (config.kind == "dumbbell") {
                                poseDetector.detect(imageProxy)
                            } else {
                                detector.detect(imageProxy)
                            }
                        }
                    }

                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(lifecycleOwner, cameraSelector, preview, imageAnalysis)
            }, ContextCompat.getMainExecutor(context))
        }

        AndroidView(
            factory = { previewView },
            modifier = Modifier.fillMaxSize()
        )

        // z1 · Alignment guide + AI hand skeleton
        AlignmentCirclesOverlay(modifier = Modifier.fillMaxSize())
        HandLandmarkCanvas(
            modifier = Modifier.fillMaxSize(),
            landmarks = landmarks,
            isFrontCamera = lensFacing == CameraSelector.LENS_FACING_FRONT
        )

        // ── Top bar (top: 12) ──────────────────────────────────────────
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 12.dp, start = 12.dp, end = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            GlassCircle(onClick = { showExitDialog = true }) {
                Icon(Icons.Default.Close, contentDescription = "닫기",
                    tint = Color.White, modifier = Modifier.size(20.dp))
            }

            // Hand toggle (flex-1 glass pill)
            Row(
                modifier = Modifier
                    .weight(1f)
                    .height(40.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(GlassFill)
                    .border(1.dp, Color.White.copy(alpha = 0.18f), RoundedCornerShape(20.dp))
            ) {
                listOf("left" to "왼손", "right" to "오른손").forEach { (key, label) ->
                    val active = hand == key
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxSize()
                            .clip(RoundedCornerShape(20.dp))
                            .background(if (active) MedTeal else Color.Transparent)
                            .clickable(
                                indication = null,
                                interactionSource = remember { MutableInteractionSource() }
                            ) { hand = key },
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            if (active) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                            Text(
                                label,
                                style = TextStyle(
                                    fontWeight = if (active) FontWeight.SemiBold else FontWeight.Medium,
                                    fontSize = 13.sp,
                                    color = if (active) Color.White else FgDim,
                                )
                            )
                        }
                    }
                }
            }

            GlassCircle(onClick = {
                lensFacing = if (lensFacing == CameraSelector.LENS_FACING_FRONT)
                    CameraSelector.LENS_FACING_BACK
                else
                    CameraSelector.LENS_FACING_FRONT
            }) {
                Icon(Icons.Default.CameraAlt, contentDescription = "카메라 전환",
                    tint = Color.White, modifier = Modifier.size(20.dp))
            }
        }

        // ── Subject chip + REC badge (top: 64) ────────────────────────
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 64.dp, start = 12.dp, end = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Subject chip with avatar
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(GlassFill)
                    .border(1.dp, GlassHair, RoundedCornerShape(12.dp))
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(CircleShape)
                        .background(NavyFaint),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        initial,
                        style = TextStyle(
                            fontFamily = MontserratFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            color = Navy
                        )
                    )
                }
                Column {
                    Text(
                        patientName,
                        style = TextStyle(
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 12.sp,
                            color = Color.White
                        )
                    )
                    Text(
                        patientPid,
                        style = TextStyle(
                            fontFamily = MontserratFamily,
                            fontSize = 9.sp,
                            letterSpacing = (0.06f * 9f).sp,
                            color = FgLabel
                        )
                    )
                }
            }

            // 운동 종류 배지
            val kindLabel = when (config.kind) {
                "dumbbell"       -> "덤벨컬"
                "wrist_rotation" -> "손목회전"
                else             -> "공쥐기"
            }
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(MedTeal.copy(alpha = 0.18f))
                    .border(1.dp, MedTeal.copy(alpha = 0.45f), RoundedCornerShape(12.dp))
                    .padding(horizontal = 14.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(MedTeal)
                )
                Text(
                    kindLabel,
                    style = TextStyle(
                        fontFamily = MontserratFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.sp,
                        letterSpacing = (0.14f * 10f).sp,
                        color = Color.White
                    )
                )
            }
        }

        // ── Timer + Set indicator card (top: 116, full-width) ─────────
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 116.dp, start = 12.dp, end = 12.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(GlassFill)
                .border(1.dp, Color.White.copy(alpha = 0.12f), RoundedCornerShape(12.dp))
                .padding(horizontal = 14.dp, vertical = 10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Clock + time
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text("⏱", style = TextStyle(fontSize = 14.sp))
                    Text(
                        mmss(seconds),
                        style = TextStyle(
                            fontFamily = MontserratFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = Color.White
                        )
                    )
                    Text("/", style = TextStyle(fontSize = 12.sp, color = FgFaint))
                    Text(
                        mmss(setSeconds),
                        style = TextStyle(
                            fontFamily = MontserratFamily,
                            fontSize = 12.sp,
                            color = FgDim
                        )
                    )
                }

                // Set indicator chips
//                Row(
//                    verticalAlignment = Alignment.CenterVertically,
//                    horizontalArrangement = Arrangement.spacedBy(4.dp)
//                ) {
//                    Text(
//                        "SET",
//                        style = TextStyle(
//                            fontFamily = MontserratFamily,
//                            fontWeight = FontWeight.Bold,
//                            fontSize = 9.sp,
//                            letterSpacing = (0.14f * 9f).sp,
//                            color = Color.White.copy(alpha = 0.50f)
//                        )
//                    )
//                    Spacer(Modifier.width(2.dp))
//                    for (n in 1..totalSets) {
//                        Box(
//                            modifier = Modifier
//                                .size(18.dp)
//                                .clip(RoundedCornerShape(4.dp))
//                                .background(
//                                    when {
//                                        n < currentSet -> MedTeal
//                                        n == currentSet -> ArtRed
//                                        else -> Color.White.copy(alpha = 0.15f)
//                                    }
//                                ),
//                            contentAlignment = Alignment.Center
//                        ) {
//                            Text(
//                                if (n < currentSet) "✓" else n.toString(),
//                                style = TextStyle(
//                                    fontFamily = MontserratFamily,
//                                    fontWeight = FontWeight.Bold,
//                                    fontSize = 10.sp,
//                                    color = if (n <= currentSet) Color.White
//                                            else Color.White.copy(alpha = 0.50f)
//                                )
//                            )
//                        }
//                    }
//                    Text(
//                        "/ $totalSets",
//                        style = TextStyle(
//                            fontFamily = MontserratFamily,
//                            fontWeight = FontWeight.SemiBold,
//                            fontSize = 11.sp,
//                            color = Color.White.copy(alpha = 0.55f)
//                        )
//                    )
//                }
                // Set indicator chips
                Column(
                    horizontalAlignment = Alignment.End,
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ){
                    Text(
                        "$currentSet / $totalSets 세트",
                        style = TextStyle(
                            fontFamily = MontserratFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = Color.White
                        )
                    )
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        for (n in 1..totalSets) {
                            Box(
                                modifier = Modifier
                                    .height(6.dp)
                                    .width(28.dp)
                                    .clip(RoundedCornerShape(3.dp))
                                    .background(
                                        when {
                                            n < currentSet      -> MedTeal
                                            n == currentSet     -> ArtRed
                                            else                -> Color.White.copy(alpha = 0.20f)
                                        }
                                    )
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(8.dp))

            // Progress bar — Teal fill
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(Color.White.copy(alpha = 0.12f))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(progress)
                        .height(4.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(MedTeal)
                )
            }
        }

        // ── AI tracking caption (top: 184, centered) ──────────────────
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 184.dp),
            horizontalArrangement = Arrangement.Center
        ) {
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(999.dp))
                    .background(MedTeal.copy(alpha = 0.18f))
                    .border(1.dp, MedTeal.copy(alpha = 0.40f), RoundedCornerShape(999.dp))
                    .padding(horizontal = 12.dp, vertical = 5.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(MedTeal)
                )
                Text(
                    "AI POSE TRACKING · 공쥐기",
                    style = TextStyle(
                        fontFamily = MontserratFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.sp,
                        letterSpacing = (0.18f * 10f).sp,
                        color = MedTeal
                    )
                )
            }
        }

        // ── Rep counter (top: 222, centered, tap to increment) ────────
        Column(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 222.dp)
                .clickable(
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() }
                ) { if (reps < target) reps++ },
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                "탭 수",
                style = TextStyle(
                    fontFamily = MontserratFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 10.sp,
                    letterSpacing = (0.22f * 10f).sp,
                    color = FgLabel
                )
            )
            Spacer(Modifier.height(2.dp))
            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    reps.toString().padStart(2, '0'),
                    style = TextStyle(
                        fontFamily = MontserratFamily,
                        fontWeight = FontWeight.Black,
                        fontSize = 108.sp,
                        color = Color.White,
                        lineHeight = 108.sp,
                        letterSpacing = (-0.05f * 108f).sp,
                    )
                )
                Spacer(Modifier.width(10.dp))
                Text(
                    "/ $target",
                    style = TextStyle(
                        fontFamily = MontserratFamily,
                        fontWeight = FontWeight.Medium,
                        fontSize = 28.sp,
                        color = FgFaint,
                    ),
                    modifier = Modifier.padding(bottom = 18.dp)
                )
            }
        }

        // ── Right action stack (bottom: 200, right: 12) ───────────────
//        Column(
//            modifier = Modifier
//                .align(Alignment.BottomEnd)
//                .padding(end = 12.dp, bottom = 200.dp),
//            verticalArrangement = Arrangement.spacedBy(8.dp)
//        ) {
//            RightActionBtn(icon = Icons.Default.CenterFocusWeak, label = "기준 설정")
//            RightActionBtn(icon = Icons.Default.FitnessCenter,   label = "미션 선택")
//        }

        // ── Bottom HUD (bottom: 16) ────────────────────────────────────
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(horizontal = 12.dp)
                .padding(bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // AI feedback bar (주석 처리)
//            Row(
//                modifier = Modifier
//                    .fillMaxWidth()
//                    .clip(RoundedCornerShape(14.dp))
//                    .background(MedTeal.copy(alpha = 0.18f))
//                    .border(1.dp, MedTeal.copy(alpha = 0.40f), RoundedCornerShape(14.dp))
//                    .padding(horizontal = 14.dp, vertical = 12.dp),
//                verticalAlignment = Alignment.CenterVertically,
//                horizontalArrangement = Arrangement.spacedBy(12.dp)
//            ) {
//                Box(
//                    modifier = Modifier
//                        .size(34.dp)
//                        .clip(CircleShape)
//                        .background(MedTeal),
//                    contentAlignment = Alignment.Center
//                ) {
//                    Icon(
//                        imageVector = Icons.Default.CheckCircle,
//                        contentDescription = null,
//                        tint = Color.White,
//                        modifier = Modifier.size(20.dp)
//                    )
//                }
//                Column(modifier = Modifier.weight(1f)) {
//                    Text(
//                        "좋아요! 자세가 정확합니다",
//                        style = TextStyle(
//                            fontWeight = FontWeight.SemiBold,
//                            fontSize = 13.sp,
//                            color = Color.White
//                        )
//                    )
//                    Text(
//                        "손목 162° · 그립 78% · 안정 ↑",
//                        style = TextStyle(
//                            fontFamily = MontserratFamily,
//                            fontSize = 11.sp,
//                            letterSpacing = (0.04f * 11f).sp,
//                            color = Color.White.copy(alpha = 0.65f)
//                        )
//                    )
//                }
//            }

            // Metric chips
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                MetricChip(label = "그립", value = "$gripPercent",  unit = "%", modifier = Modifier.weight(1f))
                MetricChip(label = "속도", value = if (repSpeedSec > 0) "%.1f".format(repSpeedSec) else "-", unit = "s", modifier =
                Modifier.weight(1f))
            }

            // Controls
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Pause / Play
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.12f))
                        .border(1.dp, Color.White.copy(alpha = 0.18f), CircleShape)
                        .clickable { paused = !paused },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (paused) Icons.Default.PlayArrow else Icons.Default.Pause,
                        contentDescription = if (paused) "재생" else "일시정지",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }

                // 시작하기 / 세트 완료하기 버튼
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp)
                        .clip(RoundedCornerShape(28.dp))
                        .background(
                            when {
                                isWaitingToStart -> MedTeal
                                repDone          -> MedTeal
                                else             -> ArtRed
                            }
                        )
                        .clickable {
                            if (isWaitingToStart) {
                                // 시작하기 — 카운트다운 후 타이머 시작
                                isWaitingToStart = false
                                scope.launch {
                                    isCountingDown = true
                                    for (i in 3 downTo 1) {
                                        countdownValue = i
                                        delay(1000L)
                                    }
                                    isCountingDown = false
                                }
                                return@clickable
                            }
                            if (setCompleted) return@clickable
                            setCompleted = true

                            val today = java.time.LocalDate.now().toString()
                            val feedback = when {
                                reps >= target     -> "perfect"
                                reps >= target / 2 -> "minor"
                                else               -> "major"
                            }
                            val lm = landmarks3DState.value
                            val landmarksJson =
                                if (lm.isEmpty()) "" else lm.joinToString(",", "[", "]") {
                                    "[${it.first},${it.second},${it.third}]"
                                }
                            val req = SessionRequest(
                                id = "${patient?.id ?: "unknown"}-set${currentSet}-${System.currentTimeMillis()}",
                                patientId = patient?.id ?: "unknown",
                                date = today,
                                kind = config.kind,
                                repsCompleted = reps,
                                repsTarget = target,
                                postureScore = 0,
                                durationSec = seconds,
                                feedback = feedback,
                                landmarks = landmarksJson
                            )
                            scope.launch {
                                try { patientApi.saveSession(req) } catch (_: Exception) {}
                            }
                            val accReps = totalRepsAcc + reps
                            val accSecs = totalSecsAcc + seconds
                            if (currentSet < totalSets) {
                                totalRepsAcc = accReps
                                totalSecsAcc = accSecs
                                isResting = true
                            } else {
                                onFinish(accReps, accSecs)
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            if (isWaitingToStart) "시작하기" else "세트 완료하기",
                            style = TextStyle(
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 16.sp,
                                letterSpacing = (0.1f).sp,
                                color = Color.White
                            )
                        )
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }

        // ── 휴식 오버레이 (모든 UI 위에 표시) ────────────────────────────
        if (isResting) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xCC0A1422)),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        "휴식",
                        style = TextStyle(
                            fontFamily = MontserratFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            letterSpacing = (0.14f * 14f).sp,
                            color = Color.White.copy(alpha = 0.60f)
                        )
                    )
                    Text(
                        restRemaining.toString(),
                        style = TextStyle(
                            fontFamily = MontserratFamily,
                            fontWeight = FontWeight.Black,
                            fontSize = 96.sp,
                            color = Color.White,
                            lineHeight = 96.sp
                        )
                    )
                    Text(
                        "다음 세트 $currentSet / $totalSets",
                        style = TextStyle(
                            fontSize = 14.sp,
                            color = Color.White.copy(alpha = 0.55f)
                        )
                    )
                    Spacer(Modifier.height(8.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(28.dp))
                            .background(MedTeal)
                            .clickable(
                                indication = null,
                                interactionSource = remember { MutableInteractionSource() }
                            ) {
                                isResting = false
                                currentSet++
                                reps = 0
                                seconds = 0
                                setCompleted = false
                                scope.launch {
                                    isCountingDown = true
                                    for (i in 3 downTo 1) {
                                        countdownValue = i
                                        delay(1000L)
                                    }
                                    isCountingDown = false
                                }
                            }
                            .padding(horizontal = 32.dp, vertical = 14.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "바로 시작",
                            style = TextStyle(
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 15.sp,
                                color = Color.White
                            )
                        )
                    }
                }
            }
        }

        // ── 카운트다운 오버레이 ───────────────────────────────────────────
        if (isCountingDown) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xCC0A1422)),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        "세트 $currentSet / $totalSets 시작",
                        style = TextStyle(
                            fontFamily = MontserratFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            letterSpacing = (0.14f * 14f).sp,
                            color = Color.White.copy(alpha = 0.60f)
                        )
                    )
                    Text(
                        countdownValue.toString(),
                        style = TextStyle(
                            fontFamily = MontserratFamily,
                            fontWeight = FontWeight.Black,
                            fontSize = 96.sp,
                            color = ArtRed,
                            lineHeight = 96.sp
                        )
                    )
                    Text(
                        "준비하세요",
                        style = TextStyle(
                            fontSize = 14.sp,
                            color = Color.White.copy(alpha = 0.55f)
                        )
                    )
                }
            }
        }
    }
}

// ---------------------------------------------------------------------------
// GlassCircle — 40dp glass icon button
// ---------------------------------------------------------------------------

@Composable
private fun GlassCircle(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    content: @Composable () -> Unit,
) {
    Box(
        modifier = modifier
            .size(40.dp)
            .clip(CircleShape)
            .background(GlassFill)
            .border(1.dp, Color.White.copy(alpha = 0.18f), CircleShape)
            .then(
                if (onClick != null)
                    Modifier.clickable(onClick = onClick)
                else Modifier
            ),
        contentAlignment = Alignment.Center
    ) { content() }
}

// ---------------------------------------------------------------------------
// SetBox — kept for backward compat (unused — chips inline above)
// ---------------------------------------------------------------------------

@Composable
private fun SetBox(number: Int, done: Boolean = false, current: Boolean = false) {
    val bg = when {
        done    -> MedTeal
        current -> ArtRed
        else    -> Color.White.copy(alpha = 0.15f)
    }
    Box(
        modifier = Modifier
            .size(18.dp)
            .clip(RoundedCornerShape(4.dp))
            .background(bg),
        contentAlignment = Alignment.Center
    ) {
        Text(
            if (done) "✓" else number.toString(),
            style = TextStyle(
                fontFamily = MontserratFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 10.sp,
                color = if (done || current) Color.White else Color.White.copy(alpha = 0.50f)
            )
        )
    }
}

// ---------------------------------------------------------------------------
// HandLandmarkCanvas — clenched fist skeleton + AVF marker
// ---------------------------------------------------------------------------

@Composable
private fun HandLandmarkCanvas(
    modifier: Modifier = Modifier,
    landmarks: List<Pair<Float, Float>> = emptyList(),
    isFrontCamera: Boolean = true
) {
    Canvas(modifier = modifier) {
        if (landmarks.size == 21) {
            //실시간 랜드마크 사용
            val pts = landmarks.map { (nx, ny) ->
                val x = if (isFrontCamera) (1f - nx) * size.width else nx * size.width
                Offset(x, ny * size.height)
            }

            val connections = listOf(
                0 to 1, 1 to 2, 2 to 3, 3 to 4,
                0 to 5, 5 to 6, 6 to 7, 7 to 8,
                0 to 9, 9 to 10, 10 to 11, 11 to 12,
                0 to 13, 13 to 14, 14 to 15, 15 to 16,
                0 to 17, 17 to 18, 18 to 19, 19 to 20,
                5 to 9, 9 to 13, 13 to 17
            )

            for ((a, b) in connections) {
                drawLine(
                    MedTeal.copy(alpha = 0.9f), pts[a], pts[b],
                    strokeWidth = size.width * 0.012f, cap = StrokeCap.Round
                )
            }

            for (pt in pts) {
                drawCircle(MedTeal.copy(alpha = 0.22f), radius = size.width * 0.022f, center = pt)
                drawCircle(MedTeal, radius = size.width * 0.012f, center = pt)
            }

            //AVF 마커 (손목 = 랜드마크 0번)
            val wrist = pts[0]
            val avfPt = Offset(wrist.x - size.width * 0.08f, wrist.y + size.height * 0.1f)
            drawLine(
                ArtRed.copy(alpha = 0.9f), avfPt, wrist, strokeWidth = size.width * 0.008f,
                cap = StrokeCap.Round, pathEffect = PathEffect.dashPathEffect(floatArrayOf(5f, 3f))
            )
            drawCircle(ArtRed, radius = size.width * 0.014f, center = avfPt)
            drawCircle(
                ArtRed.copy(alpha = 0.5f), radius = size.width * 0.024f, center = avfPt,
                style = Stroke(width = size.width * 0.005f)
            )
        } else {

            val sw = size.width
            val sh = size.height
            val sx = sw / 412f
            val sy = sh / 828f

            fun p(x: Float, y: Float) = Offset(x * sx, y * sy)

            val wrist = p(206f, 600f)
            val palm = p(206f, 510f)

            // Finger joints (clenched fist)
            val fingers = listOf(
                listOf(p(160f, 525f), p(148f, 480f), p(168f, 448f)),
                listOf(p(170f, 478f), p(175f, 438f), p(185f, 420f)),
                listOf(p(206f, 468f), p(212f, 425f), p(218f, 415f)),
                listOf(p(238f, 478f), p(244f, 438f), p(246f, 425f)),
                listOf(p(256f, 515f), p(262f, 478f), p(258f, 458f)),
            )

            val boneColor = MedTeal.copy(alpha = 0.90f)
            val strokeW = 1.8f * minOf(sx, sy) * 5f

            // Wrist to palm
            drawLine(boneColor, wrist, palm, strokeWidth = strokeW, cap = StrokeCap.Round)

            // Finger bones
            for (f in fingers) {
                drawLine(boneColor, palm, f[0], strokeWidth = strokeW, cap = StrokeCap.Round)
                drawLine(boneColor, f[0], f[1], strokeWidth = strokeW, cap = StrokeCap.Round)
                drawLine(boneColor, f[1], f[2], strokeWidth = strokeW, cap = StrokeCap.Round)
            }

            // Joints
            val allJoints = fingers.flatten() + listOf(wrist, palm)
            for (j in allJoints) {
                drawCircle(
                    MedTeal.copy(alpha = 0.22f),
                    radius = 6f * minOf(sx, sy) * 3f,
                    center = j
                )
                drawCircle(MedTeal, radius = 3.5f * minOf(sx, sy) * 3f, center = j)
            }

            // AVF marker
            val avfPt = p(155f, 685f)
            val avfPath = Path().apply { moveTo(avfPt.x, avfPt.y); lineTo(wrist.x, wrist.y) }
            drawPath(
                avfPath,
                color = ArtRed.copy(alpha = 0.90f),
                style = Stroke(
                    width = 2.2f * minOf(sx, sy) * 5f,
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(5f, 3f))
                )
            )
            drawCircle(ArtRed, radius = 5f * minOf(sx, sy) * 3f, center = avfPt)
            drawCircle(
                ArtRed.copy(alpha = 0.50f), radius = 9f * minOf(sx, sy) * 3f, center = avfPt,
                style = Stroke(width = 1.5f * minOf(sx, sy) * 3f)
            )
        }
    }
}

// ---------------------------------------------------------------------------
// AlignmentCirclesOverlay — dashed outer ring, teal inner ring, 4 brackets
// ---------------------------------------------------------------------------

@Composable
private fun AlignmentCirclesOverlay(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val cx = size.width / 2f
        val cy = size.height * (450f / 828f)

        val outerR = size.width * (180f / 412f)
        val innerR = size.width * (130f / 412f)

        // Dashed outer ring
        drawCircle(
            color = Color.White.copy(alpha = 0.12f),
            radius = outerR,
            center = Offset(cx, cy),
            style = Stroke(width = 1f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 6f)))
        )

        // Solid inner teal ring
        drawCircle(
            color = MedTeal.copy(alpha = 0.35f),
            radius = innerR,
            center = Offset(cx, cy),
            style = Stroke(width = 1.5f)
        )

        // 4 corner brackets
        val sx = size.width  / 412f
        val sy = size.height / 828f
        val legLen = 24f * minOf(sx, sy) * 10f
        val brackets = listOf(
            Triple(20f * sx, 260f * sy,  Pair( 1f,  1f)),
            Triple(392f * sx, 260f * sy, Pair(-1f,  1f)),
            Triple(20f * sx, 640f * sy,  Pair( 1f, -1f)),
            Triple(392f * sx, 640f * sy, Pair(-1f, -1f)),
        )
        val bracketColor = MedTeal.copy(alpha = 0.70f)
        for ((bx, by, dir) in brackets) {
            val pt = Offset(bx, by)
            drawLine(bracketColor, pt, Offset(bx + legLen * dir.first,  by), strokeWidth = 2f, cap = StrokeCap.Round)
            drawLine(bracketColor, pt, Offset(bx, by + legLen * dir.second), strokeWidth = 2f, cap = StrokeCap.Round)
        }
    }
}
