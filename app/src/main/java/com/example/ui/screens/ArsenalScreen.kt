package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.data.local.OperatorProfileEntity
import com.example.model.OperatorId
import com.example.model.WeaponId
import com.example.ui.components.OperatorAvatar
import com.example.ui.components.SectionHeaderTag
import com.example.ui.components.TacticalStatBar
import com.example.ui.theme.BioCrimson
import com.example.ui.theme.BunkerSurface
import com.example.ui.theme.CarbonPlate
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.HazardAmber
import com.example.ui.theme.SteelBorder
import com.example.ui.theme.TacticalMuted
import com.example.ui.theme.TacticalWhite
import com.example.ui.theme.ToxicAcidLime

@Composable
fun ArsenalScreen(
    profile: OperatorProfileEntity,
    onSelectOperator: (OperatorId) -> Unit,
    onSelectWeapon: (WeaponId) -> Unit,
    modifier: Modifier = Modifier
) {
    val selectedOp = OperatorId.fromId(profile.selectedOperatorId)
    val selectedPrimary = WeaponId.fromId(profile.selectedPrimaryWeaponId)
    val selectedSecondary = WeaponId.fromId(profile.selectedSecondaryWeaponId)

    Row(
        modifier = modifier
            .fillMaxSize()
            .padding(14.dp),
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Left Column: Tactical Operators
        Column(
            modifier = Modifier
                .weight(0.45f)
                .fillMaxHeight()
        ) {
            SectionHeaderTag(title = "TACTICAL OPERATORS", accent = CyberCyan)
            Spacer(Modifier.height(4.dp))
            Text(
                text = "Choose your operative specialist.",
                style = MaterialTheme.typography.bodyMedium,
                color = TacticalMuted
            )
            Spacer(Modifier.height(8.dp))

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(bottom = 8.dp)
            ) {
                items(OperatorId.entries) { op ->
                    val isSelected = op == selectedOp
                    val accent = Color(op.accentHex)
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelectOperator(op) }
                            .testTag("operator_card_${op.id}"),
                        shape = CutCornerShape(8.dp),
                        color = if (isSelected) accent.copy(alpha = 0.14f) else CarbonPlate,
                        border = BorderStroke(if (isSelected) 1.5.dp else 1.dp, if (isSelected) accent else SteelBorder)
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OperatorAvatar(operator = op, size = 58.dp)
                            Spacer(Modifier.width(10.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = op.callsign,
                                        style = MaterialTheme.typography.titleMedium,
                                        color = TacticalWhite
                                    )
                                    if (isSelected) {
                                        Icon(
                                            imageVector = Icons.Filled.CheckCircle,
                                            contentDescription = "Equipped",
                                            tint = accent
                                        )
                                    }
                                }
                                Text(
                                    text = op.roleTitle,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = accent
                                )
                                Spacer(Modifier.height(2.dp))
                                Text(
                                    text = op.perkSummary,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = TacticalMuted
                                )
                            }
                        }
                    }
                }
            }
        }

        // Right Column: Realistic Firearms & Ballistics
        Column(
            modifier = Modifier
                .weight(0.55f)
                .fillMaxHeight()
        ) {
            SectionHeaderTag(title = "REALISTIC MILITARY ARSENAL", accent = ToxicAcidLime)
            Spacer(Modifier.height(4.dp))
            Text(
                text = "Tap a firearm to equip Primary or Sidearm slot.",
                style = MaterialTheme.typography.bodyMedium,
                color = TacticalMuted
            )
            Spacer(Modifier.height(8.dp))

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(bottom = 8.dp)
            ) {
                items(WeaponId.entries) { weapon ->
                    val isEquipped = weapon == selectedPrimary || weapon == selectedSecondary
                    val slotTag = if (weapon.isPrimary) "PRIMARY" else "SIDEARM"
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelectWeapon(weapon) }
                            .testTag("weapon_card_${weapon.id}"),
                        shape = CutCornerShape(8.dp),
                        color = if (isEquipped) ToxicAcidLime.copy(alpha = 0.12f) else CarbonPlate,
                        border = BorderStroke(
                            width = if (isEquipped) 1.5.dp else 1.dp,
                            color = if (isEquipped) ToxicAcidLime else SteelBorder
                        )
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = weapon.displayName,
                                        style = MaterialTheme.typography.titleMedium,
                                        color = TacticalWhite
                                    )
                                    Text(
                                        text = "${weapon.caliber} • ${weapon.category} • $slotTag • ${weapon.magazineSize} RDS",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = if (isEquipped) ToxicAcidLime else CyberCyan
                                    )
                                }
                                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    if (weapon.isSuppressed) {
                                        Surface(color = BunkerSurface, shape = CutCornerShape(4.dp)) {
                                            Text(
                                                text = "SUPPRESSED",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = CyberCyan,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                                            )
                                        }
                                    }
                                    if (isEquipped) {
                                        Surface(color = ToxicAcidLime, shape = CutCornerShape(4.dp)) {
                                            Text(
                                                text = "EQUIPPED",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = Color(0xFF090C10),
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                            )
                                        }
                                    }
                                }
                            }

                            Spacer(Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                TacticalStatBar(
                                    label = "DAMAGE",
                                    value = weapon.statDamage,
                                    barColor = BioCrimson,
                                    modifier = Modifier.weight(1f)
                                )
                                TacticalStatBar(
                                    label = "FIRE RATE",
                                    value = weapon.statFireRate,
                                    barColor = HazardAmber,
                                    modifier = Modifier.weight(1f)
                                )
                                TacticalStatBar(
                                    label = "MOBILITY",
                                    value = weapon.statMobility,
                                    barColor = CyberCyan,
                                    modifier = Modifier.weight(1f)
                                )
                                TacticalStatBar(
                                    label = "CONTROL",
                                    value = weapon.statControl,
                                    barColor = ToxicAcidLime,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
