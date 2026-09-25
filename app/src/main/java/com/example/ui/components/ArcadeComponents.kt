package com.example.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.DarkPill
import com.example.ui.theme.FredokaFontFamily
import com.example.ui.theme.PrimaryCyan
import com.example.ui.theme.PureWhite

/**
 * Chunky, tactile arcade pill button replicating the physical press-down feel
 * of the 1 2 3 4 Player Games design system.
 */
@Composable
fun ArcadeButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    backgroundColor: Color = PrimaryCyan,
    textColor: Color = PureWhite,
    shadowColor: Color = Color(0x33000000),
    enabled: Boolean = true,
    height: Dp = 64.dp,
    testTag: String = "arcade_button"
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val pressOffset by animateFloatAsState(
        targetValue = if (isPressed && enabled) 4f else 0f,
        label = "pressOffset"
    )

    val effectiveBg = if (enabled) backgroundColor else Color(0xFFBDC3C7)
    val effectiveText = if (enabled) textColor else Color(0xFF7F8C8D)

    Box(
        modifier = modifier
            .testTag(testTag)
            .height(height + 4.dp),
        contentAlignment = Alignment.TopCenter
    ) {
        // Shadow/Base layer
        if (enabled) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(height)
                    .offset(y = 4.dp)
                    .clip(RoundedCornerShape(32.dp))
                    .background(shadowColor)
            )
        }

        // Action surface layer (moves down on press)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(height)
                .offset(y = pressOffset.dp)
                .clip(RoundedCornerShape(32.dp))
                .background(effectiveBg)
                .clickable(
                    interactionSource = interactionSource,
                    indication = null,
                    enabled = enabled,
                    onClick = onClick
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = text,
                color = effectiveText,
                fontFamily = FredokaFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 22.sp,
                letterSpacing = 1.sp,
                textAlign = TextAlign.Center
            )
        }
    }
}

/**
 * Diagonal-striped dark card used in Pass-and-Play screen
 */
@Composable
fun DiagonalStripedCard(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(0.dp))
            .background(Color(0xFF22242A)),
        contentAlignment = Alignment.Center
    ) {
        // Draw diagonal arcade stripes
        Canvas(modifier = Modifier.fillMaxSize()) {
            val stripeWidth = 14f
            val spacing = 28f
            val width = size.width
            val height = size.height
            val color = Color(0xFF1B1D22)

            var x = -height
            while (x < width + height) {
                drawLine(
                    color = color,
                    start = Offset(x, 0f),
                    end = Offset(x + height, height),
                    strokeWidth = stripeWidth
                )
                x += spacing
            }
        }

        content()
    }
}

/**
 * Question Mark subtle watermarked pattern for Navy background screens
 */
@Composable
fun QuestionMarkWatermarkBackground(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    Box(modifier = modifier.background(Color(0xFF2C3359))) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val questionPositions = listOf(
                Offset(size.width * 0.15f, size.height * 0.12f),
                Offset(size.width * 0.85f, size.height * 0.18f),
                Offset(size.width * 0.50f, size.height * 0.35f),
                Offset(size.width * 0.18f, size.height * 0.55f),
                Offset(size.width * 0.82f, size.height * 0.65f),
                Offset(size.width * 0.45f, size.height * 0.82f),
                Offset(size.width * 0.88f, size.height * 0.92f)
            )

            for (pos in questionPositions) {
                // Subtle arcade question marks
                drawCircle(
                    color = Color(0x0EFFFFFF),
                    radius = 36f,
                    center = pos
                )
            }
        }
        content()
    }
}

/**
 * Fedora and Sunglasses vector illustration for Impostor reveal and icons
 */
@Composable
fun SpyFedoraIllustration(
    modifier: Modifier = Modifier,
    hatColor: Color = Color(0xFF5A589C),
    glassesColor: Color = Color(0xFFFF5964)
) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height

        // Hat crown
        val crownPath = Path().apply {
            moveTo(w * 0.30f, h * 0.50f)
            cubicTo(w * 0.32f, h * 0.20f, w * 0.45f, h * 0.12f, w * 0.50f, h * 0.12f)
            cubicTo(w * 0.55f, h * 0.12f, w * 0.68f, h * 0.20f, w * 0.70f, h * 0.50f)
            close()
        }
        drawPath(crownPath, hatColor)

        // Hat brim
        drawOval(
            color = Color(0xFF45427D),
            topLeft = Offset(w * 0.10f, h * 0.42f),
            size = androidx.compose.ui.geometry.Size(w * 0.80f, h * 0.24f)
        )

        // Sunglasses lenses
        val lensY = h * 0.64f
        val lensRadius = w * 0.13f
        drawCircle(
            color = glassesColor,
            radius = lensRadius,
            center = Offset(w * 0.36f, lensY)
        )
        drawCircle(
            color = glassesColor,
            radius = lensRadius,
            center = Offset(w * 0.64f, lensY)
        )

        // Glasses bridge
        drawRect(
            color = glassesColor,
            topLeft = Offset(w * 0.44f, lensY - 4f),
            size = androidx.compose.ui.geometry.Size(w * 0.12f, 10f)
        )

        // Reflection glint
        drawCircle(
            color = Color.White.copy(alpha = 0.5f),
            radius = lensRadius * 0.35f,
            center = Offset(w * 0.34f, lensY - 8f)
        )
        drawCircle(
            color = Color.White.copy(alpha = 0.5f),
            radius = lensRadius * 0.35f,
            center = Offset(w * 0.62f, lensY - 8f)
        )
    }
}

