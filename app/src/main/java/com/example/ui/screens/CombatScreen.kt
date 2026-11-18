package com.example.ui.screens

import android.opengl.GLSurfaceView
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.CenterFocusStrong
import androidx.compose.material.icons.filled.DirectionsRun
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.Radio
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.SettingsInputAntenna
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.engine.CityRendererGL
import com.example.engine.CombatHudState
import com.example.engine.CombatRenderSnapshot
import com.example.model.GameMode
import com.example.ui.components.BioRadarMinimap
import com.example.ui.components.FirstPersonWeaponOverlay
import com.example.ui.components.TacticalCrosshair
import com.example.ui.components.TacticalStatBar
import com.example.ui.theme.BioCrimson
import com.example.ui.theme.BunkerSurface
import com.example.ui.theme.CarbonPlate
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.HazardAmber
import com.example.ui.theme.ObsidianAsphalt
import com.example.ui.theme.SafeEmerald
import com.example.ui.theme.SteelBorder
import com.example.ui.theme.TacticalMuted
import com.example.ui.theme.TacticalWhite
import com.example.ui.theme.ToxicAcidLime
import kotlin.math.sqrt

@Composable
fun CombatScreen(
    hud: CombatHudState,
    isPaused: Boolean,
    snapshotProvider: () -> CombatRenderSnapshot,
    onMoveInput: (Float, Float, Boolean) -> Unit,
    onLookDelta: (Float, Float) -> Unit,
    onJump: () -> Unit,
    onSetAds: (Boolean) -> Unit,
    onSetFiring: (Boolean) -> Unit,
    onSlide: () -> Unit,
    onSwapWeapon: () -> Unit,
    onReload: () -> Unit,
    onEatRation: () -> Unit,
    onScavenge: () -> Unit,
    onSquadPing: (String) -> Unit,
    onTogglePause: (Boolean) -> Unit,
    onExtract: () -> Unit,
    onExitToLobby: () -> Unit
) {
    val haptic = LocalHapticFeedback.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val muzzleFlash = snapshotProvider().muzzleFlash

    BackHandler {
        if (!hud.isGameOver && !hud.isExtracted) {
            onTogglePause(!isPaused)
        } else {
            onExitToLobby()
        }
    }

    var glViewRef by remember { mutableStateOf<GLSurfaceView?>(null) }

    DisposableEffect(lifecycleOwner, glViewRef) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> glViewRef?.onResume()
                Lifecycle.Event.ON_PAUSE -> glViewRef?.onPause()
                else -> Unit
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(ObsidianAsphalt)
    ) {
        // 1. Hardware OpenGL ES 2.0 First-Person 3D Half-Destroyed City Viewport
        AndroidView(
            factory = { ctx ->
                GLSurfaceView(ctx).apply {
                    setEGLContextClientVersion(2)
                    setRenderer(CityRendererGL(snapshotProvider))
                    renderMode = GLSurfaceView.RENDERMODE_CONTINUOUSLY
                    glViewRef = this
                }
            },
            modifier = Modifier.fillMaxSize()
        )

        // 2. Realistic First-person Firearm Viewmodel & Muzzle Flash
        FirstPersonWeaponOverlay(
            weapon = hud.activeWeapon,
            isAds = hud.isAds,
            isReloading = hud.isReloading,
            recoilKick = hud.recoilKickUi,
            muzzleFlash = muzzleFlash,
            modifier = Modifier.fillMaxSize()
        )

        // 3. Red damage flash overlay
        if (hud.damageFlashAlpha > 0.02f) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(BioCrimson.copy(alpha = (hud.damageFlashAlpha * 0.42f).coerceIn(0f, 0.5f)))
            )
        }

        // 4. Right-Side Look Touch Surface
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .fillMaxWidth(0.62f)
                .align(Alignment.CenterEnd)
                .pointerInput(Unit) {
                    detectDragGestures { change, dragAmount ->
                        change.consume()
                        onLookDelta(dragAmount.x * 0.0046f, -dragAmount.y * 0.0034f)
                    }
                }
        )

        // 5. Center Tactical Crosshair & Hitmarkers
        TacticalCrosshair(
            isAds = hud.isAds,
            hitMarker = hud.hitMarker,
            modifier = Modifier.align(Alignment.Center)
        )

        // 6. Landscape Safe-Area Tactical HUD Layer
        Box(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.safeDrawing)
                .padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
            // Top Row: Bio-Radar (Left), Story/Wave Objective Banner (Center), Pause & Squad (Right)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.TopCenter),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                BioRadarMinimap(hud = hud)

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.weight(1f).padding(horizontal = 10.dp)
                ) {
                    Surface(
                        color = ObsidianAsphalt.copy(alpha = 0.80f),
                        shape = CutCornerShape(6.dp),
                        border = BorderStroke(1.dp, if (hud.bossActive) BioCrimson else ToxicAcidLime.copy(alpha = 0.6f))
                    ) {
                        Column(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 5.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = if (hud.gameMode == GameMode.SINGLE_PLAYER && hud.storyChapter != null) {
                                    hud.storyChapter.title
                                } else {
                                    "WAVE ${hud.waveNumber} • ${hud.activeDistrict.title}"
                                },
                                style = MaterialTheme.typography.labelLarge,
                                color = if (hud.bossActive) BioCrimson else ToxicAcidLime
                            )
                            Text(
                                text = hud.storyObjectiveText,
                                style = MaterialTheme.typography.labelSmall,
                                color = TacticalWhite
                            )
                        }
                    }

                    // Live Story / Squad Radio Callout
                    hud.radioCallouts.firstOrNull()?.let { callout ->
                        Spacer(Modifier.height(4.dp))
                        Surface(
                            color = if (callout.isAlert) BioCrimson.copy(alpha = 0.28f) else CarbonPlate.copy(alpha = 0.82f),
                            shape = CutCornerShape(4.dp),
                            border = BorderStroke(0.5.dp, if (callout.isAlert) BioCrimson else CyberCyan.copy(alpha = 0.5f))
                        ) {
                            Text(
                                text = "[${callout.sender}] ${callout.message}",
                                style = MaterialTheme.typography.labelSmall,
                                color = if (callout.isAlert) HazardAmber else CyberCyan,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp)
                            )
                        }
                    }

                    hud.statusToast?.let { toast ->
                        Spacer(Modifier.height(4.dp))
                        Surface(color = ToxicAcidLime, shape = CutCornerShape(4.dp)) {
                            Text(
                                text = toast,
                                style = MaterialTheme.typography.labelSmall,
                                color = ObsidianAsphalt,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp)
                            )
                        }
                    }
                }

                Column(horizontalAlignment = Alignment.End) {
                    IconButton(
                        onClick = { onTogglePause(true) },
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CutCornerShape(6.dp))
                            .background(CarbonPlate.copy(alpha = 0.85f))
                            .testTag("pause_combat_button")
                    ) {
                        Icon(Icons.Filled.Pause, contentDescription = "Pause mission", tint = TacticalWhite)
                    }
                    if (hud.squadMates.isNotEmpty()) {
                        Spacer(Modifier.height(4.dp))
                        Surface(
                            color = ObsidianAsphalt.copy(alpha = 0.78f),
                            shape = CutCornerShape(4.dp),
                            border = BorderStroke(0.5.dp, SteelBorder)
                        ) {
                            Column(modifier = Modifier.padding(6.dp)) {
                                hud.squadMates.forEach { mate ->
                                    Text(
                                        text = "${mate.callsign}: ${mate.kills}K",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = CyberCyan
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Center Context Prompts: Story Blackbox Beacon or Supply Crate
            Column(
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(top = 110.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (hud.nearBeacon != null) {
                    Button(
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            onScavenge()
                        },
                        shape = CutCornerShape(6.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = CyberCyan, contentColor = ObsidianAsphalt),
                        modifier = Modifier.height(48.dp).testTag("secure_beacon_button")
                    ) {
                        Icon(Icons.Filled.SettingsInputAntenna, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("SECURE BLACKBOX BEACON", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                    }
                }

                hud.nearLootNode?.let { crate ->
                    Button(
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            onScavenge()
                        },
                        shape = CutCornerShape(6.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = ToxicAcidLime, contentColor = ObsidianAsphalt),
                        modifier = Modifier.height(48.dp).testTag("scavenge_crate_button")
                    ) {
                        Icon(Icons.Filled.Inventory, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("PICK UP ${crate.kind.label.uppercase()}", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                    }
                }
            }

            // Bottom-Left: Compact Vitals & Virtual Movement/Sprint Thumbstick
            Row(
                modifier = Modifier.align(Alignment.BottomStart),
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                VirtualMoveStick(
                    onMove = onMoveInput,
                    modifier = Modifier.testTag("virtual_move_stick")
                )

                Surface(
                    color = ObsidianAsphalt.copy(alpha = 0.80f),
                    shape = CutCornerShape(6.dp),
                    border = BorderStroke(1.dp, SteelBorder),
                    modifier = Modifier.width(156.dp)
                ) {
                    Column(modifier = Modifier.padding(8.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        TacticalStatBar(
                            label = "HP",
                            value = hud.health,
                            maxValue = hud.maxHealth,
                            barColor = if (hud.health < 35f) BioCrimson else SafeEmerald,
                            valueText = "${hud.health.toInt()}"
                        )
                        TacticalStatBar(
                            label = "STAMINA",
                            value = hud.stamina,
                            maxValue = 100f,
                            barColor = CyberCyan,
                            valueText = if (hud.stimBuffSec > 0f) "STIM" else "${hud.stamina.toInt()}%"
                        )
                        TacticalStatBar(
                            label = "ENERGY",
                            value = hud.hunger,
                            maxValue = 100f,
                            barColor = HazardAmber,
                            valueText = "${hud.hunger.toInt()}%"
                        )
                    }
                }
            }

            // Bottom-Right: Weapon Telemetry & Landscape Tactical Combat Buttons
            Column(
                modifier = Modifier.align(Alignment.BottomEnd),
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Surface(
                    color = ObsidianAsphalt.copy(alpha = 0.82f),
                    shape = CutCornerShape(6.dp),
                    border = BorderStroke(1.dp, CyberCyan.copy(alpha = 0.5f))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Column {
                            Text(hud.activeWeapon.displayName, style = MaterialTheme.typography.labelMedium, color = CyberCyan)
                            Text(hud.activeWeapon.caliber, style = MaterialTheme.typography.labelSmall, color = TacticalMuted)
                        }
                        Text(
                            text = if (hud.isReloading) "RLD ${(hud.reloadProgress * 100).toInt()}%" else "${hud.clipAmmo} / ${hud.ammoReserve}",
                            style = MaterialTheme.typography.headlineMedium,
                            color = if (hud.clipAmmo <= 4) BioCrimson else TacticalWhite
                        )
                    }
                }

                // Utility row: Ping, Ration, Swap, Slide, Jump
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    if (hud.gameMode == GameMode.COOP_SQUAD) {
                        TacticalActionCircle(
                            icon = Icons.Filled.Radio,
                            label = "PING",
                            tint = CyberCyan,
                            onClick = { onSquadPing("HOSTILES!") },
                            modifier = Modifier.testTag("squad_ping_button")
                        )
                    }
                    TacticalActionCircle(
                        icon = Icons.Filled.Restaurant,
                        label = "MRE(${hud.preparedRationsCount})",
                        tint = HazardAmber,
                        onClick = onEatRation,
                        modifier = Modifier.testTag("quick_eat_button")
                    )
                    TacticalActionCircle(
                        icon = Icons.Filled.SwapHoriz,
                        label = "SWAP",
                        tint = TacticalWhite,
                        onClick = onSwapWeapon,
                        modifier = Modifier.testTag("swap_weapon_button")
                    )
                    TacticalActionCircle(
                        icon = Icons.Filled.DirectionsRun,
                        label = "SLIDE",
                        tint = ToxicAcidLime,
                        onClick = onSlide,
                        modifier = Modifier.testTag("slide_button")
                    )
                    TacticalActionCircle(
                        icon = Icons.Filled.ArrowUpward,
                        label = "JUMP",
                        tint = CyberCyan,
                        onClick = onJump,
                        modifier = Modifier.testTag("jump_button")
                    )
                }

                // Primary Combat Row: Reload, ADS, Hold-to-Fire
                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TacticalActionCircle(
                        icon = Icons.Filled.Refresh,
                        label = "RLD",
                        tint = TacticalWhite,
                        onClick = onReload,
                        modifier = Modifier.testTag("reload_button")
                    )
                    Surface(
                        onClick = { onSetAds(!hud.isAds) },
                        shape = CircleShape,
                        color = if (hud.isAds) CyberCyan else CarbonPlate.copy(alpha = 0.88f),
                        border = BorderStroke(1.5.dp, CyberCyan),
                        modifier = Modifier.size(54.dp).testTag("ads_button")
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                Icons.Filled.CenterFocusStrong,
                                contentDescription = "Aim Down Sights",
                                tint = if (hud.isAds) ObsidianAsphalt else CyberCyan,
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                text = "ADS",
                                style = MaterialTheme.typography.labelSmall,
                                color = if (hud.isAds) ObsidianAsphalt else CyberCyan
                            )
                        }
                    }

                    // Hold-to-fire trigger button
                    Surface(
                        shape = CircleShape,
                        color = BioCrimson.copy(alpha = 0.90f),
                        border = BorderStroke(2.dp, TacticalWhite.copy(alpha = 0.85f)),
                        modifier = Modifier
                            .size(72.dp)
                            .pointerInput(Unit) {
                                awaitEachGesture {
                                    awaitFirstDown(requireUnconsumed = false)
                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    onSetFiring(true)
                                    do {
                                        val event = awaitPointerEvent()
                                    } while (event.changes.any { it.pressed })
                                    onSetFiring(false)
                                }
                            }
                            .testTag("fire_button")
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(Icons.Filled.MyLocation, contentDescription = "Fire weapon", tint = TacticalWhite, modifier = Modifier.size(26.dp))
                            Text("FIRE", style = MaterialTheme.typography.labelSmall, color = TacticalWhite, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // Pause Menu Overlay
        if (isPaused && !hud.isGameOver && !hud.isExtracted) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(ObsidianAsphalt.copy(alpha = 0.85f)),
                contentAlignment = Alignment.Center
            ) {
                Surface(
                    shape = CutCornerShape(10.dp),
                    color = CarbonPlate,
                    border = BorderStroke(1.5.dp, CyberCyan),
                    modifier = Modifier.width(340.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("MISSION PAUSED", style = MaterialTheme.typography.headlineMedium, color = TacticalWhite)
                        Text(
                            text = hud.storyChapter?.title ?: "Wave ${hud.waveNumber} • ${hud.kills} Infected Eliminated",
                            style = MaterialTheme.typography.bodyMedium,
                            color = CyberCyan
                        )
                        Button(
                            onClick = { onTogglePause(false) },
                            modifier = Modifier.fillMaxWidth().height(48.dp).testTag("resume_combat_button"),
                            colors = ButtonDefaults.buttonColors(containerColor = ToxicAcidLime, contentColor = ObsidianAsphalt)
                        ) { Text("RESUME COMBAT", fontWeight = FontWeight.Bold) }
                        OutlinedButton(
                            onClick = onExtract,
                            modifier = Modifier.fillMaxWidth().height(48.dp).testTag("extract_mission_button"),
                            border = BorderStroke(1.dp, CyberCyan)
                        ) { Text("EXTRACT TO HUB", color = CyberCyan) }
                    }
                }
            }
        }

        // Mission Complete / KIA Summary Overlay
        if (hud.isGameOver || hud.isExtracted) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(ObsidianAsphalt.copy(alpha = 0.90f)),
                contentAlignment = Alignment.Center
            ) {
                Surface(
                    shape = CutCornerShape(10.dp),
                    color = CarbonPlate,
                    border = BorderStroke(1.5.dp, if (hud.isExtracted) ToxicAcidLime else BioCrimson),
                    modifier = Modifier.width(360.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        val title = when {
                            hud.isExtracted && hud.gameMode == GameMode.SINGLE_PLAYER -> "MISSION ACCOMPLISHED"
                            hud.isExtracted -> "EXTRACTION COMPLETE"
                            else -> "OPERATOR OVERRUN"
                        }
                        Text(
                            text = title,
                            style = MaterialTheme.typography.headlineMedium,
                            color = if (hud.isExtracted) ToxicAcidLime else BioCrimson
                        )
                        Text(
                            text = hud.storyChapter?.title ?: hud.activeDistrict.title,
                            style = MaterialTheme.typography.labelLarge,
                            color = CyberCyan
                        )
                        Surface(
                            color = BunkerSurface,
                            shape = CutCornerShape(6.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                SummaryStatRow("HOSTILES ELIMINATED", "${hud.kills}")
                                SummaryStatRow("HEADSHOTS", "${hud.headshots}")
                                if (hud.gameMode == GameMode.SINGLE_PLAYER) {
                                    SummaryStatRow("BLACKBOX BEACONS", "${hud.beaconsCollected} / ${hud.beaconsTarget}")
                                } else {
                                    SummaryStatRow("WAVE REACHED", "${hud.waveNumber}")
                                }
                            }
                        }
                        Button(
                            onClick = onExitToLobby,
                            modifier = Modifier.fillMaxWidth().height(48.dp).testTag("return_to_lobby_button"),
                            colors = ButtonDefaults.buttonColors(containerColor = ToxicAcidLime, contentColor = ObsidianAsphalt)
                        ) {
                            Text("RETURN TO BASE", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun VirtualMoveStick(
    onMove: (Float, Float, Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    var knobOffset by remember { mutableStateOf(Offset.Zero) }
    var isSprinting by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
            .size(116.dp)
            .pointerInput(Unit) {
                val maxRadius = size.width * 0.38f
                awaitEachGesture {
                    val down = awaitFirstDown()
                    val center = Offset(size.width / 2f, size.height / 2f)
                    fun updateFrom(pos: Offset) {
                        val delta = pos - center
                        val dist = sqrt(delta.x * delta.x + delta.y * delta.y)
                        val clamped = if (dist > maxRadius && dist > 0f) delta * (maxRadius / dist) else delta
                        knobOffset = clamped
                        val normX = (clamped.x / maxRadius).coerceIn(-1f, 1f)
                        val normY = (-clamped.y / maxRadius).coerceIn(-1f, 1f)
                        val sprint = dist > maxRadius * 0.78f && normY > 0.35f
                        isSprinting = sprint
                        onMove(normX, normY, sprint)
                    }
                    updateFrom(down.position)
                    do {
                        val event = awaitPointerEvent()
                        val ptr = event.changes.firstOrNull { it.id == down.id }
                        if (ptr != null && ptr.pressed) {
                            ptr.consume()
                            updateFrom(ptr.position)
                        }
                    } while (event.changes.any { it.id == down.id && it.pressed })
                    knobOffset = Offset.Zero
                    isSprinting = false
                    onMove(0f, 0f, false)
                }
            }
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val center = Offset(size.width / 2f, size.height / 2f)
            val radius = size.minDimension * 0.44f
            drawCircle(
                color = ObsidianAsphalt.copy(alpha = 0.65f),
                radius = radius,
                center = center
            )
            drawCircle(
                color = if (isSprinting) ToxicAcidLime else CyberCyan.copy(alpha = 0.65f),
                radius = radius,
                center = center,
                style = Stroke(width = 3f)
            )
            drawCircle(
                color = if (isSprinting) ToxicAcidLime else CyberCyan,
                radius = 20.dp.toPx(),
                center = center + knobOffset
            )
        }
    }
}

@Composable
private fun TacticalActionCircle(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    tint: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        shape = CircleShape,
        color = CarbonPlate.copy(alpha = 0.88f),
        border = BorderStroke(1.dp, tint.copy(alpha = 0.7f)),
        modifier = modifier.size(48.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(icon, contentDescription = label, tint = tint, modifier = Modifier.size(17.dp))
            Text(label, style = MaterialTheme.typography.labelSmall, color = tint)
        }
    }
}

@Composable
private fun SummaryStatRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, style = MaterialTheme.typography.labelMedium, color = TacticalMuted)
        Text(value, style = MaterialTheme.typography.labelLarge, color = ToxicAcidLime)
    }
}
