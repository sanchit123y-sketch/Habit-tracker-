package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
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
fun AiTeacherAvatarIcon(
    modifier: Modifier = Modifier,
    size: Dp = 46.dp
) {
    val cd = stringResource(R.string.cd_ai_teacher_icon)
    Box(
        modifier = modifier
            .size(size)
            .clip(RoundedCornerShape(16.dp))
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(EmeraldBright, EmeraldDeep)
                )
            )
            .semantics { contentDescription = cd },
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(size * 0.72f)) {
            val w = this.size.width
            val h = this.size.height

            // Friendly round scholar face / badge
            drawCircle(
                color = PureWhite.copy(alpha = 0.22f),
                radius = w * 0.30f,
                center = Offset(w * 0.50f, h * 0.60f)
            )
            drawCircle(
                color = PureWhite,
                radius = w * 0.28f,
                center = Offset(w * 0.50f, h * 0.60f),
                style = Stroke(width = w * 0.075f)
            )

            // Friendly eyes
            drawCircle(
                color = PureWhite,
                radius = w * 0.036f,
                center = Offset(w * 0.41f, h * 0.58f)
            )
            drawCircle(
                color = PureWhite,
                radius = w * 0.036f,
                center = Offset(w * 0.59f, h * 0.58f)
            )

            // Warm gentle smile
            val smilePath = Path().apply {
                moveTo(w * 0.42f, h * 0.67f)
                quadraticTo(w * 0.50f, h * 0.74f, w * 0.58f, h * 0.67f)
            }
            drawPath(
                path = smilePath,
                color = PureWhite,
                style = Stroke(width = w * 0.06f, cap = StrokeCap.Round)
            )

            // Scholar mortarboard cap on top
            val capPath = Path().apply {
                moveTo(w * 0.50f, h * 0.12f)
                lineTo(w * 0.86f, h * 0.27f)
                lineTo(w * 0.50f, h * 0.40f)
                lineTo(w * 0.14f, h * 0.27f)
                close()
            }
            drawPath(
                path = capPath,
                color = EmeraldLight
            )
            drawPath(
                path = capPath,
                color = PureWhite,
                style = Stroke(
                    width = w * 0.055f,
                    cap = StrokeCap.Round,
                    join = StrokeJoin.Round
                )
            )

            // Tassel & leaf sparkle accent
            drawLine(
                color = Color(0xFFFDE047),
                start = Offset(w * 0.80f, h * 0.29f),
                end = Offset(w * 0.80f, h * 0.47f),
                strokeWidth = w * 0.055f,
                cap = StrokeCap.Round
            )
            drawRoundRect(
                color = Color(0xFFFDE047),
                topLeft = Offset(w * 0.76f, h * 0.45f),
                size = Size(w * 0.08f, h * 0.09f),
                cornerRadius = CornerRadius(w * 0.04f, w * 0.04f)
            )
        }
    }
}

@Composable
fun AiSetupRequiredBanner(
    message: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("ai_setup_required_banner"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFFFFBEB)
        ),
        border = BorderStroke(1.dp, Color(0xFFF59E0B).copy(alpha = 0.55f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.Top
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(0xFFF59E0B).copy(alpha = 0.16f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Rounded.Info,
                    contentDescription = stringResource(R.string.ai_teacher_setup_title),
                    tint = Color(0xFFB45309),
                    modifier = Modifier.size(20.dp)
                )
            }
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = stringResource(R.string.ai_teacher_setup_title),
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = Color(0xFF78350F)
                )
                Text(
                    text = message.ifBlank { stringResource(R.string.ai_teacher_setup_body) },
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF92400E)
                )
            }
        }
    }
}
