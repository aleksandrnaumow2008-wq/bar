package com.bar.trainer.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bar.trainer.data.IngredientItem
import com.bar.trainer.ui.theme.*

@Composable
fun FrostedCard(
    modifier: Modifier = Modifier,
    glowColor: Color = Color.Transparent,
    cornerRadius: Dp = 20.dp,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    val shape = RoundedCornerShape(cornerRadius)
    val baseModifier = modifier
        .then(
            if (glowColor != Color.Transparent) {
                Modifier.shadow(
                    elevation = 16.dp,
                    shape = shape,
                    ambientColor = glowColor.copy(alpha = 0.35f),
                    spotColor = glowColor.copy(alpha = 0.45f)
                )
            } else Modifier
        )
        .clip(shape)
        .background(
            brush = Brush.verticalGradient(
                colors = listOf(
                    Color(0x26FFFFFF),
                    Color(0x0DFFFFFF)
                )
            )
        )
        .border(
            width = 1.dp,
            brush = Brush.linearGradient(
                colors = listOf(
                    if (glowColor != Color.Transparent) glowColor.copy(alpha = 0.6f) else FrostedGlassBorder,
                    Color(0x14FFFFFF)
                )
            ),
            shape = shape
        )
        .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier)
        .padding(16.dp)

    Column(modifier = baseModifier, content = content)
}

@Composable
fun NeonBadge(
    text: String,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(accentColor.copy(alpha = 0.16f))
            .border(1.dp, accentColor.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
            .padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        Text(
            text = text.uppercase(),
            color = accentColor,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.6.sp
        )
    }
}

@Composable
fun CocktailGlassVisualizer(
    ingredients: List<IngredientItem>,
    modifier: Modifier = Modifier
) {
    val fillProgress by animateFloatAsState(
        targetValue = if (ingredients.isEmpty()) 0f else 1f,
        animationSpec = tween(durationMillis = 500),
        label = "liquid_fill"
    )

    Canvas(modifier = modifier.size(width = 110.dp, height = 150.dp)) {
        val w = size.width
        val h = size.height
        val topInset = w * 0.12f
        val bottomInset = w * 0.24f
        val glassBottom = h * 0.90f
        val glassTop = h * 0.12f
        val liquidMaxHeight = (glassBottom - glassTop) * 0.85f

        val glassPath = Path().apply {
            moveTo(topInset, glassTop)
            lineTo(w - topInset, glassTop)
            lineTo(w - bottomInset, glassBottom)
            lineTo(bottomInset, glassBottom)
            close()
        }

        if (ingredients.isNotEmpty()) {
            val layerHeight = (liquidMaxHeight / ingredients.size) * fillProgress
            var currentBottom = glassBottom - 4f

            ingredients.forEach { item ->
                val rawColor = Color(item.colorHex)
                val visibleColor = if (rawColor.alpha < 0.35f) {
                    rawColor.copy(alpha = 0.45f)
                } else {
                    rawColor.copy(alpha = 0.85f)
                }

                val progressFromBottom = (glassBottom - currentBottom) / (glassBottom - glassTop)
                val currentInset = bottomInset - (bottomInset - topInset) * progressFromBottom

                drawRect(
                    color = visibleColor,
                    topLeft = Offset(currentInset + 4f, currentBottom - layerHeight),
                    size = Size(w - (currentInset + 4f) * 2, layerHeight)
                )
                currentBottom -= layerHeight
            }
        }

        drawPath(
            path = glassPath,
            color = Color.White.copy(alpha = 0.65f),
            style = Stroke(width = 3.dp.toPx())
        )
        drawLine(
            color = Color.White.copy(alpha = 0.25f),
            start = Offset(topInset + 10f, glassTop + 12f),
            end = Offset(bottomInset + 8f, glassBottom - 16f),
            strokeWidth = 2.dp.toPx()
        )
    }
}
