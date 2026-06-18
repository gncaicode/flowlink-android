package com.gncaitech.flowlink.ml

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Matrix
import android.os.SystemClock
import androidx.camera.core.ImageProxy
import com.google.mediapipe.framework.image.BitmapImageBuilder
import com.google.mediapipe.tasks.core.BaseOptions
import com.google.mediapipe.tasks.vision.core.RunningMode
import com.google.mediapipe.tasks.vision.poselandmarker.PoseLandmarker
import com.google.mediapipe.tasks.vision.poselandmarker.PoseLandmarkerResult

// 팔꿈치 각도로 덤벨컬 1회를 감지합니다.
// 어깨(11.12) -> 팔꿈치(13/14) -> 손목(15/16) 세 점으로 각도 계산
// 각도가 CURL_UP_ANGLE 이하로 내려가면 "올림", CURL_DOWN_ANGLE 이상으로 올라가면 "내림" = 1회
data class CurlDebugInfo(
    val angle: Float,
    val curlIsUp: Boolean,
    val visibilityShoulder: Float,
    val visibilityElbow: Float,
    val visibilityWrist: Float,
)

data class PoseFrameData(
    val landmarks3D: List<Triple<Float, Float, Float>>,
    val elbowAngle: Float,
    val curlIsUp: Boolean,
    val visibilityShoulder: Float,
    val visibilityElbow: Float,
    val visibilityWrist: Float,
)

