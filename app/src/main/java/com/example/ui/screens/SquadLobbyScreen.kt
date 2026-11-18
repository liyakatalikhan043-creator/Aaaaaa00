package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.GroupAdd
import androidx.compose.material.icons.filled.LocationCity
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Radio
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.local.OperatorProfileEntity
import com.example.data.local.SquadDeploymentLogEntity
import com.example.data.local.SurvivalStashEntity
import com.example.model.CityDistrict
import com.example.model.CookingRecipe
import com.example.model.OperatorId
import com.example.model.WeaponId
import com.example.ui.components.FieldRationModal
import com.example.ui.components.OperatorAvatar
import com.example.ui.components.SectionHeaderTag
import com.example.ui.components.TacticalPanel
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

@Composable
fun SquadLobbyScreen(
    profile: OperatorProfileEntity,
    stash: SurvivalStashEntity,
    logs: List<SquadDeploymentLogEntity>,
    onUpdateCallsign: (String) -> Unit,
    onSelectDistrict: (CityDistrict) -> Unit,
    onNewSquadCode: () -> Unit,
    onJoinSquadCode: (String) -> Unit,
    onCookMeal: (CookingRecipe) -> Unit,
    onEatMeal: (CookingRecipe) -> Unit,
    onDeployClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val activeOperator = OperatorId.fromId(profile.selectedOperatorId)
    val activePrimary = WeaponId.fromId(profile.selectedPrimaryWeaponId)
    val activeDistrict = CityDistrict.fromId(profile.selectedDistrictId)
    val rankLevel = (profile.rankXp / 250) + 1

    var showCallsignDialog by remember { mutableStateOf(false) }
    var showJoinDialog by remember { mutableStateOf(false) }
    var showRationModal by remember { mutableStateOf(false) }

    Box(modifier = modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(14.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Left Column: Operator Card + Co-Op Squad Roster & Frequency Sync
            LazyColumn(
                modifier = Modifier
                    .weight(0.48f)
                    .fillMaxHeight(),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(bottom = 8.dp)
            ) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(132.dp)
                            .clip(CutCornerShape(10.dp))
                            .border(1.dp, ToxicAcidLime.copy(alpha = 0.45f), CutCornerShape(10.dp))
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.img_city_quarantine_banner),
                            contentDescription = "Ruined city skyline",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    Brush.verticalGradient(
                                        colors = listOf(
                                            ObsidianAsphalt.copy(alpha = 0.35f),
                                            ObsidianAsphalt.copy(alpha = 0.92f)
                                        )
                                    )
                                )
                        )
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(12.dp),
                            verticalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(
                                    color = ObsidianAsphalt.copy(alpha = 0.85f),
                                    shape = CutCornerShape(4.dp),
                                    border = BorderStroke(1.dp, CyberCyan.copy(alpha = 0.7f))
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(Icons.Filled.Radio, contentDescription = null, tint = CyberCyan, modifier = Modifier.size(13.dp))
                                        Spacer(Modifier.width(5.dp))
                                        Text(stringResource(R.string.squad_mode_badge), style = MaterialTheme.typography.labelSmall, color = CyberCyan)
                                    }
                                }
                                Text("RANK $rankLevel • ${profile.totalKills} KILLS", style = MaterialTheme.typography.labelSmall, color = ToxicAcidLime)
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                OperatorAvatar(operator = activeOperator, size = 48.dp)
                                Spacer(Modifier.width(10.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(profile.callsign, style = MaterialTheme.typography.titleMedium, color = TacticalWhite)
                                        IconButton(
                                            onClick = { showCallsignDialog = true },
                                            modifier = Modifier
                                                .size(40.dp)
                                                .testTag("edit_callsign_button")
                                        ) {
                                            Icon(Icons.Filled.Edit, contentDescription = "Edit callsign", tint = CyberCyan, modifier = Modifier.size(16.dp))
                                        }
                                    }
                                    Text("${activeOperator.callsign} • ${activePrimary.displayName}", style = MaterialTheme.typography.labelSmall, color = ToxicAcidLime)
                                }
                                OutlinedButton(
                                    onClick = { showRationModal = true },
                                    shape = CutCornerShape(6.dp),
                                    border = BorderStroke(1.dp, HazardAmber.copy(alpha = 0.7f)),
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                                    modifier = Modifier
                                        .height(48.dp)
                                        .testTag("open_field_rations_button")
                                ) {
                                    Icon(Icons.Filled.Restaurant, contentDescription = "Field Rations", tint = HazardAmber, modifier = Modifier.size(15.dp))
                                    Spacer(Modifier.width(4.dp))
                                    Text("RATIONS (${stash.stewCount + stash.stimBrothCount})", style = MaterialTheme.typography.labelSmall, color = HazardAmber)
                                }
                            }
                        }
                    }
                }

                item {
                    TacticalPanel {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            SectionHeaderTag(title = "CO-OP SQUAD ROSTER", accent = CyberCyan)
                            Surface(
                                color = BunkerSurface,
                                shape = CutCornerShape(4.dp),
                                border = BorderStroke(1.dp, CyberCyan.copy(alpha = 0.6f))
                            ) {
                                Text(
                                    text = "FREQ ${profile.squadRoomCode}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = CyberCyan,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                        Spacer(Modifier.height(8.dp))

                        val squadSlots = listOf(
                            Triple(profile.callsign, activeOperator, "SQUAD LEADER • READY"),
                            Triple("VIPER-7", OperatorId.VIPER, "BIO-RECON • FLANK AI"),
                            Triple("KESTREL", OperatorId.KESTREL, "FIELD MEDIC • SUPPORT AI"),
                            Triple("BULWARK", OperatorId.BULWARK, "HEAVY • PERIMETER GUARD")
                        )

                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            squadSlots.forEach { (name, op, role) ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(CutCornerShape(6.dp))
                                        .background(BunkerSurface)
                                        .padding(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    OperatorAvatar(operator = op, size = 34.dp)
                                    Spacer(Modifier.width(8.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(name, style = MaterialTheme.typography.labelLarge, color = TacticalWhite)
                                        Text(role, style = MaterialTheme.typography.labelSmall, color = Color(op.accentHex))
                                    }
                                    Text("ARMED", style = MaterialTheme.typography.labelSmall, color = SafeEmerald)
                                }
                            }
                        }

                        Spacer(Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedButton(
                                onClick = onNewSquadCode,
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp)
                                    .testTag("new_squad_code_button"),
                                shape = CutCornerShape(6.dp),
                                border = BorderStroke(1.dp, CyberCyan)
                            ) {
                                Icon(Icons.Filled.Refresh, contentDescription = null, tint = CyberCyan, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(6.dp))
                                Text(stringResource(R.string.create_squad), style = MaterialTheme.typography.labelMedium, color = CyberCyan)
                            }
                            OutlinedButton(
                                onClick = { showJoinDialog = true },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp)
                                    .testTag("join_squad_code_button"),
                                shape = CutCornerShape(6.dp),
                                border = BorderStroke(1.dp, ToxicAcidLime)
                            ) {
                                Icon(Icons.Filled.GroupAdd, contentDescription = null, tint = ToxicAcidLime, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(6.dp))
                                Text(stringResource(R.string.join_squad), style = MaterialTheme.typography.labelMedium, color = ToxicAcidLime)
                            }
                        }
                    }
                }
            }

            // Right Column: Ruined City Insertion Sectors + Deploy Co-Op + Recent Logs
            Column(
                modifier = Modifier
                    .weight(0.52f)
                    .fillMaxHeight(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                SectionHeaderTag(title = "HALF-DESTROYED CITY SECTORS", accent = ToxicAcidLime)

                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(CityDistrict.entries) { district ->
                        val selected = district == activeDistrict
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onSelectDistrict(district) }
                                .testTag("district_card_${district.id}"),
                            shape = CutCornerShape(8.dp),
                            color = if (selected) ToxicAcidLime.copy(alpha = 0.14f) else CarbonPlate,
                            border = BorderStroke(
                                width = if (selected) 1.5.dp else 1.dp,
                                color = if (selected) ToxicAcidLime else SteelBorder
                            )
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                    Icon(
                                        imageVector = Icons.Filled.LocationCity,
                                        contentDescription = null,
                                        tint = if (selected) ToxicAcidLime else CyberCyan,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(Modifier.width(10.dp))
                                    Column {
                                        Text(district.title, style = MaterialTheme.typography.titleMedium, color = TacticalWhite)
                                        Text(district.lootFocus, style = MaterialTheme.typography.labelSmall, color = TacticalMuted)
                                    }
                                }
                                Text(
                                    text = district.threatLabel,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (district == CityDistrict.SUBWAY_OVERPASS) BioCrimson else HazardAmber
                                )
                            }
                        }
                    }

                    if (logs.isNotEmpty()) {
                        item {
                            Spacer(Modifier.height(4.dp))
                            SectionHeaderTag(title = "RECENT COMBAT LOGS", accent = HazardAmber)
                        }
                        items(logs.take(3)) { log ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(CutCornerShape(4.dp))
                                    .background(BunkerSurface)
                                    .padding(horizontal = 10.dp, vertical = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "${log.districtTitle} • Wave ${log.waveReached} • ${log.kills} Kills (${log.headshots} HS)",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = TacticalWhite
                                )
                                Text(
                                    text = if (log.extractedSafely) "EXTRACTED" else "KIA",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (log.extractedSafely) SafeEmerald else BioCrimson
                                )
                            }
                        }
                    }
                }

                Button(
                    onClick = onDeployClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("deploy_quarantine_button"),
                    shape = CutCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ToxicAcidLime,
                        contentColor = ObsidianAsphalt
                    )
                ) {
                    Icon(Icons.Filled.PlayArrow, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = "${stringResource(R.string.deploy_quarantine)} • ${activeDistrict.title}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        if (showRationModal) {
            FieldRationModal(
                stash = stash,
                onCookMeal = onCookMeal,
                onEatMeal = onEatMeal,
                onClose = { showRationModal = false }
            )
        }
    }

    if (showCallsignDialog) {
        var draft by remember { mutableStateOf(profile.callsign) }
        AlertDialog(
            onDismissRequest = { showCallsignDialog = false },
            containerColor = CarbonPlate,
            title = { Text("UPDATE CALLSIGN", style = MaterialTheme.typography.headlineMedium, color = TacticalWhite) },
            text = {
                OutlinedTextField(
                    value = draft,
                    onValueChange = { draft = it },
                    label = { Text("Operator Callsign") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("callsign_input")
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        onUpdateCallsign(draft)
                        showCallsignDialog = false
                    },
                    modifier = Modifier.testTag("save_callsign_button")
                ) { Text("CONFIRM") }
            },
            dismissButton = {
                TextButton(onClick = { showCallsignDialog = false }) { Text("CANCEL") }
            }
        )
    }

    if (showJoinDialog) {
        var code by remember { mutableStateOf(profile.squadRoomCode) }
        AlertDialog(
            onDismissRequest = { showJoinDialog = false },
            containerColor = CarbonPlate,
            title = { Text("JOIN SQUAD FREQUENCY", style = MaterialTheme.typography.headlineMedium, color = TacticalWhite) },
            text = {
                OutlinedTextField(
                    value = code,
                    onValueChange = { code = it },
                    label = { Text("Squad Code (e.g. BIO-404)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("join_code_input")
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        onJoinSquadCode(code)
                        showJoinDialog = false
                    },
                    modifier = Modifier.testTag("confirm_join_code_button")
                ) { Text("SYNC SQUAD") }
            },
            dismissButton = {
                TextButton(onClick = { showJoinDialog = false }) { Text("CANCEL") }
            }
        )
    }
}
