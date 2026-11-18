package com.example.engine

import com.example.data.local.SurvivalStashEntity
import com.example.model.BulletTracer
import com.example.model.CityDistrict
import com.example.model.CityProp
import com.example.model.CityWorldMap
import com.example.model.GameMode
import com.example.model.LootKind
import com.example.model.OperatorId
import com.example.model.PhysicsParticle
import com.example.model.PropKind
import com.example.model.RadioCallout
import com.example.model.SquadMateState
import com.example.model.StoryChapter
import com.example.model.WeaponId
import com.example.model.WorldLootNode
import com.example.model.ZombieState
import com.example.model.ZombieType
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.random.Random

enum class HitMarkerKind { NONE, BODY, HEADSHOT, KILL }

data class CombatRenderSnapshot(
    val playerX: Float = 16.5f * CityWorldMap.CELL_SIZE,
    val playerY: Float = 0f,
    val playerZ: Float = 24.5f * CityWorldMap.CELL_SIZE,
    val yaw: Float = 0f,
    val pitch: Float = 0f,
    val fovDegrees: Float = 68f,
    val walkCycle: Float = 0f,
    val moveIntensity: Float = 0f,
    val isSliding: Boolean = false,
    val muzzleFlash: Float = 0f,
    val screenShake: Float = 0f,
    val zombies: List<ZombieState> = emptyList(),
    val squadMates: List<SquadMateState> = emptyList(),
    val lootNodes: List<WorldLootNode> = emptyList(),
    val props: List<CityProp> = emptyList(),
    val particles: List<PhysicsParticle> = emptyList(),
    val tracers: List<BulletTracer> = emptyList()
)

data class CombatHudState(
    val gameMode: GameMode = GameMode.SINGLE_PLAYER,
    val storyChapter: StoryChapter? = StoryChapter.CHAPTER_1,
    val storyObjectiveText: String = "",
    val beaconsCollected: Int = 0,
    val beaconsTarget: Int = 0,
    val killsTarget: Int = 0,
    val bossActive: Boolean = false,
    val bossDefeated: Boolean = false,
    val activeDistrict: CityDistrict = CityDistrict.NEON_MARKET,
    val waveNumber: Int = 1,
    val zombiesRemainingInWave: Int = 6,
    val safehouseIntegrity: Float = 100f,
    val health: Float = 100f,
    val maxHealth: Float = 100f,
    val hunger: Float = 92f,
    val stamina: Float = 100f,
    val stimBuffSec: Float = 0f,
    val isGrounded: Boolean = true,
    val isSprinting: Boolean = false,
    val activeWeapon: WeaponId = WeaponId.M4A1_SOPMOD,
    val secondaryWeapon: WeaponId = WeaponId.P226_TAC,
    val primaryWeapon: WeaponId = WeaponId.M4A1_SOPMOD,
    val usingPrimary: Boolean = true,
    val clipAmmo: Int = 30,
    val ammoReserve: Int = 210,
    val isReloading: Boolean = false,
    val reloadProgress: Float = 0f,
    val isAds: Boolean = false,
    val recoilKickUi: Float = 0f,
    val hitMarker: HitMarkerKind = HitMarkerKind.NONE,
    val damageFlashAlpha: Float = 0f,
    val kills: Int = 0,
    val headshots: Int = 0,
    val nearLootNode: WorldLootNode? = null,
    val nearBeacon: CityProp? = null,
    val preparedRationsCount: Int = 2,
    val radioCallouts: List<RadioCallout> = emptyList(),
    val squadMates: List<SquadMateState> = emptyList(),
    val zombiesForRadar: List<ZombieState> = emptyList(),
    val lootForRadar: List<WorldLootNode> = emptyList(),
    val propsForRadar: List<CityProp> = emptyList(),
    val playerX: Float = 0f,
    val playerZ: Float = 0f,
    val playerYaw: Float = 0f,
    val isGameOver: Boolean = false,
    val isExtracted: Boolean = false,
    val statusToast: String? = null
)