class PoseLandmarkDetector(
    context: Context,
    private val onCurlRep: () -> Unit = {},
    private val onLandmarks: (List<Pair<Float,Float>>) -> Unit = {},
    private val onLandmarks3D: (List<Triple<Float,Float,Float>>) -> Unit = {},
    private val onPoseFrame: ((PoseFrameData) -> Unit)? = null,
    private val onDebugInfo: ((CurlDebugInfo) -> Unit)? = null,
    private val onLog: ((String) -> Unit)? = null,
) {
    @Volatile private var closed = false
    private var poseLandmarker: PoseLandmarker? = null
    private var curlIsUp = false
    private var repCount = 0
    private var frameCount = 0

    // 오른팔(true): 12,14,16 / 왼팔(false): 11,13,15
    @Volatile var useRightArm: Boolean = true

    // 팔꿈치 각도 기준 (도 단위)
    private val CURL_UP_ANGLE = 100f        // 이 각도 이하 = 완전히 올린 상태
    private val CURL_DOWN_ANGLE = 130f     // 이 각도 이상 = 완전히 내린 상태

    init {
        val baseOptions = BaseOptions.builder()
            .setModelAssetPath("pose_landmarker_lite.task")
            .build()

        val options = PoseLandmarker.PoseLandmarkerOptions.builder()
            .setBaseOptions(baseOptions)
            .setNumPoses(1)
            .setMinPoseDetectionConfidence(0.5f)
            .setMinPosePresenceConfidence(0.5f)
            .setMinTrackingConfidence(0.5f)
            .setRunningMode(RunningMode.LIVE_STREAM)
            .setResultListener { result: PoseLandmarkerResult, _ ->
                if (closed) return@setResultListener

                val pose = result.landmarks().firstOrNull()
                if (pose == null || pose.size < 17) return@setResultListener

                val pose3D = result.worldLandmarks().firstOrNull()
                val using3D = pose3D != null && pose3D.size >= 17

                val indices  = if (useRightArm) listOf(12, 14, 16) else listOf(11, 13, 15)
                val visibilities = indices.map { i -> (pose[i].visibility().orElse(0f) ?: 0f) }
                val vS = visibilities[0]; val vE = visibilities[1]; val vW = visibilities[2]
                val armVisible = visibilities.all { it > 0.3f }

                if (!armVisible) {
                    onLandmarks(emptyList()); onLandmarks3D(emptyList())
                    return@setResultListener
                }

                onLandmarks(pose.map { Pair(it.x(), it.y()) })
                onLandmarks3D(pose.map { Triple(it.x(), it.y(), it.z()) })

                val shoulder2D = Pair(pose[indices[0]].x(), pose[indices[0]].y())
                val elbow2D    = Pair(pose[indices[1]].x(), pose[indices[1]].y())
                val wrist2D    = Pair(pose[indices[2]].x(), pose[indices[2]].y())

                val angle2D = calcAngle(shoulder2D, elbow2D, wrist2D)
                val angle = if (using3D) {
                    calcAngle3D(
                        Triple(pose3D!![indices[0]].x(), pose3D[indices[0]].y(), pose3D[indices[0]].z()),
                        Triple(pose3D[indices[1]].x(), pose3D[indices[1]].y(), pose3D[indices[1]].z()),
                        Triple(pose3D[indices[2]].x(), pose3D[indices[2]].y(), pose3D[indices[2]].z()),
                    )
                } else {
                    angle2D
                }

                val pts3D = pose.map { Triple(it.x(), it.y(), it.z()) }

                onPoseFrame?.invoke(PoseFrameData(
                    landmarks3D = pts3D, elbowAngle = angle, curlIsUp = curlIsUp,
                    visibilityShoulder = vS, visibilityElbow = vE, visibilityWrist = vW,
                ))
                onDebugInfo?.invoke(CurlDebugInfo(
                    angle = angle, curlIsUp = curlIsUp,
                    visibilityShoulder = vS, visibilityElbow = vE, visibilityWrist = vW,
                ))

                detectCurl(angle)
            }
            .setErrorListener { error -> error.printStackTrace() }
            .build()

        poseLandmarker = PoseLandmarker.createFromOptions(context, options)
    }

    fun detect(imageProxy: ImageProxy) {
        if (closed) { imageProxy.close(); return }
        val bitmap = imageProxy.toBitmap()
        val rotationDegrees = imageProxy.imageInfo.rotationDegrees
        val rotatedBitmap = if (rotationDegrees != 0) {
            val matrix = Matrix().apply { postRotate(rotationDegrees.toFloat()) }
            Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height,matrix, true)
        } else {
            bitmap
        }
        val mpImage = BitmapImageBuilder(rotatedBitmap).build()
        poseLandmarker?.detectAsync(mpImage, SystemClock.uptimeMillis())
        imageProxy.close()
    }

    fun close() {
        closed = true
        poseLandmarker?.close()
        poseLandmarker = null
    }

    // 2D 각도 계산 — 폴백용
    private fun calcAngle(a: Pair<Float,Float>, b: Pair<Float,Float>, c: Pair<Float,Float>): Float {
        val ab = Pair(a.first - b.first, a.second - b.second)
        val cb = Pair(c.first - b.first, c.second - b.second)
        val dot = ab.first * cb.first + ab.second * cb.second
        val magAb = Math.sqrt((ab.first * ab.first + ab.second * ab.second).toDouble())
        val magCb = Math.sqrt((cb.first * cb.first + cb.second * cb.second).toDouble())
        if (magAb == 0.0 || magCb == 0.0) return 0f
        return Math.toDegrees(Math.acos((dot / (magAb * magCb)).coerceIn(-1.0, 1.0))).toFloat()
    }

    // 3D 각도 계산 — worldLandmarks 사용, 카메라 각도 무관하게 정확
    private fun calcAngle3D(a: Triple<Float,Float,Float>, b: Triple<Float,Float,Float>, c: Triple<Float,Float,Float>): Float {
        val ab = Triple(a.first-b.first, a.second-b.second, a.third-b.third)
        val cb = Triple(c.first-b.first, c.second-b.second, c.third-b.third)
        val dot = ab.first*cb.first + ab.second*cb.second + ab.third*cb.third
        val magAb = Math.sqrt((ab.first*ab.first + ab.second*ab.second + ab.third*ab.third).toDouble())
        val magCb = Math.sqrt((cb.first*cb.first + cb.second*cb.second + cb.third*cb.third).toDouble())
        if (magAb == 0.0 || magCb == 0.0) return 0f
        return Math.toDegrees(Math.acos((dot / (magAb * magCb)).coerceIn(-1.0, 1.0))).toFloat()
    }

    private fun detectCurl(angle: Float) {
        frameCount++
        onLog?.invoke("CurlPV: angle=${"%.1f".format(angle)} curlIsUp=$curlIsUp")

        if (!curlIsUp && angle < CURL_UP_ANGLE) {
            curlIsUp = true
            onLog?.invoke("CurlPV: ▲ 올림 angle=${"%.1f".format(angle)} (< CURL_UP=$CURL_UP_ANGLE)")
        } else if (curlIsUp && angle > CURL_DOWN_ANGLE) {
            curlIsUp = false
            repCount++
            onLog?.invoke("CurlPV: ▼ 내림 → ★ REP #$repCount angle=${"%.1f".format(angle)} (> CURL_DOWN=$CURL_DOWN_ANGLE)")
            onCurlRep()
        }

        if (frameCount % 30 == 0) {
            onLog?.invoke("CurlPV: ◆상태[$frameCount] curlIsUp=$curlIsUp angle=${"%.1f".format(angle)} reps=$repCount")
        }
    }

}
