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

class HandLandmarkDetector(
    context: Context,
    private val onResult: (List<List<Pair<Float,Float>>>) -> Unit,
    private val onGrip: (isClosed: Boolean) -> Unit = {},
) {
    private var handLandmarker: HandLandmarker? = null

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
                if (hand != null && hand.size == 21 ) {
                    val closed = isHandClosed(hand.map { Pair(it.x(), it.y()) })
                    onGrip(closed)
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

    // 손가락 4개(검지~새끼)의 끝(TIP)이 중간마디(PIP)보다 y값이 크면 구부러진 것
    // (y 좌표: 위=0, 아래=1 이므로 TIP.y > PIP.y = 손끝이 아래 = 구부러짐)
    private fun isHandClosed(pts: List<Pair<Float, Float>>): Boolean {
        // 검지(8>6), 중지(12>10), 약지(16>14), 새끼(20>18)
        val fingerTips = listOf(8, 12, 16, 20)
        val fingerPips = listOf(6, 10, 14, 18)
        var closedCount = 0
        for (i in fingerTips.indices) {
            if (pts[fingerTips[i]].second > pts[fingerPips[i]].second) closedCount++
        }
        return closedCount >= 3  // 4개 중 3개 이상 구부러지면 "쥔 상태"
    }

}