package com.flowlink.app.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.flowlink.app.ui.theme.ArtRed
import com.flowlink.app.ui.theme.MedTeal
import com.flowlink.app.ui.theme.MontserratFamily
import com.flowlink.app.ui.theme.Navy

/**
 * FLSymbol — AVF Infinity Loop symbol drawn on Canvas.
 * ViewBox: 80x80. Scales proportionally from [size].
 */
@Composable
fun FLSymbol(
    modifier: Modifier = Modifier,
    size: Dp = 80.dp,
) {
    Canvas(modifier = modifier.size(size)) {
        val vb = 80f
        val scaleX = this.size.width / vb
        val scaleY = this.size.height / vb
        val scale = minOf(scaleX, scaleY)
        val cx = this.size.width / 2f
        val cy = this.size.height / 2f

        // Outer circle — ArtRed opacity 0.18, strokeWidth 4.5dp scaled
        drawCircle(
            color = ArtRed.copy(alpha = 0.18f),
            radius = 38f * scale,
            center = Offset(cx, cy),
            style = Stroke(width = 4.5f * scale)
        )

        // AVF infinity loop path
        // SVG: M18,40 C18,28 32,24 40,32 C48,40 52,44 62,40 C52,36 48,40 40,48 C32,56 18,52 18,40Z
        // Scaled from 80x80 viewBox
        val loopPath = Path().apply {
            // Map from 80x80 viewBox to canvas center
            val ox = cx - 40f * scale
            val oy = cy - 40f * scale
            fun sx(x: Float) = ox + x * scale
            fun sy(y: Float) = oy + y * scale

            moveTo(sx(18f), sy(40f))
            cubicTo(sx(18f), sy(28f), sx(32f), sy(24f), sx(40f), sy(32f))
            cubicTo(sx(48f), sy(40f), sx(52f), sy(44f), sx(62f), sy(40f))
            cubicTo(sx(52f), sy(36f), sx(48f), sy(40f), sx(40f), sy(48f))
            cubicTo(sx(32f), sy(56f), sx(18f), sy(52f), sx(18f), sy(40f))
            close()
        }
        drawPath(
            path = loopPath,
            color = ArtRed,
            style = Stroke(
                width = 3.5f * scale,
                cap = StrokeCap.Round,
                join = StrokeJoin.Round
            )
        )

        // Network line — Navy opacity 0.85, strokeWidth 1.5dp
        val netLineY = cy
        drawLine(
            color = Navy.copy(alpha = 0.85f),
            start = Offset(cx - 30f * scale, netLineY),
            end = Offset(cx + 30f * scale, netLineY),
            strokeWidth = 1.5f * scale
        )

        // 4 connector lines to center (Navy opacity 0.18)
        val connPoints = listOf(
            Offset(cx - 25f * scale, cy - 18f * scale),
            Offset(cx + 25f * scale, cy - 18f * scale),
            Offset(cx - 25f * scale, cy + 18f * scale),
            Offset(cx + 25f * scale, cy + 18f * scale),
        )
        for (pt in connPoints) {
            drawLine(
                color = Navy.copy(alpha = 0.18f),
                start = Offset(cx, cy),
                end = pt,
                strokeWidth = 1.2f * scale
            )
        }

        // Left/right endpoint dots — ArtRed, radius 2.5dp
        drawCircle(
            color = ArtRed,
            radius = 2.5f * scale,
            center = Offset(cx - 22f * scale, cy)
        )
        drawCircle(
            color = ArtRed,
            radius = 2.5f * scale,
            center = Offset(cx + 22f * scale, cy)
        )

        // Center dot — MedTeal, radius 4dp
        drawCircle(
            color = MedTeal,
            radius = 4f * scale,
            center = Offset(cx, cy)
        )

        // 4 corner satellite dots — Navy with varying opacity
        val satData = listOf(
            Triple(Offset(cx - 28f * scale, cy - 20f * scale), 0.4f, 2f),
            Triple(Offset(cx + 28f * scale, cy - 20f * scale), 0.4f, 2f),
            Triple(Offset(cx - 28f * scale, cy + 20f * scale), 0.25f, 2f),
            Triple(Offset(cx + 28f * scale, cy + 20f * scale), 0.25f, 2f),
        )
        for ((pos, alpha, radius) in satData) {
            drawCircle(
                color = Navy.copy(alpha = alpha),
                radius = radius * scale,
                center = pos
            )
        }
    }
}

