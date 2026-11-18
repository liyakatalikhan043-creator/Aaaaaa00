package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.HealthAndSafety
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
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
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.model.OperatorId
import com.example.model.WeaponId
import com.example.ui.theme.BunkerSurface
import com.example.ui.theme.CarbonPlate
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.HazardAmber
import com.example.ui.theme.ObsidianAsphalt
import com.example.ui.theme.SteelBorder
import com.example.ui.theme.TacticalMuted
import com.example.ui.theme.TacticalWhite
import com.example.ui.theme.ToxicAcidLime

@Composable
fun TacticalPanel(
    modifier: Modifier = Modifier,
    borderColor: Color = SteelBorder,
    backgroundColor: Color = CarbonPlate,
    content: @Composable ColumnScope.() -> Unit
) {
    Surface(
        modifier = modifier,
        shape = CutCornerShape(8.dp),
        color = backgroundColor,
        border = BorderStroke(1.dp, borderColor),
        tonalElevation = 4.dp
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            content = content
        )
    }
}

@Composable
fun TacticalStatBar(
    label: String,
    value: Float,
    maxValue: Float = 1f,
    barColor: Color = ToxicAcidLime,
    modifier: Modifier = Modifier,
    valueText: String? = null
) {
    val pct = (value / maxValue).coerceIn(0f, 1f)
    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = TacticalMuted
            )
            if (valueText != null) {
                Text(
                    text = valueText,
                    style = MaterialTheme.typography.labelSmall,
                    color = barColor,
                    fontWeight = FontWeight.Bold
                )
            }
        }
        Spacer(modifier = Modifier.height(3.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(ObsidianAsphalt)
                .border(0.5.dp, SteelBorder, RoundedCornerShape(2.dp))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(pct)
                    .height(6.dp)
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(barColor.copy(alpha = 0.65f), barColor)
                        )
                    )
            )
        }
    }
}

@Composable
fun OperatorAvatar(
    operator: OperatorId,
    size: Dp = 56.dp,
    modifier: Modifier = Modifier
) {
    val accent = Color(operator.accentHex)
    Box(
        modifier = modifier
            .size(size)
            .clip(CutCornerShape(6.dp))
            .background(BunkerSurface)
            .border(1.5.dp, accent, CutCornerShape(6.dp)),
        contentAlignment = Alignment.Center
    ) {
        if (operator.portraitRes != null) {
            Image(
                painter = painterResource(id = operator.portraitRes),
                contentDescription = "${operator.callsign} portrait",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        } else {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.radialGradient(
                            colors = listOf(accent.copy(alpha = 0.35f), ObsidianAsphalt)
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (operator == OperatorId.KESTREL) Icons.Filled.HealthAndSafety else Icons.Filled.Shield,
                    contentDescription = "${operator.callsign} emblem",
                    tint = accent,
                    modifier = Modifier.size(size * 0.52f)
                )
            }
        }
    }
}

