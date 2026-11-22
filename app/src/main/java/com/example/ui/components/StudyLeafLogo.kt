package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.ui.theme.EmeraldBright
import com.example.ui.theme.EmeraldDeep
import com.example.ui.theme.EmeraldLight
import com.example.ui.theme.PureWhite

@Composable
fun StudyLeafLogoMark(
    modifier: Modifier = Modifier,
    size: Dp = 40.dp
) {
    val cdLogo = stringResource(R.string.cd_app_logo)
    Box(
        modifier = modifier
            .size(size)
            .clip(RoundedCornerShape(14.dp))
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(EmeraldBright, EmeraldDeep)
                )
            )
            .semantics { contentDescription = cdLogo },
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(size * 0.68f)) {
            val w = this.size.width
            val h = this.size.height

            // Open book left & right curved pages
            val bookPath = Path().apply {
                moveTo(w * 0.50f, h * 0.78f)
                cubicTo(w * 0.34f, h * 0.68f, w * 0.16f, h * 0.70f, w * 0.08f, h * 0.76f)
                lineTo(w * 0.08f, h * 0.34f)
                cubicTo(w * 0.18f, h * 0.28f, w * 0.36f, h * 0.30f, w * 0.50f, h * 0.42f)
                cubicTo(w * 0.64f, h * 0.30f, w * 0.82f, h * 0.28f, w * 0.92f, h * 0.34f)
                lineTo(w * 0.92f, h * 0.76f)
                cubicTo(w * 0.84f, h * 0.70f, w * 0.66f, h * 0.68f, w * 0.50f, h * 0.78f)
                close()
            }
            drawPath(
                path = bookPath,
                color = PureWhite,
                style = Stroke(
                    width = w * 0.09f,
                    cap = StrokeCap.Round,
                    join = StrokeJoin.Round
                )
            )

            // Sprouting leaf rising from center spine
            val leafPath = Path().apply {
                moveTo(w * 0.50f, h * 0.46f)
                cubicTo(w * 0.48f, h * 0.24f, w * 0.62f, h * 0.10f, w * 0.76f, h * 0.10f)
                cubicTo(w * 0.76f, h * 0.26f, w * 0.66f, h * 0.40f, w * 0.50f, h * 0.46f)
                close()
            }
            drawPath(
                path = leafPath,
                color = EmeraldLight
            )

            // Center stem vein
            drawLine(
                color = PureWhite,
                start = Offset(w * 0.50f, h * 0.76f),
                end = Offset(w * 0.50f, h * 0.44f),
                strokeWidth = w * 0.08f,
                cap = StrokeCap.Round
            )
        }
    }
}

@Composable
fun StudyLeafBrandHeader(
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    inverseColors: Boolean = false
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        StudyLeafLogoMark(size = 42.dp)
        Column {
            Text(
                text = stringResource(R.string.app_name),
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                color = if (inverseColors) PureWhite else MaterialTheme.colorScheme.onSurface
            )
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.labelMedium,
                    color = if (inverseColors) Color(0xFFD1FAE5) else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
