package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.local.SurvivalStashEntity
import com.example.engine.CombatHudState
import com.example.engine.HitMarkerKind
import com.example.model.CityWorldMap
import com.example.model.CookingRecipe
import com.example.ui.theme.BioCrimson
import com.example.ui.theme.BunkerSurface
import com.example.ui.theme.CarbonPlate
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.HazardAmber
import com.example.ui.theme.ObsidianAsphalt
import com.example.ui.theme.SafeEmerald
import com.example.ui.theme.SteelBorder
import com.example.ui.theme.TacticalWhite
import com.example.ui.theme.ToxicAcidLime
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun BioRadarMinimap(
    hud: CombatHudState,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(92.dp)
            .clip(CircleShape)
            .background(ObsidianAsphalt.copy(alpha = 0.80f))
            .border(1.5.dp, CyberCyan.copy(alpha = 0.75f), CircleShape)
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val r = size.minDimension / 2f
            val cx = size.width / 2f
            val cy = size.height / 2f
            val scale = r / 36f

            drawCircle(CyberCyan.copy(alpha = 0.22f), radius = r * 0.65f, center = Offset(cx, cy), style = Stroke(1f))
            drawCircle(CyberCyan.copy(alpha = 0.22f), radius = r * 0.32f, center = Offset(cx, cy), style = Stroke(1f))

            val cosY = cos(-hud.playerYaw)
            val sinY = sin(-hud.playerYaw)

            fun toRadar(wx: Float, wz: Float): Offset? {
                val dx = wx - hud.playerX
                val dz = wz - hud.playerZ
                val rx = dx * cosY - dz * sinY
                val rz = dx * sinY + dz * cosY
                val px = cx + rx * scale
                val py = cy + rz * scale
                val distSq = (px - cx) * (px - cx) + (py - cy) * (py - cy)
                return if (distSq <= (r - 4f) * (r - 4f)) Offset(px, py) else null
            }

            // Nearby ruined buildings
            val pGx = (hud.playerX / CityWorldMap.CELL_SIZE).toInt()
            val pGz = (hud.playerZ / CityWorldMap.CELL_SIZE).toInt()
            for (gz in (pGz - 10).coerceAtLeast(0)..(pGz + 10).coerceAtMost(CityWorldMap.GRID_SIZE - 1)) {
                for (gx in (pGx - 10).coerceAtLeast(0)..(pGx + 10).coerceAtMost(CityWorldMap.GRID_SIZE - 1)) {
                    if (CityWorldMap.cellAt(gx, gz) != 0) {
                        toRadar((gx + 0.5f) * CityWorldMap.CELL_SIZE, (gz + 0.5f) * CityWorldMap.CELL_SIZE)?.let { pos ->
                            drawRect(Color(0xFF3A4856), topLeft = Offset(pos.x - 2f, pos.y - 2f), size = Size(4f, 4f))
                        }
                    }
                }
            }

            // Story Beacons (Cyan target markers!)
            for (bcn in hud.propsForRadar) {
                toRadar(bcn.x, bcn.z)?.let {
                    drawCircle(CyberCyan, radius = 4.5f, center = it)
                }
            }

            // Supply Crates
            for (crate in hud.lootForRadar) {
                if (crate.collected) continue
                toRadar(crate.x, crate.z)?.let {
                    drawCircle(ToxicAcidLime, radius = 2.8f, center = it)
                }
            }

            // Co-Op Squadmates
            for (mate in hud.squadMates) {
                toRadar(mate.x, mate.z)?.let {
                    drawCircle(SafeEmerald, radius = 3.2f, center = it)
                }
            }

            // Zombies
            for (z in hud.zombiesForRadar) {
                toRadar(z.x, z.z)?.let {
                    drawCircle(BioCrimson, radius = 3f, center = it)
                }
            }

            // Player
            drawCircle(TacticalWhite, radius = 3.8f, center = Offset(cx, cy))
        }
    }
}

