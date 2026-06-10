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
class PoseLandmarkDetector(
    context: Context,
    private val onCurlRep: () -> Unit = {},
    private val onLandmarks: (List<Pair<Float,Float>>) -> Unit = {},
) {
    private var poseLandmarker: PoseLandmarker? = null
    private var curlIsUp = false

    // 팔꿈치 각도 기준 (도 단위)
    private val CURL_UP_ANGLE = 70f         // 이 각도 이하 = 완전히 올린 상태
    private val CURL_DOWN_ANGLE = 150f     // 이 각도 이상 = 완전히 내린 상태

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
                val pose = result.landmarks().firstOrNull() ?: return@setResultListener
                if (pose.size < 17) return@setResultListener

                // 2D landmarks 전달 (화면 오버레이용)
                onLandmarks(pose.map { Pair(it.x(),it.y()) })

                // 오른팔 기준: 어깨(12), 팔꿈치(14), 손목(16)
                val shoulder    = Pair(pose[12].x(), pose[12].y())
                val elbow       = Pair(pose[14].x(), pose[14].y())
                val wrist       = Pair(pose[16].x(), pose[16].y())

                val angle = calcAngle(shoulder, elbow, wrist)
                detectCurl(angle)
            }
            .setErrorListener { error -> error.printStackTrace() }
            .build()

        poseLandmarker = PoseLandmarker.createFromOptions(context, options)
    }

    fun detect(imageProxy: ImageProxy) {
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
        poseLandmarker?.close()
        poseLandmarker = null
    }

    // 세 점(A->B->C)의 B에서의 각도 계산 (도 단위)
    private fun calcAngle(a: Pair<Float,Float>,b:Pair<Float,Float>,c:Pair<Float,Float>):Float {
        val ab = Pair(a.first - b.first, a.second - b.second)
        val cb = Pair(c.first - b.first, c.second - b.second)
        val dot = ab.first * cb.first + ab.second * cb.second
        val magAb = Math.sqrt((ab.first * ab.first + ab.second * ab.second).toDouble())
        val magCb = Math.sqrt((cb.first * cb.first + cb.second * cb.second).toDouble())
        if (magAb == 0.0 || magCb == 0.0) return 0f
        val cosAngle = (dot / (magAb * magCb)).coerceIn(-1.0,1.0)
        return Math.toDegrees(Math.acos(cosAngle)).toFloat()
    }

    private fun detectCurl(angle: Float) {
        if (!curlIsUp && angle < CURL_UP_ANGLE) {
            curlIsUp = true
        } else if (curlIsUp && angle > CURL_DOWN_ANGLE) {
            curlIsUp = false
            onCurlRep()
        }
    }

}