class GameSimulation(
    private val gameMode: GameMode,
    private val storyChapter: StoryChapter?,
    private val operator: OperatorId,
    private val primaryWeapon: WeaponId,
    private val secondaryWeapon: WeaponId,
    startDistrict: CityDistrict,
    initialStash: SurvivalStashEntity
) {
    private val lock = Any()
    private val rng = Random(System.currentTimeMillis())

    private val initialGridX = storyChapter?.spawnGridX ?: startDistrict.spawnGridX
    private val initialGridZ = storyChapter?.spawnGridZ ?: startDistrict.spawnGridZ

    private var playerX = (initialGridX + 0.5f) * CityWorldMap.CELL_SIZE
    private var playerY = 0f
    private var playerZ = (initialGridZ + 0.5f) * CityWorldMap.CELL_SIZE
    private var velX = 0f
    private var velY = 0f
    private var velZ = 0f
    private var isGrounded = true
    private var yaw = 0f
    private var pitch = 0f
    private var fov = 68f
    private var walkCycle = 0f
    private var moveIntensity = 0f

    private val maxHealth = 100f + operator.bonusHp
    private var health = maxHealth
    private var hunger = initialStash.hunger.coerceIn(55f, 100f)
    private var stamina = 100f
    private var stimBuffSec = 0f

    private var usingPrimary = true
    private var primaryClip = primaryWeapon.magazineSize
    private var secondaryClip = secondaryWeapon.magazineSize
    private var ammoReserve = initialStash.ammoReserve.coerceAtLeast(180)
    private var fireCooldown = 0f
    private var reloadTimer = 0f
    private var isReloading = false
    private var isAds = false
    private var isFiringHeld = false
    private var slideTimer = 0f
    private var muzzleFlash = 0f
    private var recoilPitch = 0f
    private var screenShake = 0f
    private var hitMarker = HitMarkerKind.NONE
    private var hitMarkerTimer = 0f
    private var damageFlash = 0f

    private var moveInputX = 0f
    private var moveInputY = 0f
    private var isSprintInput = false

    private var waveNumber = 1
    private var wavePendingSpawns = storyChapter?.targetKills ?: 7
    private var spawnTimer = 0.8f
    private var nextZombieUid = 1
    private var kills = 0
    private var headshots = 0
    private var beaconsCollected = 0
    private var bossSpawned = false
    private var bossDefeated = false
    private var storyBeatIndex = 0
    private var missionElapsedSec = 0f

    private var safehouseIntegrity = 100f
    private var statusToast: String? = null
    private var toastTimer = 0f
    private var stewCount = initialStash.stewCount
    private var stimBrothCount = initialStash.stimBrothCount

    private var isGameOver = false
    private var isExtracted = false

    private val zombies = mutableListOf<ZombieState>()
    private val lootNodes = CityWorldMap.initialLootNodes().toMutableList()
    private val props = CityWorldMap.initialProps(if (gameMode == GameMode.SINGLE_PLAYER) storyChapter else null).toMutableList()
    private val particles = mutableListOf<PhysicsParticle>()
    private val tracers = mutableListOf<BulletTracer>()
    private val callouts = mutableListOf<RadioCallout>()

    private val squadMates = if (gameMode == GameMode.COOP_SQUAD) {
        mutableListOf(
            SquadMateState("mate_viper", "VIPER-7", OperatorId.VIPER, playerX - 2.6f, playerZ - 1.5f, 0f, 105f, 105f, 0),
            SquadMateState("mate_kestrel", "KESTREL", OperatorId.KESTREL, playerX + 2.6f, playerZ - 1.5f, 0f, 110f, 110f, 0),
            SquadMateState("mate_bulwark", "BULWARK", OperatorId.BULWARK, CityWorldMap.CAMP_STOVE_X + 1.8f, CityWorldMap.CAMP_STOVE_Z - 3.5f, 0f, 140f, 140f, 0)
        )
    } else {
        mutableListOf()
    }

    init {
        if (gameMode == GameMode.SINGLE_PLAYER && storyChapter != null) {
            pushCallout("OVERWATCH", "${storyChapter.title}: ${storyChapter.subtitle}", isAlert = true)
        } else {
            pushCallout("COMMAND", "Co-Op Squad deployed to ${startDistrict.title}. Hold the city!")
        }
    }

    @Volatile
    var renderSnapshot = CombatRenderSnapshot()
        private set

    fun setMoveInput(x: Float, y: Float, sprint: Boolean) = synchronized(lock) {
        moveInputX = x.coerceIn(-1f, 1f)
        moveInputY = y.coerceIn(-1f, 1f)
        isSprintInput = sprint
    }

    fun addLookDelta(deltaYawRad: Float, deltaPitchRad: Float) = synchronized(lock) {
        val sens = if (isAds) 0.52f else 1.0f
        yaw += deltaYawRad * sens
        pitch = (pitch + deltaPitchRad * sens).coerceIn(-0.55f, 0.55f)
    }

    fun jump() = synchronized(lock) {
        if (isGrounded && stamina > 8f) {
            velY = 7.4f
            isGrounded = false
            stamina = (stamina - 10f).coerceAtLeast(0f)
        }
    }

    fun setAds(enabled: Boolean) = synchronized(lock) {
        isAds = enabled
    }

    fun setFiring(held: Boolean) = synchronized(lock) {
        isFiringHeld = held
    }

    fun triggerSlide() = synchronized(lock) {
        if (slideTimer <= 0f && isGrounded && stamina > 14f) {
            slideTimer = 0.42f
            val fwdX = sin(yaw)
            val fwdZ = -cos(yaw)
            velX += fwdX * 6.5f
            velZ += fwdZ * 6.5f
            stamina = (stamina - 16f).coerceAtLeast(0f)
        }
    }

    fun swapWeapon() = synchronized(lock) {
        if (isReloading) {
            isReloading = false
            reloadTimer = 0f
        }
        usingPrimary = !usingPrimary
        showToast("Equipped ${currentWeapon().displayName}")
    }

    fun startReload() = synchronized(lock) {
        val weapon = currentWeapon()
        val clip = currentClip()
        if (!isReloading && clip < weapon.magazineSize && ammoReserve > 0) {
            isReloading = true
            val buff = if (stimBuffSec > 0f) 0.65f else 1f
            reloadTimer = weapon.reloadSec * operator.reloadMultiplier * buff
        }
    }

    fun sendSquadPing(label: String) = synchronized(lock) {
        val district = CityWorldMap.districtAtWorld(playerX, playerZ).title
        pushCallout(operator.callsign, "$label @ $district", isAlert = true)
        showToast("Radio: $label")
    }

    fun consumePreparedRation(): Boolean = synchronized(lock) {
        val potency = operator.mealPotency
        when {
            stewCount > 0 -> {
                stewCount--
                hunger = (hunger + 45f * potency).coerceAtMost(100f)
                health = (health + 35f * potency).coerceAtMost(maxHealth)
                showToast("Used Field Ration (+35 HP / Stamina)")
                true
            }
            stimBrothCount > 0 -> {
                stimBrothCount--
                hunger = (hunger + 30f * potency).coerceAtMost(100f)
                health = (health + 25f * potency).coerceAtMost(maxHealth)
                stimBuffSec = 45f
                showToast("Adrenaline Stim Active (45s)")
                true
            }
            else -> {
                showToast("No rations left — loot Med Crates in city")
                false
            }
        }
    }

    fun collectNearbyInteractive(): LootKind? = synchronized(lock) {
        // 1. Check Story Beacon first
        val bcnIdx = props.indexOfFirst { it.active && it.kind == PropKind.STORY_BEACON && dist(it.x, it.z, playerX, playerZ) < 2.9f }
        if (bcnIdx >= 0) {
            props[bcnIdx] = props[bcnIdx].copy(active = false)
            beaconsCollected++
            spawnExplosionParticles(props[bcnIdx].x, 0.8f, props[bcnIdx].z, 10, 0f, 0.9f, 1f)
            showToast("BLACKBOX SECURED ($beaconsCollected/${storyChapter?.targetBeacons ?: 1})")
            pushCallout("OVERWATCH", "Blackbox telemetry uploaded. Keep pushing!", isAlert = false)
            checkStoryCompletion()
            return null
        }

        // 2. Check Loot Crate
        val idx = lootNodes.indexOfFirst { !it.collected && dist(it.x, it.z, playerX, playerZ) < 2.7f }
        if (idx < 0) return null
        val node = lootNodes[idx]
        lootNodes[idx] = node.copy(collected = true, respawnTimer = 42f)
        when (node.kind) {
            LootKind.AMMO_CRATE -> ammoReserve = (ammoReserve + 90).coerceAtMost(420)
            LootKind.MED_RATION -> {
                stewCount++
                health = (health + 35f).coerceAtMost(maxHealth)
                hunger = (hunger + 25f).coerceAtMost(100f)
            }
            LootKind.FUEL_SUPPLY -> {
                ammoReserve = (ammoReserve + 45).coerceAtMost(420)
                stamina = 100f
            }
        }
        showToast("Picked up ${node.kind.label}")
        return node.kind
    }

    fun extractSafely() = synchronized(lock) {
        isExtracted = true
    }

    fun step(dtRaw: Float): CombatHudState = synchronized(lock) {
        val dt = dtRaw.coerceIn(0.001f, 0.05f)
        if (isGameOver || isExtracted) return buildHudState()

        missionElapsedSec += dt
        updateStoryRadioBeats()

        // Decay visual/physics timers
        if (muzzleFlash > 0f) muzzleFlash = (muzzleFlash - dt * 12f).coerceAtLeast(0f)
        if (recoilPitch > 0f) recoilPitch = (recoilPitch - dt * 0.24f).coerceAtLeast(0f)
        if (screenShake > 0f) screenShake = (screenShake - dt * 3.4f).coerceAtLeast(0f)
        if (damageFlash > 0f) damageFlash = (damageFlash - dt * 2.2f).coerceAtLeast(0f)
        if (hitMarkerTimer > 0f) {
            hitMarkerTimer -= dt
            if (hitMarkerTimer <= 0f) hitMarker = HitMarkerKind.NONE
        }
        if (toastTimer > 0f) {
            toastTimer -= dt
            if (toastTimer <= 0f) statusToast = null
        }
        if (stimBuffSec > 0f) stimBuffSec = (stimBuffSec - dt).coerceAtLeast(0f)

        // Balanced hunger & stamina physics
        val isMoving = (moveInputX * moveInputX + moveInputY * moveInputY) > 0.04f
        val canSprint = isSprintInput && isMoving && stamina > 5f && !isAds
        hunger = (hunger - (if (canSprint) 0.42f else 0.14f) * operator.sprintEfficiency * dt).coerceAtLeast(0f)
        if (canSprint) {
            stamina = (stamina - 15f * dt).coerceAtLeast(0f)
        } else {
            stamina = (stamina + 18f * dt).coerceAtMost(100f)
        }

        // Player 3D Rigid-Body Momentum & Gravity Physics
        val targetSpeed = when {
            slideTimer > 0f -> 9.2f
            canSprint -> 7.2f
            isAds -> 3.1f
            else -> 5.0f
        } * (if (stimBuffSec > 0f) 1.18f else 1f)
        if (slideTimer > 0f) slideTimer = (slideTimer - dt).coerceAtLeast(0f)

        val forwardX = sin(yaw)
        val forwardZ = -cos(yaw)
        val rightX = cos(yaw)
        val rightZ = sin(yaw)
        val wishX = forwardX * moveInputY + rightX * moveInputX
        val wishZ = forwardZ * moveInputY + rightZ * moveInputX
        val wishMag = sqrt(wishX * wishX + wishZ * wishZ)

        val accel = if (isGrounded) 14f else 4.5f
        val friction = if (isGrounded) 10f else 1.5f
        if (wishMag > 0.02f) {
            val targetVx = (wishX / wishMag) * targetSpeed
            val targetVz = (wishZ / wishMag) * targetSpeed
            velX += (targetVx - velX) * (accel * dt).coerceAtMost(1f)
            velZ += (targetVz - velZ) * (accel * dt).coerceAtMost(1f)
        } else {
            velX -= velX * (friction * dt).coerceAtMost(1f)
            velZ -= velZ * (friction * dt).coerceAtMost(1f)
        }

        // Gravity & Jump landing
        if (!isGrounded || velY > 0f) {
            velY -= 22f * dt
            playerY += velY * dt
            if (playerY <= 0f) {
                playerY = 0f
                velY = 0f
                isGrounded = true
            }
        }

        // Wall + Prop Collision
        val nextX = playerX + velX * dt
        val tryX = CityWorldMap.clampOutsideWall(nextX, playerZ)
        if (!tryX.first.isNaN() && !collidesWithSolidProp(tryX.first, playerZ, 0.55f)) {
            playerX = tryX.first
        } else {
            velX = 0f
        }
        val nextZ = playerZ + velZ * dt
        val tryZ = CityWorldMap.clampOutsideWall(playerX, nextZ)
        if (!tryZ.second.isNaN() && !collidesWithSolidProp(playerX, tryZ.second, 0.55f)) {
            playerZ = tryZ.second
        } else {
            velZ = 0f
        }

        val speedHoriz = sqrt(velX * velX + velZ * velZ)
        if (speedHoriz > 0.4f && isGrounded) {
            walkCycle += dt * (speedHoriz * 1.9f)
            moveIntensity = (speedHoriz / 6.5f).coerceIn(0f, 1f)
        } else {
            moveIntensity = (moveIntensity - dt * 6f).coerceAtLeast(0f)
        }

        val targetFov = if (isAds) 44f else if (canSprint) 75f else 68f
        fov += (targetFov - fov) * (dt * 12f).coerceAtMost(1f)

        // Weapons & Reload
        if (isReloading) {
            reloadTimer -= dt
            if (reloadTimer <= 0f) finishReload()
        }
        if (fireCooldown > 0f) fireCooldown = (fireCooldown - dt).coerceAtLeast(0f)
        if (isFiringHeld && !isReloading && fireCooldown <= 0f) {
            fireCurrentWeapon()
        }

        // Physics Particles (Gravity + Ground Bounce!)
        updateParticles(dt)
        updateTracers(dt)
        updateLootRespawns(dt)
        updateSpawning(dt)
        updateSquadMates(dt)
        updateZombies(dt)

        if (health <= 0f) {
            health = 0f
            isGameOver = true
        }

        renderSnapshot = CombatRenderSnapshot(
            playerX = playerX,
            playerY = playerY,
            playerZ = playerZ,
            yaw = yaw,
            pitch = pitch + recoilPitch,
            fovDegrees = fov,
            walkCycle = walkCycle,
            moveIntensity = moveIntensity,
            isSliding = slideTimer > 0f,
            muzzleFlash = muzzleFlash,
            screenShake = screenShake,
            zombies = zombies.toList(),
            squadMates = squadMates.toList(),
            lootNodes = lootNodes.toList(),
            props = props.toList(),
            particles = particles.toList(),
            tracers = tracers.toList()
        )

        return buildHudState()
    }

    private fun collidesWithSolidProp(wx: Float, wz: Float, radius: Float): Boolean {
        if (playerY > 1.0f) return false // Can jump over concrete barriers!
        for (p in props) {
            if (!p.active) continue
            val r = when (p.kind) {
                PropKind.WRECKED_CAR -> 1.35f
                PropKind.CONCRETE_BARRIER -> 0.85f
                PropKind.EXPLOSIVE_BARREL -> 0.45f
                PropKind.STORY_BEACON -> 0f
            }
            if (r > 0f && dist(wx, wz, p.x, p.z) < (r + radius)) return true
        }
        return false
    }

    private fun fireCurrentWeapon() {
        val weapon = currentWeapon()
        val clip = currentClip()
        if (clip <= 0) {
            startReload()
            return
        }
        setCurrentClip(clip - 1)
        fireCooldown = weapon.fireIntervalSec
        muzzleFlash = 1.0f
        recoilPitch = (recoilPitch + weapon.recoilKick * (if (isAds) 0.52f else 1f)).coerceAtMost(0.18f)

        val spread = if (isAds) weapon.adsSpreadRad else weapon.hipSpreadRad
        val eyeHeight = playerY + (if (slideTimer > 0f) 1.02f else 1.66f)

        repeat(weapon.pellets) {
            val rayYaw = yaw + (rng.nextFloat() - 0.5f) * spread * 2f
            val rayPitch = pitch + recoilPitch + (rng.nextFloat() - 0.5f) * spread * 2f
            val dx = sin(rayYaw) * cos(rayPitch)
            val dy = sin(rayPitch)
            val dz = -cos(rayYaw) * cos(rayPitch)

            // 1. Check Explosive Barrels first
            var bestBarrelIdx = -1
            var bestBarrelT = 50f
            for (i in props.indices) {
                val p = props[i]
                if (!p.active || p.kind != PropKind.EXPLOSIVE_BARREL) continue
                val rx = p.x - playerX
                val rz = p.z - playerZ
                val proj = rx * dx + rz * dz
                if (proj <= 0.4f || proj > bestBarrelT) continue
                val perpX = rx - dx * proj
                val perpZ = rz - dz * proj
                if (perpX * perpX + perpZ * perpZ <= 0.48f * 0.48f) {
                    val hitY = eyeHeight + dy * proj
                    if (hitY in 0f..1.2f && CityWorldMap.hasLineOfSight(playerX, playerZ, p.x, p.z)) {
                        bestBarrelT = proj
                        bestBarrelIdx = i
                    }
                }
            }

            // 2. Check Living Zombies
            var bestZIdx = -1
            var bestT = bestBarrelT
            var hitHead = false
            for (i in zombies.indices) {
                val z = zombies[i]
                if (z.isDying) continue
                val rx = z.x - playerX
                val rz = z.z - playerZ
                val proj = rx * dx + rz * dz
                if (proj <= 0.4f || proj > bestT) continue
                val perpX = rx - dx * proj
                val perpZ = rz - dz * proj
                val hitRadius = when (z.type) {
                    ZombieType.BOSS -> 1.05f
                    ZombieType.BRUTE -> 0.85f
                    else -> 0.58f
                }
                if (perpX * perpX + perpZ * perpZ <= hitRadius * hitRadius) {
                    val hitY = eyeHeight + dy * proj
                    if (hitY in 0.05f..(z.type.scaleHeight + 0.25f)) {
                        if (CityWorldMap.hasLineOfSight(playerX, playerZ, z.x, z.z)) {
                            bestT = proj
                            bestZIdx = i
                            hitHead = hitY >= z.type.scaleHeight * 0.68f
                        }
                    }
                }
            }

            val endX = playerX + dx * bestT
            val endY = (eyeHeight + dy * bestT).coerceIn(0.1f, 6f)
            val endZ = playerZ + dz * bestT
            tracers.add(BulletTracer(playerX + cos(yaw) * 0.16f, eyeHeight - 0.15f, playerZ + sin(yaw) * 0.16f, endX, endY, endZ, 0, 0.10f))

            if (bestZIdx >= 0) {
                val target = zombies[bestZIdx]
                val dmg = weapon.damage * (if (hitHead) weapon.headshotMult else 1f)
                val newHp = target.hp - dmg
                val kbX = dx * weapon.knockbackForce
                val kbZ = dz * weapon.knockbackForce
                spawnExplosionParticles(target.x, if (hitHead) 1.6f else 1.1f, target.z, 5, 0.85f, 0.15f, 0.2f)

                if (newHp <= 0f) {
                    zombies[bestZIdx] = target.copy(
                        hp = 0f,
                        vx = kbX * 1.4f,
                        vz = kbZ * 1.4f,
                        isDying = true,
                        fallAngleDeg = 12f,
                        deathTimer = 0f
                    )
                    kills++
                    if (hitHead) headshots++
                    if (target.type == ZombieType.BOSS) bossDefeated = true
                    ammoReserve = (ammoReserve + 8).coerceAtMost(420)
                    hitMarker = HitMarkerKind.KILL
                    hitMarkerTimer = 0.25f
                    checkStoryCompletion()
                } else {
                    zombies[bestZIdx] = target.copy(
                        hp = newHp,
                        vx = target.vx + kbX,
                        vz = target.vz + kbZ,
                        hitFlashTimer = 0.14f
                    )
                    if (hitMarker != HitMarkerKind.KILL) {
                        hitMarker = if (hitHead) HitMarkerKind.HEADSHOT else HitMarkerKind.BODY
                        hitMarkerTimer = 0.16f
                    }
                }
            } else if (bestBarrelIdx >= 0) {
                detonateBarrel(bestBarrelIdx)
            }
        }

        if (currentClip() == 0 && ammoReserve > 0) startReload()
    }

    private fun detonateBarrel(propIdx: Int) {
        val barrel = props[propIdx]
        if (!barrel.active) return
        props[propIdx] = barrel.copy(active = false)
        screenShake = 1.0f
        showToast("FUEL BARREL DETONATED!")
        spawnExplosionParticles(barrel.x, 0.7f, barrel.z, 26, 1.0f, 0.45f, 0.08f)

        // Radial Blast Physics + Damage on Zombies
        for (i in zombies.indices) {
            val z = zombies[i]
            if (z.isDying) continue
            val dx = z.x - barrel.x
            val dz = z.z - barrel.z
            val d = sqrt(dx * dx + dz * dz).coerceAtLeast(0.2f)
            if (d < 7.8f) {
                val falloff = 1f - (d / 7.8f)
                val dmg = 195f * falloff
                val kb = 12f * falloff
                val newHp = z.hp - dmg
                if (newHp <= 0f) {
                    zombies[i] = z.copy(hp = 0f, vx = (dx / d) * kb, vz = (dz / d) * kb, isDying = true, fallAngleDeg = 15f)
                    kills++
                    if (z.type == ZombieType.BOSS) bossDefeated = true
                    hitMarker = HitMarkerKind.KILL
                    hitMarkerTimer = 0.30f
                } else {
                    zombies[i] = z.copy(hp = newHp, vx = (dx / d) * kb, vz = (dz / d) * kb, hitFlashTimer = 0.2f)
                }
            }
        }
        checkStoryCompletion()
    }

    private fun spawnExplosionParticles(x: Float, y: Float, z: Float, count: Int, r: Float, g: Float, b: Float) {
        repeat(count) {
            val angle = rng.nextFloat() * 6.283f
            val speed = 2.0f + rng.nextFloat() * 6.5f
            particles.add(
                PhysicsParticle(
                    x = x,
                    y = y,
                    z = z,
                    vx = cos(angle) * speed,
                    vy = 2.5f + rng.nextFloat() * 6.5f,
                    vz = sin(angle) * speed,
                    size = 0.12f + rng.nextFloat() * 0.16f,
                    r = r,
                    g = g,
                    b = b,
                    ttl = 0.55f + rng.nextFloat() * 0.45f,
                    maxTtl = 1.0f
                )
            )
        }
    }

    private fun updateParticles(dt: Float) {
        val iter = particles.listIterator()
        while (iter.hasNext()) {
            val p = iter.next()
            val rem = p.ttl - dt
            if (rem <= 0f) {
                iter.remove()
            } else {
                var ny = p.y + p.vy * dt
                var nvy = p.vy - 18f * dt
                var nvx = p.vx * (1f - 1.5f * dt)
                var nvz = p.vz * (1f - 1.5f * dt)
                if (ny <= 0.06f) {
                    ny = 0.06f
                    nvy = -nvy * 0.42f
                    nvx *= 0.7f
                    nvz *= 0.7f
                }
                iter.set(p.copy(x = p.x + nvx * dt, y = ny, z = p.z + nvz * dt, vx = nvx, vy = nvy, vz = nvz, ttl = rem))
            }
        }
    }

    private fun updateTracers(dt: Float) {
        val iter = tracers.listIterator()
        while (iter.hasNext()) {
            val t = iter.next()
            if (t.ttl - dt <= 0f) iter.remove()
            else iter.set(t.copy(ttl = t.ttl - dt))
        }
    }

    private fun updateLootRespawns(dt: Float) {
        for (i in lootNodes.indices) {
            val n = lootNodes[i]
            if (n.collected) {
                val left = n.respawnTimer - dt
                lootNodes[i] = if (left <= 0f) n.copy(collected = false, respawnTimer = 0f) else n.copy(respawnTimer = left)
            }
        }
    }

    private fun updateSpawning(dt: Float) {
        val livingCount = zombies.count { !it.isDying }
        if (gameMode == GameMode.SINGLE_PLAYER && storyChapter != null) {
            if (storyChapter.hasBoss && !bossSpawned && kills >= (storyChapter.targetKills / 2)) {
                bossSpawned = true
                val bossType = if (storyChapter == StoryChapter.CHAPTER_4) ZombieType.BOSS else ZombieType.BRUTE
                val spawn = CityWorldMap.zombieSpawnPoints().maxByOrNull { dist(it.first, it.second, playerX, playerZ) } ?: (16.5f * 3.2f to 6.5f * 3.2f)
                zombies.add(
                    ZombieState(
                        uid = nextZombieUid++,
                        type = bossType,
                        x = spawn.first,
                        z = spawn.second,
                        hp = bossType.maxHp,
                        maxHp = bossType.maxHp
                    )
                )
                pushCallout("OVERWATCH", "WARNING: ${bossType.label.uppercase()} BREACH! Aim for the head!", isAlert = true)
            }
            if (livingCount < 8 && (wavePendingSpawns > 0 || kills < storyChapter.targetKills)) {
                spawnTimer -= dt
                if (spawnTimer <= 0f) {
                    spawnZombie(storyChapter.number)
                    if (wavePendingSpawns > 0) wavePendingSpawns--
                    spawnTimer = 1.6f
                }
            }
        } else {
            if (wavePendingSpawns > 0 && livingCount < 12) {
                spawnTimer -= dt
                if (spawnTimer <= 0f) {
                    spawnZombie(waveNumber)
                    wavePendingSpawns--
                    spawnTimer = (1.9f - waveNumber * 0.1f).coerceAtLeast(0.7f)
                }
            } else if (wavePendingSpawns == 0 && livingCount == 0) {
                waveNumber++
                wavePendingSpawns = 6 + waveNumber * 3
                spawnTimer = 2.0f
                pushCallout("COMMAND", "WAVE $waveNumber INCOMING — Bio-signatures spiking!", isAlert = true)
                showToast("WAVE $waveNumber STARTED")
            }
        }
    }

    private fun spawnZombie(difficultyTier: Int) {
        val spawns = CityWorldMap.zombieSpawnPoints()
        val chosen = spawns.filter { dist(it.first, it.second, playerX, playerZ) > 10f }.randomOrNull(rng) ?: spawns.random(rng)
        val roll = rng.nextFloat()
        val type = when {
            difficultyTier >= 3 && roll < 0.18f -> ZombieType.BRUTE
            difficultyTier >= 2 && roll < 0.42f -> ZombieType.SPITTER
            roll < 0.68f -> ZombieType.STALKER
            else -> ZombieType.SHAMBLER
        }
        val hp = type.maxHp * (1f + (difficultyTier - 1) * 0.12f)
        zombies.add(ZombieState(nextZombieUid++, type, chosen.first, chosen.second, hp = hp, maxHp = hp))
    }

    private fun updateSquadMates(dt: Float) {
        for (i in squadMates.indices) {
            val m = squadMates[i]
            val flash = (m.fireFlashTimer - dt).coerceAtLeast(0f)
            val anchorX = if (m.id == "mate_bulwark") CityWorldMap.CAMP_STOVE_X + 2.5f else playerX + (i - 0.5f) * 3.0f
            val anchorZ = if (m.id == "mate_bulwark") CityWorldMap.CAMP_STOVE_Z - 4.0f else playerZ + 2.2f

            val distAnchor = dist(m.x, m.z, anchorX, anchorZ)
            var nx = m.x
            var nz = m.z
            var anim = m.animPhase
            var myaw = m.yaw

            if (distAnchor > 2.4f) {
                val angle = atan2(anchorZ - m.z, anchorX - m.x)
                myaw = atan2(cos(angle), -sin(angle))
                val safe = CityWorldMap.clampOutsideWall(m.x + cos(angle) * 4.0f * dt, m.z + sin(angle) * 4.0f * dt, 0.45f)
                if (!safe.first.isNaN()) {
                    nx = safe.first
                    nz = safe.second
                    anim += dt * 9f
                }
            }

            var newKills = m.kills
            var newFlash = flash
            val nearestIdx = zombies.indices.minByOrNull { idx ->
                val z = zombies[idx]
                if (z.isDying) 999f else {
                    val d = dist(nx, nz, z.x, z.z)
                    if (d < 22f && CityWorldMap.hasLineOfSight(nx, nz, z.x, z.z)) d else 999f
                }
            }
            if (nearestIdx != null) {
                val target = zombies[nearestIdx]
                myaw = atan2(target.x - nx, -(target.z - nz))
                if (!target.isDying && rng.nextFloat() < dt * 2.0f && dist(nx, nz, target.x, target.z) < 22f && CityWorldMap.hasLineOfSight(nx, nz, target.x, target.z)) {
                    newFlash = 0.12f
                    tracers.add(BulletTracer(nx, 1.32f, nz, target.x, 1.25f, target.z, 1, 0.12f))
                    val newHp = target.hp - 18f
                    if (newHp <= 0f) {
                        zombies[nearestIdx] = target.copy(hp = 0f, isDying = true, fallAngleDeg = 15f)
                        newKills++
                        kills++
                    } else {
                        zombies[nearestIdx] = target.copy(hp = newHp, hitFlashTimer = 0.1f)
                    }
                }
            }

            squadMates[i] = m.copy(x = nx, z = nz, yaw = myaw, kills = newKills, animPhase = anim, fireFlashTimer = newFlash)
        }
    }

    private fun updateZombies(dt: Float) {
        val iter = zombies.listIterator()
        while (iter.hasNext()) {
            val z = iter.next()
            if (z.isDying) {
                val nextDeath = z.deathTimer + dt
                if (nextDeath >= 1.4f) {
                    iter.remove()
                } else {
                    val fall = (z.fallAngleDeg + dt * 240f).coerceAtMost(90f)
                    val safe = CityWorldMap.clampOutsideWall(z.x + z.vx * dt, z.z + z.vz * dt, 0.42f)
                    iter.set(
                        z.copy(
                            x = if (!safe.first.isNaN()) safe.first else z.x,
                            z = if (!safe.second.isNaN()) safe.second else z.z,
                            vx = z.vx * (1f - 7f * dt),
                            vz = z.vz * (1f - 7f * dt),
                            fallAngleDeg = fall,
                            deathTimer = nextDeath
                        )
                    )
                }
                continue
            }

            val dPlayer = dist(z.x, z.z, playerX, playerZ)
            val dx = playerX - z.x
            val dz = playerZ - z.z
            val d = sqrt(dx * dx + dz * dz).coerceAtLeast(0.01f)
            val faceYaw = atan2(dx, -dz)

            var moveVx = if (d > 1.45f) (dx / d) * z.type.speed else 0f
            var moveVz = if (d > 1.45f) (dz / d) * z.type.speed else 0f
            val totalVx = moveVx + z.vx
            val totalVz = moveVz + z.vz

            var nx = z.x
            var nz = z.z
            val tryX = CityWorldMap.clampOutsideWall(z.x + totalVx * dt, z.z, 0.45f)
            if (!tryX.first.isNaN()) nx = tryX.first
            val tryZ = CityWorldMap.clampOutsideWall(nx, z.z + totalVz * dt, 0.45f)
            if (!tryZ.second.isNaN()) nz = tryZ.second

            var cd = (z.attackCooldown - dt).coerceAtLeast(0f)
            if (dPlayer < 1.85f && cd <= 0f) {
                health = (health - z.type.damage).coerceAtLeast(0f)
                damageFlash = 0.85f
                screenShake = 0.45f
                cd = 1.1f
            } else if (z.type == ZombieType.SPITTER && dPlayer < 13f && cd <= 0f && CityWorldMap.hasLineOfSight(nx, nz, playerX, playerZ)) {
                tracers.add(BulletTracer(nx, 1.38f, nz, playerX, 1.32f, playerZ, 2, 0.14f))
                health = (health - z.type.damage * 0.6f).coerceAtLeast(0f)
                damageFlash = 0.60f
                cd = 2.3f
            }

            iter.set(
                z.copy(
                    x = nx,
                    z = nz,
                    vx = z.vx * (1f - 8f * dt),
                    vz = z.vz * (1f - 8f * dt),
                    yaw = faceYaw,
                    attackCooldown = cd,
                    hitFlashTimer = (z.hitFlashTimer - dt).coerceAtLeast(0f),
                    animPhase = z.animPhase + dt * z.type.speed * 3.0f
                )
            )
        }
    }

    private fun updateStoryRadioBeats() {
        val ch = storyChapter ?: return
        if (gameMode != GameMode.SINGLE_PLAYER) return
        if (storyBeatIndex == 0 && missionElapsedSec > 8f) {
            storyBeatIndex = 1
            val line = when (ch) {
                StoryChapter.CHAPTER_1 -> "GHOSTLINE: Streets are littered with wreckage. Watch for explosive fuel drums."
                StoryChapter.CHAPTER_2 -> "OVERWATCH: Mercy Plaza bio-spitters ahead. Keep moving and use cover."
                StoryChapter.CHAPTER_3 -> "OVERWATCH: Convoy survivors didn't make it. Grab the drives and clear the Brute."
                StoryChapter.CHAPTER_4 -> "EVAC-1: Chopper inbound! Hold the Helipad until Patient Zero drops!"
            }
            pushCallout("STORY", line)
        }
    }

    private fun checkStoryCompletion() {
        val ch = storyChapter ?: return
        if (gameMode != GameMode.SINGLE_PLAYER) return
        val beaconsDone = beaconsCollected >= ch.targetBeacons
        val killsDone = kills >= ch.targetKills
        val bossDone = !ch.hasBoss || bossDefeated
        if (beaconsDone && killsDone && bossDone) {
            isExtracted = true
            pushCallout("OVERWATCH", "${ch.title} COMPLETE! Extraction confirmed.", isAlert = false)
        }
    }

    private fun finishReload() {
        val weapon = currentWeapon()
        val needed = weapon.magazineSize - currentClip()
        val loaded = needed.coerceAtMost(ammoReserve)
        setCurrentClip(currentClip() + loaded)
        ammoReserve -= loaded
        isReloading = false
        reloadTimer = 0f
    }

    private fun pushCallout(sender: String, msg: String, isAlert: Boolean = false) {
        callouts.add(0, RadioCallout(sender, msg, isAlert))
        if (callouts.size > 3) callouts.removeAt(callouts.lastIndex)
    }

    private fun showToast(msg: String) {
        statusToast = msg
        toastTimer = 2.2f
    }

    private fun currentWeapon(): WeaponId = if (usingPrimary) primaryWeapon else secondaryWeapon
    private fun currentClip(): Int = if (usingPrimary) primaryClip else secondaryClip
    private fun setCurrentClip(v: Int) {
        if (usingPrimary) primaryClip = v else secondaryClip = v
    }

    private fun dist(x1: Float, z1: Float, x2: Float, z2: Float): Float {
        val dx = x1 - x2
        val dz = z1 - z2
        return sqrt(dx * dx + dz * dz)
    }

    private fun buildHudState(): CombatHudState {
        val weapon = currentWeapon()
        val reloadTotal = weapon.reloadSec * operator.reloadMultiplier
        val nearCrate = lootNodes.firstOrNull { !it.collected && dist(it.x, it.z, playerX, playerZ) < 2.7f }
        val nearBeacon = props.firstOrNull { it.active && it.kind == PropKind.STORY_BEACON && dist(it.x, it.z, playerX, playerZ) < 2.9f }
        val objectiveStr = if (gameMode == GameMode.SINGLE_PLAYER && storyChapter != null) {
            val bossPart = if (storyChapter.hasBoss) " • BOSS: ${if (bossDefeated) "KILLED" else if (bossSpawned) "ACTIVE" else "PENDING"}" else ""
            "BEACONS $beaconsCollected/${storyChapter.targetBeacons} • HOSTILES $kills/${storyChapter.targetKills}$bossPart"
        } else {
            "WAVE $waveNumber • HOSTILES ${zombies.count { !it.isDying } + wavePendingSpawns} • KILLS $kills"
        }

        return CombatHudState(
            gameMode = gameMode,
            storyChapter = storyChapter,
            storyObjectiveText = objectiveStr,
            beaconsCollected = beaconsCollected,
            beaconsTarget = storyChapter?.targetBeacons ?: 0,
            killsTarget = storyChapter?.targetKills ?: 0,
            bossActive = bossSpawned && !bossDefeated,
            bossDefeated = bossDefeated,
            activeDistrict = CityWorldMap.districtAtWorld(playerX, playerZ),
            waveNumber = waveNumber,
            zombiesRemainingInWave = zombies.count { !it.isDying } + wavePendingSpawns,
            safehouseIntegrity = safehouseIntegrity,
            health = health,
            maxHealth = maxHealth,
            hunger = hunger,
            stamina = stamina,
            stimBuffSec = stimBuffSec,
            isGrounded = isGrounded,
            isSprinting = isSprintInput && stamina > 5f,
            activeWeapon = weapon,
            primaryWeapon = primaryWeapon,
            secondaryWeapon = secondaryWeapon,
            usingPrimary = usingPrimary,
            clipAmmo = currentClip(),
            ammoReserve = ammoReserve,
            isReloading = isReloading,
            reloadProgress = if (isReloading && reloadTotal > 0f) 1f - (reloadTimer / reloadTotal).coerceIn(0f, 1f) else 0f,
            isAds = isAds,
            recoilKickUi = recoilPitch,
            hitMarker = hitMarker,
            damageFlashAlpha = damageFlash,
            kills = kills,
            headshots = headshots,
            nearLootNode = nearCrate,
            nearBeacon = nearBeacon,
            preparedRationsCount = stewCount + stimBrothCount,
            radioCallouts = callouts.toList(),
            squadMates = squadMates.toList(),
            zombiesForRadar = zombies.filter { !it.isDying },
            lootForRadar = lootNodes.toList(),
            propsForRadar = props.filter { it.active && it.kind == PropKind.STORY_BEACON },
            playerX = playerX,
            playerZ = playerZ,
            playerYaw = yaw,
            isGameOver = isGameOver,
            isExtracted = isExtracted,
            statusToast = statusToast
        )
    }
}
