package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.local.OperatorProfileEntity
import com.example.data.local.SquadDeploymentLogEntity
import com.example.data.local.SurvivalStashEntity
import com.example.data.repository.GameRepository
import com.example.engine.CombatHudState
import com.example.engine.CombatRenderSnapshot
import com.example.engine.GameSimulation
import com.example.model.CityDistrict
import com.example.model.CookingRecipe
import com.example.model.GameMode
import com.example.model.OperatorId
import com.example.model.StoryChapter
import com.example.model.WeaponId
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.random.Random

class GameViewModel(
    private val repository: GameRepository
) : ViewModel() {

    val profile: StateFlow<OperatorProfileEntity> = repository.profileFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), OperatorProfileEntity())

    val stash: StateFlow<SurvivalStashEntity> = repository.stashFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), SurvivalStashEntity())

    val deploymentLogs: StateFlow<List<SquadDeploymentLogEntity>> = repository.deploymentLogsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _bannerMessage = MutableStateFlow<String?>(null)
    val bannerMessage: StateFlow<String?> = _bannerMessage.asStateFlow()

    private val _combatHud = MutableStateFlow(CombatHudState())
    val combatHud: StateFlow<CombatHudState> = _combatHud.asStateFlow()

    private val _isCombatPaused = MutableStateFlow(false)
    val isCombatPaused: StateFlow<Boolean> = _isCombatPaused.asStateFlow()

    private var simulation: GameSimulation? = null
    private var simLoopJob: Job? = null
    private var summaryRecorded = false

    init {
        viewModelScope.launch {
            repository.ensureSeeded()
        }
    }

    fun clearBanner() {
        _bannerMessage.value = null
    }

    fun updateCallsign(callsign: String) {
        viewModelScope.launch {
            repository.updateCallsign(callsign)
            _bannerMessage.value = "Callsign updated."
        }
    }

    fun selectMode(mode: GameMode) {
        viewModelScope.launch {
            repository.selectMode(mode)
        }
    }

    fun selectChapter(chapter: StoryChapter) {
        viewModelScope.launch {
            repository.selectChapter(chapter)
        }
    }

    fun selectOperator(op: OperatorId) {
        viewModelScope.launch {
            repository.selectOperator(op)
        }
    }

    fun selectWeapon(weapon: WeaponId) {
        viewModelScope.launch {
            repository.selectWeapon(weapon)
        }
    }

    fun selectDistrict(district: CityDistrict) {
        viewModelScope.launch {
            repository.selectDistrict(district)
        }
    }

    fun generateNewSquadRoom() {
        val code = "BIO-${Random.nextInt(100, 999)}"
        viewModelScope.launch {
            repository.updateSquadRoomCode(code)
            _bannerMessage.value = "Squad frequency set to $code"
        }
    }

    fun joinSquadRoom(code: String) {
        viewModelScope.launch {
            repository.updateSquadRoomCode(code)
            _bannerMessage.value = "Synced tactical squad ${code.trim().uppercase()}"
        }
    }

    fun cookMeal(recipe: CookingRecipe) {
        viewModelScope.launch {
            val res = repository.cookMeal(recipe)
            _bannerMessage.value = res.getOrElse { it.message ?: "Unable to craft ration." }
        }
    }

    fun eatMeal(recipe: CookingRecipe) {
        viewModelScope.launch {
            val op = OperatorId.fromId(profile.value.selectedOperatorId)
            val res = repository.consumeMeal(recipe, op.mealPotency)
            _bannerMessage.value = if (res.isSuccess) {
                "Consumed ${recipe.title}."
            } else {
                res.exceptionOrNull()?.message ?: "No ration available."
            }
        }
    }

    fun startDeployment(modeOverride: GameMode? = null, chapterOverride: StoryChapter? = null) {
        val p = profile.value
        val s = stash.value
        val mode = modeOverride ?: GameMode.fromId(p.selectedModeId)
        val chapter = if (mode == GameMode.SINGLE_PLAYER) {
            chapterOverride ?: StoryChapter.fromNumber(p.selectedChapter)
        } else null
        val op = OperatorId.fromId(p.selectedOperatorId)
        val primary = WeaponId.fromId(p.selectedPrimaryWeaponId)
        val secondary = WeaponId.fromId(p.selectedSecondaryWeaponId)
        val district = chapter?.district ?: CityDistrict.fromId(p.selectedDistrictId)

        summaryRecorded = false
        _isCombatPaused.value = false
        val sim = GameSimulation(mode, chapter, op, primary, secondary, district, s)
        simulation = sim
        _combatHud.value = sim.step(0.016f)

        simLoopJob?.cancel()
        simLoopJob = viewModelScope.launch {
            var lastNs = System.nanoTime()
            while (isActive) {
                delay(16L)
                val nowNs = System.nanoTime()
                val dt = ((nowNs - lastNs) / 1_000_000_000f).coerceIn(0.004f, 0.05f)
                lastNs = nowNs
                if (!_isCombatPaused.value) {
                    val state = sim.step(dt)
                    _combatHud.value = state
                    if ((state.isGameOver || state.isExtracted) && !summaryRecorded) {
                        summaryRecorded = true
                        persistMatchResults(state)
                    }
                }
            }
        }
    }

    fun currentRenderSnapshot(): CombatRenderSnapshot =
        simulation?.renderSnapshot ?: CombatRenderSnapshot()

    fun setCombatPaused(paused: Boolean) {
        _isCombatPaused.value = paused
    }

    fun setMoveInput(x: Float, y: Float, sprint: Boolean) {
        simulation?.setMoveInput(x, y, sprint)
    }

    fun addLookDelta(yawRad: Float, pitchRad: Float) {
        simulation?.addLookDelta(yawRad, pitchRad)
    }

    fun jump() {
        simulation?.jump()
    }

    fun setAds(enabled: Boolean) {
        simulation?.setAds(enabled)
    }

    fun setFiring(held: Boolean) {
        simulation?.setFiring(held)
    }

    fun triggerSlide() {
        simulation?.triggerSlide()
    }

    fun swapWeapon() {
        simulation?.swapWeapon()
    }

    fun reloadWeapon() {
        simulation?.startReload()
    }

    fun sendSquadPing(label: String) {
        simulation?.sendSquadPing(label)
    }

    fun quickEatRation() {
        simulation?.consumePreparedRation()
    }

    fun scavengeNearby() {
        val kind = simulation?.collectNearbyInteractive() ?: return
        viewModelScope.launch {
            repository.addLootFromCity(kind)
        }
    }

    fun extractFromCombat() {
        simulation?.extractSafely()
    }

    fun stopDeployment() {
        simLoopJob?.cancel()
        simLoopJob = null
        val state = _combatHud.value
        if (!summaryRecorded && state.kills > 0) {
            summaryRecorded = true
            viewModelScope.launch {
                persistMatchResults(state)
            }
        }
        simulation = null
    }

    private suspend fun persistMatchResults(state: CombatHudState) {
        repository.syncCombatVitals(
            hunger = if (state.isGameOver) 85f else state.hunger,
            health = if (state.isGameOver) 100f else state.health,
            ammoReserve = state.ammoReserve.coerceAtLeast(150),
            stewCount = stash.value.stewCount,
            stimBrothCount = stash.value.stimBrothCount,
            sharedSquadRations = stash.value.sharedSquadRations
        )
        repository.recordDeploymentSummary(
            districtTitle = state.storyChapter?.title ?: state.activeDistrict.title,
            waveReached = state.waveNumber,
            kills = state.kills,
            headshots = state.headshots,
            completedChapter = state.storyChapter?.number,
            extractedSafely = state.isExtracted
        )
    }

    override fun onCleared() {
        super.onCleared()
        simLoopJob?.cancel()
    }

    companion object {
        fun factory(repository: GameRepository): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return GameViewModel(repository) as T
                }
            }
    }
}