/**
 * Authentic Spy Detective Silhouette with White Collar matching Screenshot 8
 */
@Composable
fun SpySilhouetteWithCollar(
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height

        // Dark silhouette of head and shoulders
        val bodyColor = Color(0xFF1B1F38)

        // Shoulders / Coat
        drawOval(
            color = bodyColor,
            topLeft = Offset(w * 0.10f, h * 0.55f),
            size = androidx.compose.ui.geometry.Size(w * 0.80f, h * 0.65f)
        )

        // Head
        drawCircle(
            color = bodyColor,
            radius = w * 0.26f,
            center = Offset(w * 0.50f, h * 0.38f)
        )

        // Hair spikes silhouette
        val hairPath = Path().apply {
            moveTo(w * 0.25f, h * 0.30f)
            lineTo(w * 0.18f, h * 0.22f)
            lineTo(w * 0.32f, h * 0.20f)
            lineTo(w * 0.38f, h * 0.12f)
            lineTo(w * 0.52f, h * 0.15f)
            lineTo(w * 0.65f, h * 0.14f)
            lineTo(w * 0.76f, h * 0.24f)
            lineTo(w * 0.70f, h * 0.36f)
            close()
        }
        drawPath(hairPath, bodyColor)

        // White suit collar triangles
        val leftCollar = Path().apply {
            moveTo(w * 0.42f, h * 0.58f)
            lineTo(w * 0.34f, h * 0.88f)
            lineTo(w * 0.45f, h * 0.80f)
            close()
        }
        drawPath(leftCollar, PureWhite)

        val rightCollar = Path().apply {
            moveTo(w * 0.58f, h * 0.58f)
            lineTo(w * 0.66f, h * 0.88f)
            lineTo(w * 0.55f, h * 0.80f)
            close()
        }
        drawPath(rightCollar, PureWhite)

        // Dark glasses on silhouette
        val glassColor = Color(0xFF282D4E)
        drawCircle(
            color = glassColor,
            radius = w * 0.11f,
            center = Offset(w * 0.39f, h * 0.38f)
        )
        drawCircle(
            color = glassColor,
            radius = w * 0.11f,
            center = Offset(w * 0.61f, h * 0.38f)
        )
    }
}

/**
 * Tilted Red Rubber Stamp "EXPOSED" matching Screenshot 8
 */
@Composable
fun ExposedStampGraphic(
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .rotate(-12f)
            .border(
                width = 6.dp,
                color = Color(0xFFE63946),
                shape = RoundedCornerShape(12.dp)
            )
            .padding(horizontal = 24.dp, vertical = 8.dp)
            .background(Color(0x1AE63946), RoundedCornerShape(8.dp)),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "EXPOSED",
            color = Color(0xFFE63946),
            fontFamily = FredokaFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 38.sp,
            letterSpacing = 4.sp
        )
    }
}

/**
 * Top Back Navigation button in circular white card with left arrow
 */
@Composable
fun CircularBackButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    testTag: String = "back_button"
) {
    Box(
        modifier = modifier
            .testTag(testTag)
            .size(48.dp)
            .clip(CircleShape)
            .background(PureWhite)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        // Chunky left chevron
        Canvas(modifier = Modifier.size(20.dp)) {
            val stroke = 4.dp.toPx()
            val path = Path().apply {
                moveTo(size.width * 0.65f, size.height * 0.15f)
                lineTo(size.width * 0.25f, size.height * 0.50f)
                lineTo(size.width * 0.65f, size.height * 0.85f)
            }
            drawPath(
                path = path,
                color = Color(0xFF2C3359),
                style = Stroke(width = stroke, cap = androidx.compose.ui.graphics.StrokeCap.Round, join = androidx.compose.ui.graphics.StrokeJoin.Round)
            )
        }
    }
}
