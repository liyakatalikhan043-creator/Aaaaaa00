package com.example.data.repository

import com.example.data.local.GameDao
import com.example.data.local.OperatorProfileEntity
import com.example.data.local.SquadDeploymentLogEntity
import com.example.data.local.SurvivalStashEntity
import com.example.model.CityDistrict
import com.example.model.CookingRecipe
import com.example.model.GameMode
import com.example.model.LootKind
import com.example.model.OperatorId
import com.example.model.StoryChapter
import com.example.model.WeaponId
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class GameRepository(private val dao: GameDao) {

    val profileFlow: Flow<OperatorProfileEntity> =
        dao.observeProfile().map { it ?: OperatorProfileEntity() }

    val stashFlow: Flow<SurvivalStashEntity> =
        dao.observeStash().map { it ?: SurvivalStashEntity() }

    val deploymentLogsFlow: Flow<List<SquadDeploymentLogEntity>> =
        dao.observeDeploymentLogs()

    suspend fun ensureSeeded() {
        if (dao.getProfileOnce() == null) {
            dao.upsertProfile(OperatorProfileEntity())
        }
        if (dao.getStashOnce() == null) {
            dao.upsertStash(SurvivalStashEntity())
        }
    }

    suspend fun updateCallsign(callsign: String) {
        val current = dao.getProfileOnce() ?: OperatorProfileEntity()
        val clean = callsign.trim().uppercase().take(16).ifBlank { "NOMAD-01" }
        dao.upsertProfile(current.copy(callsign = clean, updatedAt = System.currentTimeMillis()))
    }

    suspend fun selectMode(mode: GameMode) {
        val current = dao.getProfileOnce() ?: OperatorProfileEntity()
        dao.upsertProfile(current.copy(selectedModeId = mode.id, updatedAt = System.currentTimeMillis()))
    }

    suspend fun selectChapter(chapter: StoryChapter) {
        val current = dao.getProfileOnce() ?: OperatorProfileEntity()
        dao.upsertProfile(
            current.copy(
                selectedChapter = chapter.number.coerceAtMost(current.unlockedChapter),
                updatedAt = System.currentTimeMillis()
            )
        )
    }

    suspend fun selectOperator(operatorId: OperatorId) {
        val current = dao.getProfileOnce() ?: OperatorProfileEntity()
        dao.upsertProfile(current.copy(selectedOperatorId = operatorId.id, updatedAt = System.currentTimeMillis()))
    }

    suspend fun selectWeapon(weapon: WeaponId) {
        val current = dao.getProfileOnce() ?: OperatorProfileEntity()
        val updated = if (weapon.isPrimary) {
            current.copy(selectedPrimaryWeaponId = weapon.id, updatedAt = System.currentTimeMillis())
        } else {
            current.copy(selectedSecondaryWeaponId = weapon.id, updatedAt = System.currentTimeMillis())
        }
        dao.upsertProfile(updated)
    }

    suspend fun selectDistrict(district: CityDistrict) {
        val current = dao.getProfileOnce() ?: OperatorProfileEntity()
        dao.upsertProfile(current.copy(selectedDistrictId = district.id, updatedAt = System.currentTimeMillis()))
    }

    suspend fun updateSquadRoomCode(code: String) {
        val current = dao.getProfileOnce() ?: OperatorProfileEntity()
        val clean = code.trim().uppercase().take(8).ifBlank { "BIO-404" }
        dao.upsertProfile(current.copy(squadRoomCode = clean, updatedAt = System.currentTimeMillis()))
    }

    suspend fun cookMeal(recipe: CookingRecipe): Result<String> {
        val stash = dao.getStashOnce() ?: SurvivalStashEntity()
        if (stash.cannedBeef < recipe.cannedBeefCost ||
            stash.bioMushrooms < recipe.bioMushroomsCost ||
            stash.purifiedWater < recipe.purifiedWaterCost ||
            stash.wildHerbs < recipe.wildHerbsCost ||
            stash.scrapFuel < recipe.scrapFuelCost
        ) {
            return Result.failure(IllegalStateException("Missing field supplies for ${recipe.title}."))
        }
        val updated = stash.copy(
            cannedBeef = stash.cannedBeef - recipe.cannedBeefCost,
            bioMushrooms = stash.bioMushrooms - recipe.bioMushroomsCost,
            purifiedWater = stash.purifiedWater - recipe.purifiedWaterCost,
            wildHerbs = stash.wildHerbs - recipe.wildHerbsCost,
            scrapFuel = stash.scrapFuel - recipe.scrapFuelCost,
            stewCount = stash.stewCount + if (recipe == CookingRecipe.MRE_STEW) 1 else 0,
            stimBrothCount = stash.stimBrothCount + if (recipe == CookingRecipe.STIM_BROTH) 1 else 0
        )
        dao.upsertStash(updated)
        return Result.success("Prepared ${recipe.title}")
    }

    suspend fun consumeMeal(recipe: CookingRecipe, potencyMultiplier: Float): Result<Unit> {
        val stash = dao.getStashOnce() ?: SurvivalStashEntity()
        val hasMeal = when (recipe) {
            CookingRecipe.MRE_STEW -> stash.stewCount > 0
            CookingRecipe.STIM_BROTH -> stash.stimBrothCount > 0
        }
        if (!hasMeal) return Result.failure(IllegalStateException("No ${recipe.title} packed."))

        val updated = stash.copy(
            hunger = (stash.hunger + recipe.hungerRestore * potencyMultiplier).coerceAtMost(100f),
            health = (stash.health + recipe.healthRestore * potencyMultiplier).coerceAtMost(100f),
            stewCount = if (recipe == CookingRecipe.MRE_STEW) stash.stewCount - 1 else stash.stewCount,
            stimBrothCount = if (recipe == CookingRecipe.STIM_BROTH) stash.stimBrothCount - 1 else stash.stimBrothCount
        )
        dao.upsertStash(updated)
        return Result.success(Unit)
    }

    suspend fun addLootFromCity(kind: LootKind) {
        val stash = dao.getStashOnce() ?: SurvivalStashEntity()
        val updated = when (kind) {
            LootKind.AMMO_CRATE -> stash.copy(
                ammoReserve = (stash.ammoReserve + 90).coerceAtMost(420),
                scrapFuel = stash.scrapFuel + 1
            )
            LootKind.MED_RATION -> stash.copy(
                stewCount = stash.stewCount + 1,
                health = (stash.health + 35f).coerceAtMost(100f),
                hunger = (stash.hunger + 25f).coerceAtMost(100f)
            )
            LootKind.FUEL_SUPPLY -> stash.copy(
                cannedBeef = stash.cannedBeef + 1,
                purifiedWater = stash.purifiedWater + 1,
                scrapFuel = stash.scrapFuel + 1,
                ammoReserve = (stash.ammoReserve + 45).coerceAtMost(420)
            )
        }
        dao.upsertStash(updated)
    }

    suspend fun syncCombatVitals(
        hunger: Float,
        health: Float,
        ammoReserve: Int,
        stewCount: Int,
        stimBrothCount: Int,
        sharedSquadRations: Int
    ) {
        val stash = dao.getStashOnce() ?: SurvivalStashEntity()
        dao.upsertStash(
            stash.copy(
                hunger = hunger.coerceIn(0f, 100f),
                health = health.coerceIn(0f, 100f),
                ammoReserve = ammoReserve.coerceIn(0, 420),
                stewCount = stewCount.coerceAtLeast(0),
                stimBrothCount = stimBrothCount.coerceAtLeast(0),
                sharedSquadRations = sharedSquadRations.coerceAtLeast(0)
            )
        )
    }

    suspend fun recordDeploymentSummary(
        districtTitle: String,
        waveReached: Int,
        kills: Int,
        headshots: Int,
        completedChapter: Int?,
        extractedSafely: Boolean
    ) {
        val profile = dao.getProfileOnce() ?: OperatorProfileEntity()
        val xpGained = kills * 22 + headshots * 16 + waveReached * 65 + if (extractedSafely) 160 else 30
        val nextUnlocked = if (completedChapter != null && extractedSafely) {
            maxOf(profile.unlockedChapter, (completedChapter + 1).coerceAtMost(StoryChapter.entries.size))
        } else {
            profile.unlockedChapter
        }
        dao.upsertProfile(
            profile.copy(
                rankXp = profile.rankXp + xpGained,
                totalKills = profile.totalKills + kills,
                highestWave = maxOf(profile.highestWave, waveReached),
                unlockedChapter = nextUnlocked,
                selectedChapter = if (completedChapter != null && extractedSafely) nextUnlocked else profile.selectedChapter,
                updatedAt = System.currentTimeMillis()
            )
        )
        dao.insertDeploymentLog(
            SquadDeploymentLogEntity(
                districtTitle = districtTitle,
                waveReached = waveReached,
                kills = kills,
                headshots = headshots,
                mealsCooked = 0,
                extractedSafely = extractedSafely
            )
        )
    }
}