/**
 * FLWordmark — "FLOW" (ArtRed, Montserrat Black) + "LINK" (Navy, Montserrat Regular)
 */
@Composable
fun FLWordmark(
    modifier: Modifier = Modifier,
    fontSize: androidx.compose.ui.unit.TextUnit = 20.sp,
) {
    val text = buildAnnotatedString {
        withStyle(
            SpanStyle(
                fontFamily = MontserratFamily,
                fontWeight = FontWeight.Black,
                color = ArtRed,
                fontSize = fontSize,
                letterSpacing = (-0.02f * fontSize.value).sp,
            )
        ) {
            append("FLOW")
        }
        withStyle(
            SpanStyle(
                fontFamily = MontserratFamily,
                fontWeight = FontWeight.Normal,
                color = Navy,
                fontSize = fontSize,
                letterSpacing = (0.04f * fontSize.value).sp,
            )
        ) {
            append("LINK")
        }
    }
    Text(text = text, modifier = modifier)
}

/**
 * FLCaption — Montserrat SemiBold 11sp, uppercase, letterSpacing 0.18em, MedTeal
 */
@Composable
fun FLCaption(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = MedTeal,
) {
    Text(
        text = text.uppercase(),
        modifier = modifier,
        style = TextStyle(
            fontFamily = MontserratFamily,
            fontWeight = FontWeight.SemiBold,
            fontSize = 11.sp,
            letterSpacing = (0.18f * 11f).sp,
            color = color,
        )
    )
}

/**
 * DotLinePattern — decorative background dot-network canvas for SplashScreen.
 * Drawn on 412×828 coordinate space, scaled to fill the canvas.
 */
@Composable
fun DotLinePattern(
    modifier: Modifier = Modifier,
    dotColor: Color = Color.White.copy(alpha = 0.35f),
    lineColor: Color = Color.White.copy(alpha = 0.12f),
) {
    val dots = listOf(
        Pair(30f, 80f), Pair(90f, 140f), Pair(160f, 80f), Pair(240f, 120f),
        Pair(320f, 90f), Pair(380f, 170f), Pair(50f, 260f), Pair(130f, 300f),
        Pair(210f, 250f), Pair(290f, 290f), Pair(360f, 260f), Pair(40f, 600f),
        Pair(120f, 660f), Pair(210f, 620f), Pair(290f, 680f), Pair(360f, 630f),
        Pair(80f, 740f), Pair(190f, 760f), Pair(280f, 730f), Pair(350f, 780f),
    )

    // Connection indices (nearby dots linked)
    val lines = listOf(
        0 to 1, 1 to 2, 2 to 3, 3 to 4, 4 to 5,
        0 to 6, 1 to 7, 2 to 8, 3 to 9, 4 to 10, 5 to 10,
        6 to 7, 7 to 8, 8 to 9, 9 to 10,
        11 to 12, 12 to 13, 13 to 14, 14 to 15,
        11 to 16, 12 to 17, 13 to 18, 14 to 19, 15 to 19,
        16 to 17, 17 to 18, 18 to 19,
        6 to 11, 7 to 12, 8 to 13, 9 to 14, 10 to 15,
    )

    Canvas(modifier = modifier) {
        val sw = this.size.width
        val sh = this.size.height
        val sx = sw / 412f
        val sy = sh / 828f

        // Draw lines
        for ((ai, bi) in lines) {
            val a = dots[ai]
            val b = dots[bi]
            drawLine(
                color = lineColor,
                start = Offset(a.first * sx, a.second * sy),
                end = Offset(b.first * sx, b.second * sy),
                strokeWidth = 1f,
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 6f))
            )
        }

        // Draw dots
        for ((x, y) in dots) {
            drawCircle(
                color = dotColor,
                radius = 3f,
                center = Offset(x * sx, y * sy)
            )
        }
    }
}
