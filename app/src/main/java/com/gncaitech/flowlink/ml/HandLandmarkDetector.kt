package com.gncaitech.flowlink.ml

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Matrix
import android.os.SystemClock
import android.util.Log
import androidx.camera.core.ImageProxy
import com.google.mediapipe.framework.image.BitmapImageBuilder
import com.google.mediapipe.tasks.core.BaseOptions
import com.google.mediapipe.tasks.vision.core.RunningMode
import com.google.mediapipe.tasks.vision.handlandmarker.HandLandmarker
import com.google.mediapipe.tasks.vision.handlandmarker.HandLandmarkerResult

//⏺손목회전 인식 원리
//
//손목회전은 검지 MCP(5번)와 새끼 MCP(17번)의 X좌표 차이로 감지합니다.
//
//손바닥이 앞(카메라 방향): pts[5].x > pts[17].x → xDiff 양수
//손바닥이 뒤(반대 방향):   pts[5].x < pts[17].x → xDiff 음수
//
//뒤로 돌림 → 앞으로 복귀 = 1회
//
//그립 감지와 동일한 구조입니다:
//- isHandClosed → isBack (손등이 앞으로)
//- wasOpen → hasGoneBack (뒤로 돌렸던 상태)


data class HandFrameData(
    val landmarks3D: List<Triple<Float, Float, Float>>,
    val gripPercent: Int,
    val isClosed: Boolean,
    val wristXDiff: Float,   // pts[5].x - pts[17].x (손목회전 판단 기준)
)

