package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "operator_profile")
data class OperatorProfileEntity(
    @PrimaryKey val id: Int = 1,
    val callsign: String = "NOMAD-01",
    val selectedModeId: String = "single_player",
    val selectedChapter: Int = 1,
    val unlockedChapter: Int = 1,
    val selectedOperatorId: String = "ghostline",
    val selectedPrimaryWeaponId: String = "m4a1",
    val selectedSecondaryWeaponId: String = "p226",
    val selectedDistrictId: String = "neon_market",
    val squadRoomCode: String = "BIO-404",
    val rankXp: Int = 450,
    val totalKills: Int = 0,
    val highestWave: Int = 1,
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "survival_stash")
data class SurvivalStashEntity(
    @PrimaryKey val id: Int = 1,
    val hunger: Float = 92f,
    val health: Float = 100f,
    val cannedBeef: Int = 3,
    val bioMushrooms: Int = 2,
    val purifiedWater: Int = 3,
    val wildHerbs: Int = 2,
    val scrapFuel: Int = 3,
    val stewCount: Int = 2,
    val stimBrothCount: Int = 1,
    val skewerCount: Int = 0,
    val feastCount: Int = 0,
    val sharedSquadRations: Int = 2,
    val ammoReserve: Int = 210
)

@Entity(tableName = "squad_deployment_logs")
data class SquadDeploymentLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val districtTitle: String,
    val waveReached: Int,
    val kills: Int,
    val headshots: Int,
    val mealsCooked: Int,
    val extractedSafely: Boolean,
    val timestampMs: Long = System.currentTimeMillis()
)
