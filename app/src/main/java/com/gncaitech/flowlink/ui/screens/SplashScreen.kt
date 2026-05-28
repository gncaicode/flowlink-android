package com.gncaitech.flowlink.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gncaitech.flowlink.ui.components.DotLinePattern
import com.gncaitech.flowlink.ui.components.FLSymbol
import com.gncaitech.flowlink.ui.components.FLWordmark
import com.gncaitech.flowlink.ui.theme.ArtRed
import com.gncaitech.flowlink.ui.theme.MontserratFamily

private val SplashBackground = Color(0xFF0D2137)

@Composable
fun SplashScreen() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(SplashBackground)
    ) {
        // Dot-line network pattern background
        DotLinePattern(
            modifier = Modifier.fillMaxSize(),
            dotColor = Color.White.copy(alpha = 0.30f),
            lineColor = Color.White.copy(alpha = 0.10f),
        )

        // Top caption
        Text(
            text = "AVF VASCULAR MATURATION",
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 72.dp),
            style = TextStyle(
                fontFamily = MontserratFamily,
                fontWeight = FontWeight.Medium,
                fontSize = 10.sp,
                letterSpacing = (0.22f * 10f).sp,
                color = Color.White.copy(alpha = 0.45f),
            )
        )

        // Center content
        Column(
            modifier = Modifier.align(Alignment.Center),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Radial glow behind symbol — simulated with a large semi-transparent circle
            Box(contentAlignment = Alignment.Center) {
                Box(
                    modifier = Modifier
                        .size(220.dp)
                        .background(
                            brush = Brush.radialGradient(
                                colors = listOf(
                                    ArtRed.copy(alpha = 0.20f),
                                    Color.Transparent
                                )
                            )
                        )
                )
                FLSymbol(size = 148.dp)
            }

            Spacer(Modifier.height(24.dp))

            FLWordmark(fontSize = 52.sp)

            Spacer(Modifier.height(14.dp))

            Text(
                text = "완벽한 투석을 위한 과학적 연결".uppercase(),
                style = TextStyle(
                    fontFamily = MontserratFamily,
                    fontWeight = FontWeight.Medium,
                    fontSize = 12.sp,
                    letterSpacing = (0.22f * 12f).sp,
                    color = Color.White.copy(alpha = 0.55f),
                ),
                textAlign = TextAlign.Center,
            )
        }

        // Bottom version text
        Text(
            text = "v 1.0.0 · CLINICAL BUILD",
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 48.dp),
            style = TextStyle(
                fontFamily = MontserratFamily,
                fontWeight = FontWeight.Medium,
                fontSize = 10.sp,
                color = Color.White.copy(alpha = 0.32f),
                letterSpacing = 0.5.sp,
            )
        )
    }
}