@Composable
fun TacticalCrosshair(
    isAds: Boolean,
    hitMarker: HitMarkerKind,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier.size(58.dp)) {
        val cx = size.width / 2f
        val cy = size.height / 2f
        val gap = if (isAds) 4f else 10f
        val len = if (isAds) 8f else 10f
        val color = if (isAds) CyberCyan else TacticalWhite.copy(alpha = 0.85f)

        drawLine(color, Offset(cx - gap - len, cy), Offset(cx - gap, cy), strokeWidth = 2.2f)
        drawLine(color, Offset(cx + gap, cy), Offset(cx + gap + len, cy), strokeWidth = 2.2f)
        drawLine(color, Offset(cx, cy - gap - len), Offset(cx, cy - gap), strokeWidth = 2.2f)
        drawLine(color, Offset(cx, cy + gap), Offset(cx, cy + gap + len), strokeWidth = 2.2f)

        if (hitMarker != HitMarkerKind.NONE) {
            val hmColor = when (hitMarker) {
                HitMarkerKind.KILL -> BioCrimson
                HitMarkerKind.HEADSHOT -> HazardAmber
                HitMarkerKind.BODY -> TacticalWhite
                HitMarkerKind.NONE -> Color.Transparent
            }
            val d1 = 6f
            val d2 = 15f
            drawLine(hmColor, Offset(cx - d2, cy - d2), Offset(cx - d1, cy - d1), strokeWidth = 3f)
            drawLine(hmColor, Offset(cx + d1, cy - d1), Offset(cx + d2, cy - d2), strokeWidth = 3f)
            drawLine(hmColor, Offset(cx - d2, cy + d2), Offset(cx - d1, cy + d1), strokeWidth = 3f)
            drawLine(hmColor, Offset(cx + d1, cy + d1), Offset(cx + d2, cy + d2), strokeWidth = 3f)
        }
    }
}

@Composable
fun FieldRationModal(
    stash: SurvivalStashEntity,
    onCookMeal: (CookingRecipe) -> Unit,
    onEatMeal: (CookingRecipe) -> Unit,
    onClose: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(ObsidianAsphalt.copy(alpha = 0.86f))
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            modifier = Modifier.fillMaxWidth(0.75f),
            shape = CutCornerShape(10.dp),
            color = CarbonPlate,
            border = BorderStroke(1.5.dp, HazardAmber)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("FIELD RATION PACKER", style = MaterialTheme.typography.headlineMedium, color = HazardAmber)
                    OutlinedButton(onClick = onClose, modifier = Modifier.testTag("close_ration_modal")) {
                        Text("CLOSE", color = ToxicAcidLime)
                    }
                }
                Spacer(Modifier.height(6.dp))
                Text(
                    text = "Supplies — Beef: ${stash.cannedBeef} • Bio-Shrooms: ${stash.bioMushrooms} • Water: ${stash.purifiedWater} • Fuel: ${stash.scrapFuel}",
                    style = MaterialTheme.typography.labelMedium,
                    color = CyberCyan
                )
                Spacer(Modifier.height(10.dp))
                LazyColumn(
                    modifier = Modifier.height(170.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(CookingRecipe.entries) { recipe ->
                        val owned = if (recipe == CookingRecipe.MRE_STEW) stash.stewCount else stash.stimBrothCount
                        Surface(
                            color = BunkerSurface,
                            shape = CutCornerShape(6.dp),
                            border = BorderStroke(1.dp, SteelBorder)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(recipe.title, style = MaterialTheme.typography.titleMedium, color = TacticalWhite)
                                    Text("${recipe.buffLabel} • Packed: $owned", style = MaterialTheme.typography.labelSmall, color = ToxicAcidLime)
                                }
                                Button(
                                    onClick = { onCookMeal(recipe) },
                                    colors = ButtonDefaults.buttonColors(containerColor = HazardAmber, contentColor = ObsidianAsphalt),
                                    modifier = Modifier.height(40.dp)
                                ) { Text("PACK", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold) }
                                Spacer(Modifier.width(6.dp))
                                OutlinedButton(
                                    onClick = { onEatMeal(recipe) },
                                    enabled = owned > 0,
                                    modifier = Modifier.height(40.dp)
                                ) { Text("USE", style = MaterialTheme.typography.labelSmall, color = SafeEmerald) }
                            }
                        }
                    }
                }
            }
        }
    }
}
