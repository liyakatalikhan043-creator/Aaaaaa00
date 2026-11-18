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
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.local.OperatorProfileEntity
import com.example.model.OperatorId
import com.example.model.StoryChapter
import com.example.model.WeaponId
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
fun CampaignHubScreen(
    profile: OperatorProfileEntity,
    onSelectChapter: (StoryChapter) -> Unit,
    onDeployChapter: (StoryChapter) -> Unit,
    modifier: Modifier = Modifier
) {
    val selectedChapter = StoryChapter.fromNumber(profile.selectedChapter)
    val activeOperator = OperatorId.fromId(profile.selectedOperatorId)
    val activePrimary = WeaponId.fromId(profile.selectedPrimaryWeaponId)
    val rankLevel = (profile.rankXp / 250) + 1

    Row(
        modifier = modifier
            .fillMaxSize()
            .padding(14.dp),
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Left Column: 4-Chapter Story Campaign Selection
        Column(
            modifier = Modifier
                .weight(0.52f)
                .fillMaxHeight()
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                SectionHeaderTag(title = "OPERATION: ASHEN HORIZON", accent = ToxicAcidLime)
                Surface(
                    color = CyberCyan.copy(alpha = 0.16f),
                    shape = CutCornerShape(4.dp),
                    border = BorderStroke(1.dp, CyberCyan.copy(alpha = 0.6f))
                ) {
                    Text(
                        text = stringResource(R.string.story_mode_badge),
                        style = MaterialTheme.typography.labelSmall,
                        color = CyberCyan,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
            Spacer(Modifier.height(10.dp))

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(bottom = 8.dp)
            ) {
                items(StoryChapter.entries) { ch ->
                    val unlocked = ch.number <= profile.unlockedChapter
                    val selected = ch == selectedChapter
                    val cleared = ch.number < profile.unlockedChapter
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable(enabled = unlocked) { onSelectChapter(ch) }
                            .testTag("story_chapter_${ch.number}"),
                        shape = CutCornerShape(8.dp),
                        color = when {
                            selected -> ToxicAcidLime.copy(alpha = 0.14f)
                            unlocked -> CarbonPlate
                            else -> ObsidianAsphalt
                        },
                        border = BorderStroke(
                            width = if (selected) 1.5.dp else 1.dp,
                            color = when {
                                selected -> ToxicAcidLime
                                unlocked -> SteelBorder
                                else -> BunkerSurface
                            }
                        )
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = ch.title,
                                        style = MaterialTheme.typography.titleMedium,
                                        color = if (unlocked) TacticalWhite else TacticalMuted
                                    )
                                    if (ch.hasBoss) {
                                        Spacer(Modifier.width(8.dp))
                                        Icon(
                                            Icons.Filled.Warning,
                                            contentDescription = "Boss encounter",
                                            tint = BioCrimson,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                                Text(
                                    text = ch.subtitle,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = if (selected) ToxicAcidLime else TacticalMuted
                                )
                            }
                            if (!unlocked) {
                                Icon(Icons.Filled.Lock, contentDescription = "Locked", tint = TacticalMuted)
                            } else if (cleared) {
                                Icon(Icons.Filled.Star, contentDescription = "Completed", tint = HazardAmber)
                            }
                        }
                    }
                }
            }
        }

        // Right Column: Mission Briefing, Ruined City Visual & Instant Deploy
        Column(
            modifier = Modifier
                .weight(0.48f)
                .fillMaxHeight(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .clip(CutCornerShape(10.dp))
                    .border(1.dp, CyberCyan.copy(alpha = 0.45f), CutCornerShape(10.dp))
            ) {
                Image(
                    painter = painterResource(id = R.drawable.img_city_quarantine_banner),
                    contentDescription = "Ruined city mission zone",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    ObsidianAsphalt.copy(alpha = 0.25f),
                                    ObsidianAsphalt.copy(alpha = 0.94f)
                                )
                            )
                        )
                )
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(14.dp),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            OperatorAvatar(operator = activeOperator, size = 44.dp)
                            Spacer(Modifier.width(10.dp))
                            Column {
                                Text(profile.callsign, style = MaterialTheme.typography.titleMedium, color = TacticalWhite)
                                Text(
                                    "${activePrimary.displayName} • RANK $rankLevel",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = CyberCyan
                                )
                            }
                        }
                        Surface(
                            color = BunkerSurface,
                            shape = CutCornerShape(4.dp)
                        ) {
                            Text(
                                text = selectedChapter.district.title,
                                style = MaterialTheme.typography.labelSmall,
                                color = HazardAmber,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Column {
                        Text(
                            text = selectedChapter.title,
                            style = MaterialTheme.typography.headlineMedium,
                            color = ToxicAcidLime
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = selectedChapter.briefing,
                            style = MaterialTheme.typography.bodyMedium,
                            color = TacticalWhite
                        )
                    }
                }
            }

            TacticalPanel(borderColor = ToxicAcidLime.copy(alpha = 0.6f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("OBJECTIVES", style = MaterialTheme.typography.labelSmall, color = TacticalMuted)
                        Text(
                            text = "${selectedChapter.targetBeacons} Beacons • ${selectedChapter.targetKills} Hostiles",
                            style = MaterialTheme.typography.labelLarge,
                            color = SafeEmerald
                        )
                    }
                    Button(
                        onClick = { onDeployChapter(selectedChapter) },
                        modifier = Modifier
                            .height(50.dp)
                            .testTag("deploy_story_button"),
                        shape = CutCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = ToxicAcidLime,
                            contentColor = ObsidianAsphalt
                        )
                    ) {
                        Icon(Icons.Filled.PlayArrow, contentDescription = null)
                        Spacer(Modifier.width(6.dp))
                        Text(stringResource(R.string.deploy_mission), fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
