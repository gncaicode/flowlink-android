package com.gncaitech.flowlink.ui.screens

import android.net.Uri
import android.widget.MediaController
import android.widget.VideoView
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.gncaitech.flowlink.R
import com.gncaitech.flowlink.ui.theme.MontserratFamily

@Composable
fun GuideVideoScreen(
    kind: String,
    onBack: () -> Unit = {},
) {
    val context = LocalContext.current
    val title = when (kind) {
        "dumbbell"       -> "덤벨컬 가이드"
        "grip"           -> "공쥐기 가이드"
        "wrist_rotation" -> "손목회전 가이드"
        else             -> "운동 가이드"
    }

    val rawResId: Int? = when (kind) {
        "dumbbell" -> R.raw.dumbbell_curl
        "grip"     -> R.raw.ball_squeeze
        else       -> null  // 손목회전 — 영상 준비 중
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        if (rawResId != null) {
            val videoView = remember { VideoView(context) }

            AndroidView(
                factory = { videoView },
                modifier = Modifier.fillMaxSize(),
            ) { vv ->
                val uri = Uri.parse("android.resource://${context.packageName}/$rawResId")
                vv.setVideoURI(uri)
                val mc = MediaController(context)
                mc.setAnchorView(vv)
                vv.setMediaController(mc)
                vv.setOnPreparedListener { mp ->
                    mp.isLooping = true
                    vv.start()
                }
            }

            DisposableEffect(Unit) {
                onDispose { videoView.stopPlayback() }
            }
        } else {
            // 영상 없음 — 안내 표시
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                androidx.compose.foundation.layout.Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.VideoLibrary,
                        contentDescription = null,
                        tint = Color.White.copy(alpha = 0.30f),
                        modifier = Modifier.size(64.dp)
                    )
                    Text(
                        "가이드 영상 준비 중입니다",
                        style = TextStyle(
                            fontFamily = MontserratFamily,
                            fontSize = 15.sp,
                            color = Color.White.copy(alpha = 0.50f)
                        )
                    )
                }
            }
        }

        // 뒤로 버튼
        Box(
            modifier = Modifier
                .padding(16.dp)
                .size(44.dp)
                .clip(CircleShape)
                .background(Color.Black.copy(alpha = 0.50f))
                .border(1.dp, Color.White.copy(alpha = 0.18f), CircleShape)
                .clickable(
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() }
                ) { onBack() },
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Default.ArrowBack, contentDescription = "뒤로",
                tint = Color.White, modifier = Modifier.size(20.dp))
        }

        // 타이틀
        Text(
            title,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 14.dp),
            style = TextStyle(
                fontWeight = FontWeight.SemiBold,
                fontSize = 15.sp,
                color = Color.White.copy(alpha = 0.85f)
            )
        )
    }
}
