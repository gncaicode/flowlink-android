package com.gncaitech.flowlink.ml

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Matrix
import android.os.SystemClock
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
    private val exerciseKind: String = "grip",
) {
    private var handLandmarker: HandLandmarker? = null
    private var smoothWristY = -1f
    private var curlIsUp = false
    private var wristHasGoneBack = false
    private val WRIST_THRESHOLD  = 0.10f
    private var CURL_THRESHOLD   = 0.15f

    // 히스테리시스 — 굽혀진 손가락 개수 기준
    // 3개 이상 → 쥠 / 1개 이하 → 폄 / 2개 → 이전 상태 유지
    private var handIsClosedState = false

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
                val hands = result.landmarks().map { hand ->
                    hand.map { lm -> Pair(lm.x(), lm.y()) }
                }
                onResult(hands)

                // 그립 감지 - 첫 번째 손만 사용
                val hand = result.landmarks().firstOrNull()
                if (hand != null && hand.size == 21) {
                    val pts      = hand.map { Pair(it.x(), it.y()) }
                    val pts3D    = hand.map { Triple(it.x(), it.y(), it.z()) }
                    val closed   = isHandClosed(pts3D)
                    val grip     = calcGripPercent(pts3D)
                    val xDiff    = pts[5].first - pts[17].first

                    onGrip(closed)
                    onGripPercent(grip)
                    onLandmarks3D(pts3D)
                    onHandFrame?.invoke(HandFrameData(pts3D, grip, closed, xDiff))

                    if (exerciseKind == "dumbbell") detectCurl(pts)
                    if (exerciseKind == "wrist_rotation") detectWristRotation(pts)
                }
            }
            .setErrorListener{ error -> error.printStackTrace() }
            .build()

        handLandmarker = HandLandmarker.createFromOptions(context, options)
    }

    fun detect(imageProxy: ImageProxy) {
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
        for (i in fingerTips.indices) {
            val distTip = dist3D(wrist, pts3D[fingerTips[i]])
            val distMcp = dist3D(wrist, pts3D[fingerMcps[i]])
            if (distTip < distMcp) closedCount++
        }
        handIsClosedState = when {
            closedCount >= 3 -> true
            closedCount <= 1 -> false
            else             -> handIsClosedState  // 2개: 이전 상태 유지
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