class HandLandmarkDetector(
    context: Context,
    private val onResult: (List<List<Pair<Float,Float>>>) -> Unit,
    private val onGrip: (isClosed: Boolean) -> Unit = {},
    private val onGripPercent: (Int) -> Unit = {},
    private val onLandmarks3D: (List<Triple<Float,Float,Float>>) -> Unit = {},
    private val onHandFrame: ((HandFrameData) -> Unit)? = null,
    private val onCurlRep: () -> Unit = {},
    private val onWristRep: () -> Unit = {},
    private val onGripRep: () -> Unit = {},
    private val exerciseKind: String = "grip",
    private val onLog: ((String) -> Unit)? = null,
) {
    @Volatile private var closed = false
    private var handLandmarker: HandLandmarker? = null
    private var smoothWristY = -1f
    private var curlIsUp = false
    private var wristHasGoneBack = false
    private val WRIST_THRESHOLD  = 0.10f
    private var CURL_THRESHOLD   = 0.15f

    // 히스테리시스 — 굽혀진 손가락 개수 기준
    // 3개 이상 → 쥠 / 1개 이하 → 폄 / 2개 → 이전 상태 유지
    private var handIsClosedState = false

    // Peak-Valley — Median 필터(N=3) + 4방향 상태 머신 + 최소 진폭 0.15
    // ratio 평균: 높음(손 펼침, PEAK) → 낮음(손 쥠, VALLEY) → 높음(PEAK) = 1 REP
    // PEAK→DESCENDING 전환 시 peakValue 기록, VALLEY→ASCENDING 전환 시 valleyValue 기록
    // ASCENDING→PEAK 전환 시 (peakValue - valleyValue) >= MIN_AMPLITUDE 일 때만 REP 카운트
    private val ratioBuffer = FloatArray(3) { 1.5f }
    private var ratioBufferIdx = 0
    private enum class PVState { PEAK, DESCENDING, VALLEY, ASCENDING }
    private var pvState = PVState.PEAK
    private var prevSmoothedRatio = -1f
    private var peakValue = -1f
    private var valleyValue = -1f
    private val MIN_AMPLITUDE = 0.15f
    private val MIN_ASCENT    = 0.08f  // ASCENDING→PEAK 시 valley 대비 최소 상승폭

    // 진단용
    private var valleyMinRatio  = Float.MAX_VALUE  // VALLEY 상태에서 관측된 실제 최솟값
    private var ascentMaxRatio  = -1f              // ASCENDING 상태에서 관측된 실제 최댓값
    private var stateFrameCount = 0                // 현재 상태 체류 프레임 수
    private var repCount        = 0                // 내부 카운터 (로그용)
    private var frameCount      = 0                // 전체 프레임 카운터

    init {
        val baseOptions = BaseOptions.builder()
            .setModelAssetPath("hand_landmarker.task")
            .build()

        val options = HandLandmarker.HandLandmarkerOptions.builder()
            .setBaseOptions(baseOptions)
            .setNumHands(1)
            .setMinHandDetectionConfidence(0.5f)
            .setMinHandPresenceConfidence(0.5f)
            .setMinTrackingConfidence(0.5f)
            .setRunningMode(RunningMode.LIVE_STREAM)
            .setResultListener { result: HandLandmarkerResult, _ ->
                if (closed) return@setResultListener
                val hands = result.landmarks().map { hand ->
                    hand.map { lm -> Pair(lm.x(), lm.y()) }
                }
                onResult(hands)

                // 그립 감지 - 첫 번째 손만 사용
                val hand = result.landmarks().firstOrNull()
                if (hand != null && hand.size == 21) {
                    val pts      = hand.map { Pair(it.x(), it.y()) }
                    val pts3D    = hand.map { Triple(it.x(), it.y(), it.z()) }
                    val isClosed = isHandClosed(pts3D)
                    val grip     = calcGripPercent(pts3D)
                    val xDiff    = pts[5].first - pts[17].first

                    onGrip(isClosed)
                    onGripPercent(grip)
                    onLandmarks3D(pts3D)
                    onHandFrame?.invoke(HandFrameData(pts3D, grip, isClosed, xDiff))

                    if (exerciseKind == "grip")           detectGripRep(pts3D)
                    if (exerciseKind == "dumbbell")       detectCurl(pts)
                    if (exerciseKind == "wrist_rotation") detectWristRotation(pts)
                }
            }
            .setErrorListener{ error -> error.printStackTrace() }
            .build()

        handLandmarker = HandLandmarker.createFromOptions(context, options)
    }

    fun detect(imageProxy: ImageProxy) {
        if (closed) { imageProxy.close(); return }
        val bitmap = imageProxy.toBitmap()
        val rotationDegrees = imageProxy.imageInfo.rotationDegrees

        // CameraX 이미지는 센서 기준(가로)으로 오므로 화면 방향에 맞게 회전
        val rotatedBitmap = if (rotationDegrees != 0) {
            val matrix = Matrix().apply { postRotate(rotationDegrees.toFloat()) }
            Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
        } else {
            bitmap
        }

        val mpImage = BitmapImageBuilder(rotatedBitmap).build()
        handLandmarker?.detectAsync(mpImage, SystemClock.uptimeMillis())
        imageProxy.close()
    }

    fun close() {
        closed = true
        handLandmarker?.close()
        handLandmarker = null
    }

    // 손목(0)과 각 손가락 TIP/MCP 사이의 3D 거리 비교
    // dist(손목, TIP) < dist(손목, MCP) → TIP이 손목에 가깝다 → 굽혀진 상태
    private fun dist3D(a: Triple<Float,Float,Float>, b: Triple<Float,Float,Float>): Float {
        val dx = a.first  - b.first
        val dy = a.second - b.second
        val dz = a.third  - b.third
        return Math.sqrt((dx*dx + dy*dy + dz*dz).toDouble()).toFloat()
    }

    // 히스테리시스 적용 — 굽혀진 손가락 개수 기준
    // 3개 이상 → 쥠 / 1개 이하 → 폄 / 2개 → 이전 상태 유지 (경계 구간 노이즈 방지)
    private fun isHandClosed(pts3D: List<Triple<Float, Float, Float>>): Boolean {
        val wrist      = pts3D[0]
        val fingerTips = listOf(8, 12, 16, 20)
        val fingerMcps = listOf(5,  9, 13, 17)
        var closedCount = 0
        val ratios = mutableListOf<Float>()
        for (i in fingerTips.indices) {
            val distTip = dist3D(wrist, pts3D[fingerTips[i]])
            val distMcp = dist3D(wrist, pts3D[fingerMcps[i]])
            val ratio = if (distMcp > 0f) distTip / distMcp else 1f
            ratios.add(ratio)
            if (distTip < distMcp) closedCount++
        }
        val prevState = handIsClosedState
        handIsClosedState = when {
            closedCount >= 3 -> true
            closedCount <= 1 -> false
            else             -> handIsClosedState
        }
        return handIsClosedState
    }

    private fun calcGripPercent(pts3D: List<Triple<Float, Float, Float>>): Int {
        val wrist      = pts3D[0]
        val fingerTips = listOf(8, 12, 16, 20)
        val fingerMcps = listOf(5,  9, 13, 17)
        var count = 0
        for (i in fingerTips.indices) {
            val distTip = dist3D(wrist, pts3D[fingerTips[i]])
            val distMcp = dist3D(wrist, pts3D[fingerMcps[i]])
            if (distTip < distMcp) count++
        }
        return (count * 100) / 4
    }

    // Median 필터 (N=3) — 순간 스파이크 제거, 분기 없는 3-정렬
    private fun medianFilter(value: Float): Float {
        ratioBuffer[ratioBufferIdx % 3] = value
        ratioBufferIdx++
        val a = ratioBuffer[0]; val b = ratioBuffer[1]; val c = ratioBuffer[2]
        return maxOf(minOf(a, b), minOf(maxOf(a, b), c))
    }

    // 4개 손가락 TIP/MCP ratio 평균 (낮을수록 더 쥔 상태)
    private fun calcAvgRatio(pts3D: List<Triple<Float, Float, Float>>): Float {
        val wrist      = pts3D[0]
        val fingerTips = listOf(8, 12, 16, 20)
        val fingerMcps = listOf(5,  9, 13, 17)
        var sum = 0f
        for (i in fingerTips.indices) {
            val distTip = dist3D(wrist, pts3D[fingerTips[i]])
            val distMcp = dist3D(wrist, pts3D[fingerMcps[i]])
            sum += if (distMcp > 0f) distTip / distMcp else 1f
        }
        return sum / 4f
    }

    private fun logLine(tag: String, msg: String) {
        Log.d(tag, msg)
        onLog?.invoke("$tag: $msg")
    }

    // Peak-Valley grip 1회 감지
    // PEAK → DESCENDING → VALLEY → ASCENDING → PEAK 사이클 완성 시 onGripRep() 호출
    private fun detectGripRep(pts3D: List<Triple<Float, Float, Float>>) {
        frameCount++

        // ── 개별 ratio 계산 (검지/중지/약지/새끼) ──────────────────────────
        val wrist  = pts3D[0]
        val tipIdx = listOf(8, 12, 16, 20)
        val mcpIdx = listOf(5,  9, 13, 17)
        val ratios = tipIdx.indices.map { i ->
            val dt = dist3D(wrist, pts3D[tipIdx[i]])
            val dm = dist3D(wrist, pts3D[mcpIdx[i]])
            if (dm > 0f) dt / dm else 1f
        }
        val rawAvg   = ratios.sum() / 4f
        val smoothed = medianFilter(rawAvg)

        // ── 신호 로그 (매 프레임) ────────────────────────────────────────────
        logLine("GripPV",
            "ratios=[${ratios.joinToString { "%.3f".format(it) }}]" +
            " raw=${"%.3f".format(rawAvg)}" +
            " smoothed=${"%.3f".format(smoothed)}" +
            " state=$pvState"
        )

        // ── 초기화 (첫 프레임) ───────────────────────────────────────────────
        if (prevSmoothedRatio < 0f) {
            prevSmoothedRatio = smoothed
            logLine("GripPV", "초기화: prevSmoothed=${"%.3f".format(smoothed)}")
            return
        }

        val diff      = smoothed - prevSmoothedRatio
        val prevState = pvState

        // ── 상태별 실시간 최솟값/최댓값 추적 ────────────────────────────────
        if (pvState == PVState.VALLEY) {
            if (smoothed < valleyMinRatio) valleyMinRatio = smoothed
        }
        if (pvState == PVState.ASCENDING) {
            if (smoothed > ascentMaxRatio) ascentMaxRatio = smoothed
        }

        // ── 상태 전환 ────────────────────────────────────────────────────────
        when (pvState) {
            PVState.PEAK       -> if (diff < 0f) {
                peakValue = prevSmoothedRatio
                pvState = PVState.DESCENDING
                stateFrameCount = 0
                valleyMinRatio = Float.MAX_VALUE
            }
            PVState.DESCENDING -> if (diff >= 0f) {
                pvState = PVState.VALLEY
                stateFrameCount = 0
                valleyMinRatio = smoothed
            }
            PVState.VALLEY     -> if (diff > 0f) {
                valleyValue = prevSmoothedRatio
                pvState = PVState.ASCENDING
                stateFrameCount = 0
                ascentMaxRatio = smoothed
                // VALLEY 요약 로그
                logLine("GripPV",
                    "▼VALLEY 요약" +
                    " | valleyValue=${"%.3f".format(valleyValue)}" +
                    " | 실제최솟값=${"%.3f".format(valleyMinRatio)}" +
                    " | peakValue=${"%.3f".format(peakValue)}" +
                    " | 예상진폭=${"%.3f".format(peakValue - valleyValue)}" +
                    " | 필요진폭=${"%.3f".format(MIN_AMPLITUDE)}" +
                    if (peakValue - valleyValue >= MIN_AMPLITUDE) " ✓진폭OK" else " ✗진폭부족"
                )
            }
            PVState.ASCENDING  -> if (diff <= 0f) {
                val amplitude = peakValue - valleyValue
                val ascent    = prevSmoothedRatio - valleyValue
                // ASCENDING 요약 로그 (카운트 여부와 무관하게 항상 출력)
                logLine("GripPV",
                    "▲ASCENDING 요약" +
                    " | valleyValue=${"%.3f".format(valleyValue)}" +
                    " | 실제최댓값=${"%.3f".format(ascentMaxRatio)}" +
                    " | 판정시점값=${"%.3f".format(prevSmoothedRatio)}" +
                    " | ascent=${"%.3f".format(ascent)}" +
                    " | amplitude=${"%.3f".format(amplitude)}" +
                    " | 필요ascent=${"%.3f".format(MIN_ASCENT)}" +
                    " | 필요amplitude=${"%.3f".format(MIN_AMPLITUDE)}"
                )
                if (amplitude >= MIN_AMPLITUDE && ascent >= MIN_ASCENT) {
                    pvState = PVState.PEAK
                    repCount++
                    onGripRep()
                    logLine("GripPV", "★★★ REP #$repCount 카운트! amplitude=${"%.3f".format(amplitude)} ascent=${"%.3f".format(ascent)}")
                } else {
                    pvState = PVState.PEAK
                    val reason = when {
                        amplitude < MIN_AMPLITUDE && ascent < MIN_ASCENT ->
                            "진폭부족(${" %.3f".format(amplitude)}<${MIN_AMPLITUDE}) AND 상승부족(${"%.3f".format(ascent)}<${MIN_ASCENT})"
                        amplitude < MIN_AMPLITUDE ->
                            "진폭부족(${"%.3f".format(amplitude)}<${MIN_AMPLITUDE}) — 더 깊이 쥐어야 함"
                        else ->
                            "상승부족(${"%.3f".format(ascent)}<${MIN_ASCENT}) — 더 많이 펴야 함"
                    }
                    logLine("GripPV", "✗ REP 거부: $reason")
                }
                stateFrameCount = 0
            }
        }

        stateFrameCount++

        // ── 상태 전환 로그 ───────────────────────────────────────────────────
        if (prevState != pvState) {
            logLine("GripPV",
                "→전환 $prevState → $pvState" +
                " | diff=${"%.3f".format(diff)}" +
                " smoothed=${"%.3f".format(smoothed)}" +
                " peak=${"%.3f".format(peakValue)}" +
                " valley=${"%.3f".format(valleyValue)}"
            )
        }

        // ── 주기 로그 (30프레임마다) — 상태 머신이 어디서 멈춰있는지 확인 ──
        if (frameCount % 30 == 0) {
            val stuckInfo = when (pvState) {
                PVState.VALLEY -> " 실제최솟값=${"%.3f".format(valleyMinRatio)} 예상진폭=${"%.3f".format(peakValue - valleyMinRatio)}"
                PVState.ASCENDING -> " 현재ascent=${"%.3f".format(smoothed - valleyValue)} 실제최댓값=${"%.3f".format(ascentMaxRatio)}"
                else -> ""
            }
            logLine("GripPV",
                "◆상태[$frameCount] $pvState ($stateFrameCount 프레임째)" +
                " smoothed=${"%.3f".format(smoothed)}" +
                " peak=${"%.3f".format(peakValue)}" +
                " valley=${"%.3f".format(valleyValue)}" +
                " reps=$repCount" +
                stuckInfo
            )
        }

        prevSmoothedRatio = smoothed
    }

    private fun detectCurl(pts:List<Pair<Float,Float>>) {
        val wristY = pts[0].second

        if (smoothWristY < 0f) {
            smoothWristY = wristY
            return
        }

        //팔이 내려가 있을 때만 기준값 업데이트
        if (!curlIsUp){
            smoothWristY = smoothWristY * 0.90f + wristY * 0.10f
        }

        val delta = smoothWristY - wristY // 양수 = 손목이 기준보다 위

        if (!curlIsUp && delta > CURL_THRESHOLD) {
            curlIsUp = true
        } else if (curlIsUp && delta < CURL_THRESHOLD/ 2f) {
            curlIsUp = false
            onCurlRep()
        }
    }

    private fun detectWristRotation(pts: List<Pair<Float, Float>>) {
        //검지 MCP(5) - 새끼 MCP(17) X 차이
        val xDiff = pts[5].first - pts[17].first

        val isBack  = xDiff < -WRIST_THRESHOLD // 손등이 카메라 방향
        val isFront = xDiff > WRIST_THRESHOLD  // 손등이 카메라 방향

        if (isBack && !wristHasGoneBack) {
            wristHasGoneBack = true
        } else if (isFront && wristHasGoneBack) {
            wristHasGoneBack = false
            onWristRep()
        }
    }
}