@Composable
fun FirstPersonWeaponOverlay(
    weapon: WeaponId,
    isAds: Boolean,
    isReloading: Boolean,
    recoilKick: Float,
    muzzleFlash: Float,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height
        val kickPx = recoilKick * h * 0.45f
        val reloadDip = if (isReloading) h * 0.16f else 0f

        if (isAds) {
            // Realistic Holographic / ACOG Optic Housing centered on screen
            val cx = w * 0.5f
            val cy = h * 0.5f
            val radius = minOf(w, h) * 0.22f

            // Outer optic hood
            drawRoundRect(
                color = Color(0xD80A0D12),
                topLeft = Offset(cx - radius * 1.25f, cy - radius * 1.15f),
                size = Size(radius * 2.5f, radius * 2.3f),
                cornerRadius = CornerRadius(18f, 18f),
                style = Stroke(width = 24f)
            )
            // Glass tint
            drawCircle(
                color = CyberCyan.copy(alpha = 0.08f),
                radius = radius,
                center = Offset(cx, cy)
            )
            drawCircle(
                color = Color(0xFF2B3644),
                radius = radius,
                center = Offset(cx, cy),
                style = Stroke(width = 6f)
            )
            // Reticle
            drawCircle(color = ToxicAcidLime, radius = 4.5f, center = Offset(cx, cy))
            drawCircle(color = HazardAmber.copy(alpha = 0.7f), radius = 16f, center = Offset(cx, cy), style = Stroke(2f))
            drawLine(CyberCyan, Offset(cx - radius * 0.55f, cy), Offset(cx - 22f, cy), strokeWidth = 2f)
            drawLine(CyberCyan, Offset(cx + 22f, cy), Offset(cx + radius * 0.55f, cy), strokeWidth = 2f)
            drawLine(HazardAmber, Offset(cx, cy + 20f), Offset(cx, cy + radius * 0.55f), strokeWidth = 2f)
        } else {
            // Detailed Realistic Multi-Part Firearm Viewmodel (bottom-right)
            val rx = w * 0.58f
            val ry = h * 0.68f + kickPx + reloadDip

            // Muzzle Flash Starburst at barrel tip
            if (muzzleFlash > 0.15f && !weapon.isSuppressed) {
                val mx = rx - w * 0.03f
                val my = ry - h * 0.06f
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color.White.copy(alpha = muzzleFlash),
                            HazardAmber.copy(alpha = muzzleFlash * 0.8f),
                            Color.Transparent
                        ),
                        center = Offset(mx, my),
                        radius = 75f
                    ),
                    radius = 75f,
                    center = Offset(mx, my)
                )
            }

            // Barrel / Suppressor / Handguard
            val barrelColor = if (weapon.isSuppressed) Color(0xFF1A1F26) else Color(0xFF2A323D)
            val barrelPath = Path().apply {
                moveTo(rx - w * 0.03f, ry - h * 0.055f)
                lineTo(rx + w * 0.14f, ry + h * 0.08f)
                lineTo(rx + w * 0.12f, ry + h * 0.14f)
                lineTo(rx - w * 0.05f, ry - h * 0.015f)
                close()
            }
            drawPath(barrelPath, color = barrelColor)
            drawPath(barrelPath, color = Color(0xFF475569), style = Stroke(width = 2.5f))

            // Main Polymer/Alloy Receiver
            val receiverPath = Path().apply {
                moveTo(rx + w * 0.06f, ry + h * 0.01f)
                lineTo(rx + w * 0.34f, ry + h * 0.25f)
                lineTo(rx + w * 0.38f, ry + h * 0.36f)
                lineTo(rx + w * 0.14f, ry + h * 0.36f)
                lineTo(rx + w * 0.02f, ry + h * 0.11f)
                close()
            }
            drawPath(receiverPath, color = Color(0xFF141920))
            drawPath(receiverPath, color = Color(0xFF334155), style = Stroke(width = 3f))

            // Picatinny Rail & Holographic Optic Block
            val opticPath = Path().apply {
                moveTo(rx + w * 0.09f, ry + h * 0.00f)
                lineTo(rx + w * 0.16f, ry + h * 0.055f)
                lineTo(rx + w * 0.14f, ry + h * 0.09f)
                lineTo(rx + w * 0.07f, ry + h * 0.035f)
                close()
            }
            drawPath(opticPath, color = Color(0xFF1F2937))
            drawPath(opticPath, color = CyberCyan.copy(alpha = 0.7f), style = Stroke(width = 2f))

            // Magazine / Pump Forend highlight
            val accentColor = when (weapon) {
                WeaponId.M4A1_SOPMOD -> HazardAmber
                WeaponId.MP5_SD -> CyberCyan
                WeaponId.M870_BREACHER -> Color(0xFFFF5722)
                WeaponId.MK14_EBR -> ToxicAcidLime
                WeaponId.P226_TAC -> Color(0xFF94A3B8)
            }
            drawLine(
                color = accentColor.copy(alpha = 0.85f),
                start = Offset(rx + w * 0.06f, ry + h * 0.06f),
                end = Offset(rx + w * 0.21f, ry + h * 0.18f),
                strokeWidth = 4f
            )
        }
    }
}

@Composable
fun SectionHeaderTag(
    title: String,
    accent: Color = ToxicAcidLime
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .width(4.dp)
                .height(16.dp)
                .background(accent)
        )
        Spacer(Modifier.width(8.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = TacticalWhite
        )
    }
}
