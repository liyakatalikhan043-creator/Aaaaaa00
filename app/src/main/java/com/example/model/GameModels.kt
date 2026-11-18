package com.example.model

import androidx.annotation.DrawableRes
import com.example.R
import kotlin.math.abs
import kotlin.math.sqrt

enum class GameMode(val id: String, val title: String) {
    SINGLE_PLAYER("single_player", "SINGLE PLAYER STORY"),
    COOP_SQUAD("coop_squad", "CO-OP SQUAD DEFENSE");

    companion object {
        fun fromId(id: String): GameMode = entries.firstOrNull { it.id == id } ?: SINGLE_PLAYER
    }
}

enum class StoryChapter(
    val number: Int,
    val id: String,
    val title: String,
    val subtitle: String,
    val briefing: String,
    val district: CityDistrict,
    val targetKills: Int,
    val targetBeacons: Int,
    val hasBoss: Boolean,
    val spawnGridX: Int,
    val spawnGridZ: Int
) {
    CHAPTER_1(
        number = 1,
        id = "ch1",
        title = "CH 01: BLACKOUT ON 5TH AVE",
        subtitle = "Locate 3 Blackbox Beacons & Clear Sector",
        briefing = "03:14 HRS — Downtown collapsed six hours after Containment Breach Zero. Recover 3 military blackboxes from the shattered avenue and eliminate infected hostiles blocking Extraction Route Alpha.",
        district = CityDistrict.NEON_MARKET,
        targetKills = 10,
        targetBeacons = 3,
        hasBoss = false,
        spawnGridX = 5,
        spawnGridZ = 22
    ),
    CHAPTER_2(
        number = 2,
        id = "ch2",
        title = "CH 02: ASHES OF MERCY WARD",
        subtitle = "Recover Bio-Research Drives & Defeat Spitters",
        briefing = "04:02 HRS — Bio-Containment Plaza's medical wing fell during evacuation. Fight through mutated Spitters, secure 3 encrypted drives, and open the eastern overpass.",
        district = CityDistrict.BIO_PLAZA,
        targetKills = 14,
        targetBeacons = 3,
        hasBoss = false,
        spawnGridX = 16,
        spawnGridZ = 18
    ),
    CHAPTER_3(
        number = 3,
        id = "ch3",
        title = "CH 03: OVERPASS AMBUSH",
        subtitle = "Detonate Fuel Convoys & Break Armored Siege",
        briefing = "04:45 HRS — A military convoy stalled on the half-collapsed Subway Overpass. Use explosive barrels and armor-piercing fire to shatter the Armored Riot Brute ambush.",
        district = CityDistrict.SUBWAY_OVERPASS,
        targetKills = 16,
        targetBeacons = 2,
        hasBoss = true,
        spawnGridX = 25,
        spawnGridZ = 22
    ),
    CHAPTER_4(
        number = 4,
        id = "ch4",
        title = "CH 04: EVACUATION ZERO",
        subtitle = "Hold Safehouse Helipad & Kill Patient Zero",
        briefing = "05:30 HRS — Dawn approaches. Hold the fortified Safehouse perimeter while Evac-1 descends. Eliminate the Colossus Bio-Titan and escape the deadzone.",
        district = CityDistrict.SAFEHOUSE_CAMP,
        targetKills = 20,
        targetBeacons = 1,
        hasBoss = true,
        spawnGridX = 16,
        spawnGridZ = 24
    );

    companion object {
        fun fromNumber(num: Int): StoryChapter = entries.firstOrNull { it.number == num } ?: CHAPTER_1
    }
}

enum class OperatorId(
    val id: String,
    val callsign: String,
    val roleTitle: String,
    val perkSummary: String,
    val bonusHp: Float,
    val sprintEfficiency: Float,
    val reloadMultiplier: Float,
    val mealPotency: Float,
    val accentHex: Long,
    @DrawableRes val portraitRes: Int?
) {
    GHOSTLINE(
        id = "ghostline",
        callsign = "GHOSTLINE",
        roleTitle = "Task Force Strike Lead",
        perkSummary = "+18% Faster Reloads & Tighter Recoil Control",
        bonusHp = 15f,
        sprintEfficiency = 1.0f,
        reloadMultiplier = 0.82f,
        mealPotency = 1.0f,
        accentHex = 0xFF00E5FF,
        portraitRes = R.drawable.img_operator_ghostline
    ),
    VIPER(
        id = "viper",
        callsign = "VIPER-7",
        roleTitle = "Urban Recon & Mobility",
        perkSummary = "+22% Sprint Stamina Efficiency & Extended Radar",
        bonusHp = 5f,
        sprintEfficiency = 0.78f,
        reloadMultiplier = 0.94f,
        mealPotency = 1.0f,
        accentHex = 0xFFB8FF2E,
        portraitRes = R.drawable.img_operator_viper
    ),
    KESTREL(
        id = "kestrel",
        callsign = "KESTREL",
        roleTitle = "Combat Medic & Field Tech",
        perkSummary = "+30% Ration Healing & Quick-Patch Regen",
        bonusHp = 10f,
        sprintEfficiency = 0.95f,
        reloadMultiplier = 1.0f,
        mealPotency = 1.30f,
        accentHex = 0xFF22E58A,
        portraitRes = null
    ),
    BULWARK(
        id = "bulwark",
        callsign = "BULWARK",
        roleTitle = "Heavy Breacher & Juggernaut",
        perkSummary = "+40 Max Ballistic Armor & Explosion Resistance",
        bonusHp = 40f,
        sprintEfficiency = 1.05f,
        reloadMultiplier = 1.0f,
        mealPotency = 1.0f,
        accentHex = 0xFFFFB020,
        portraitRes = null
    );

    companion object {
        fun fromId(id: String): OperatorId = entries.firstOrNull { it.id == id } ?: GHOSTLINE
    }
}

enum class WeaponId(
    val id: String,
    val displayName: String,
    val caliber: String,
    val category: String,
    val isPrimary: Boolean,
    val isSuppressed: Boolean,
    val damage: Float,
    val headshotMult: Float,
    val knockbackForce: Float,
    val fireIntervalSec: Float,
    val magazineSize: Int,
    val reloadSec: Float,
    val pellets: Int,
    val hipSpreadRad: Float,
    val adsSpreadRad: Float,
    val recoilKick: Float,
    val statDamage: Float,
    val statFireRate: Float,
    val statMobility: Float,
    val statControl: Float
) {
    M4A1_SOPMOD(
        id = "m4a1",
        displayName = "M4A1 SOPMOD",
        caliber = "5.56x45mm NATO",
        category = "Assault Carbine",
        isPrimary = true,
        isSuppressed = false,
        damage = 31f,
        headshotMult = 1.90f,
        knockbackForce = 2.6f,
        fireIntervalSec = 0.105f,
        magazineSize = 30,
        reloadSec = 1.50f,
        pellets = 1,
        hipSpreadRad = 0.028f,
        adsSpreadRad = 0.005f,
        recoilKick = 0.020f,
        statDamage = 0.78f,
        statFireRate = 0.80f,
        statMobility = 0.75f,
        statControl = 0.84f
    ),
    MP5_SD(
        id = "mp5sd",
        displayName = "MP5-SD TACTICAL",
        caliber = "9x19mm Subsonic",
        category = "Suppressed SMG",
        isPrimary = true,
        isSuppressed = true,
        damage = 22f,
        headshotMult = 1.70f,
        knockbackForce = 1.6f,
        fireIntervalSec = 0.072f,
        magazineSize = 40,
        reloadSec = 1.25f,
        pellets = 1,
        hipSpreadRad = 0.032f,
        adsSpreadRad = 0.009f,
        recoilKick = 0.013f,
        statDamage = 0.60f,
        statFireRate = 0.96f,
        statMobility = 0.94f,
        statControl = 0.88f
    ),
    M870_BREACHER(
        id = "m870",
        displayName = "M870 BREACHER",
        caliber = "12-Gauge Buckshot",
        category = "Pump Shotgun",
        isPrimary = true,
        isSuppressed = false,
        damage = 21f,
        headshotMult = 1.45f,
        knockbackForce = 8.5f,
        fireIntervalSec = 0.60f,
        magazineSize = 8,
        reloadSec = 1.75f,
        pellets = 7,
        hipSpreadRad = 0.082f,
        adsSpreadRad = 0.044f,
        recoilKick = 0.056f,
        statDamage = 0.98f,
        statFireRate = 0.36f,
        statMobility = 0.78f,
        statControl = 0.58f
    ),
    MK14_EBR(
        id = "mk14",
        displayName = "MK14 EBR",
        caliber = "7.62x51mm NATO",
        category = "Marksman Rifle",
        isPrimary = true,
        isSuppressed = false,
        damage = 58f,
        headshotMult = 2.40f,
        knockbackForce = 5.2f,
        fireIntervalSec = 0.26f,
        magazineSize = 20,
        reloadSec = 1.80f,
        pellets = 1,
        hipSpreadRad = 0.045f,
        adsSpreadRad = 0.002f,
        recoilKick = 0.040f,
        statDamage = 0.94f,
        statFireRate = 0.48f,
        statMobility = 0.62f,
        statControl = 0.76f
    ),
    P226_TAC(
        id = "p226",
        displayName = "SIG P226 TAC-OPS",
        caliber = "9x19mm Parabellum",
        category = "Tactical Sidearm",
        isPrimary = false,
        isSuppressed = true,
        damage = 38f,
        headshotMult = 2.15f,
        knockbackForce = 2.2f,
        fireIntervalSec = 0.22f,
        magazineSize = 15,
        reloadSec = 1.05f,
        pellets = 1,
        hipSpreadRad = 0.020f,
        adsSpreadRad = 0.004f,
        recoilKick = 0.024f,
        statDamage = 0.72f,
        statFireRate = 0.64f,
        statMobility = 0.98f,
        statControl = 0.90f
    );

    companion object {
        fun fromId(id: String): WeaponId = entries.firstOrNull { it.id == id } ?: M4A1_SOPMOD
    }
}

enum class CityDistrict(
    val id: String,
    val title: String,
    val threatLabel: String,
    val lootFocus: String,
    val spawnGridX: Int,
    val spawnGridZ: Int
) {
    SAFEHOUSE_CAMP(
        id = "safehouse",
        title = "SAFEHOUSE HELIPAD",
        threatLabel = "FORTIFIED ZONE",
        lootFocus = "Field Rations & Ammo Resupply",
        spawnGridX = 16,
        spawnGridZ = 24
    ),
    NEON_MARKET(
        id = "neon_market",
        title = "COLLAPSED 5TH AVE",
        threatLabel = "MODERATE THREAT",
        lootFocus = "MRE Rations & Explosive Barrels",
        spawnGridX = 6,
        spawnGridZ = 16
    ),
    BIO_PLAZA(
        id = "bio_plaza",
        title = "SHATTERED MERCY PLAZA",
        threatLabel = "HIGH BIOHAZARD",
        lootFocus = "Med Crates & Bio-Hazard Zone",
        spawnGridX = 16,
        spawnGridZ = 6
    ),
    SUBWAY_OVERPASS(
        id = "subway",
        title = "RUINED OVERPASS",
        threatLabel = "EXTREME THREAT",
        lootFocus = "Convoy Heavy Ammo & Rations",
        spawnGridX = 25,
        spawnGridZ = 15
    );

    companion object {
        fun fromId(id: String): CityDistrict = entries.firstOrNull { it.id == id } ?: NEON_MARKET
    }
}

enum class CookingRecipe(
    val id: String,
    val title: String,
    val description: String,
    val hungerRestore: Float,
    val healthRestore: Float,
    val buffLabel: String,
    val cannedBeefCost: Int,
    val bioMushroomsCost: Int,
    val purifiedWaterCost: Int,
    val wildHerbsCost: Int,
    val scrapFuelCost: Int,
    val grantsSquadShare: Boolean = false
) {
    MRE_STEW(
        id = "stew",
        title = "Hot MRE Field Stew",
        description = "Quick military ration restoring stamina and patching minor wounds.",
        hungerRestore = 45f,
        healthRestore = 30f,
        buffLabel = "+45 Stamina/Hunger • +30 HP",
        cannedBeefCost = 1,
        bioMushroomsCost = 0,
        purifiedWaterCost = 1,
        wildHerbsCost = 0,
        scrapFuelCost = 1
    ),
    STIM_BROTH(
        id = "stim_broth",
        title = "Adrenaline Stim-Shot",
        description = "Concentrated field tonic boosting sprint speed and reload speed for 45s.",
        hungerRestore = 30f,
        healthRestore = 20f,
        buffLabel = "+45s Reload & Speed Boost",
        cannedBeefCost = 0,
        bioMushroomsCost = 2,
        purifiedWaterCost = 1,
        wildHerbsCost = 0,
        scrapFuelCost = 1
    );

    companion object {
        fun fromId(id: String): CookingRecipe = entries.firstOrNull { it.id == id } ?: MRE_STEW
    }
}

enum class ZombieType(
    val label: String,
    val maxHp: Float,
    val speed: Float,
    val damage: Float,
    val scaleHeight: Float,
    val xpReward: Int
) {
    SHAMBLER("Infected Civilian", 75f, 2.35f, 10f, 1.82f, 25),
    STALKER("Feral Sprinter", 62f, 4.35f, 12f, 1.74f, 35),
    SPITTER("Bio-Spitter", 105f, 2.70f, 15f, 1.92f, 45),
    BRUTE("Armored Riot Brute", 290f, 1.95f, 24f, 2.35f, 120),
    BOSS("Patient Zero Colossus", 920f, 2.25f, 32f, 2.85f, 450)
}

enum class LootKind(val label: String) {
    AMMO_CRATE("5.56/12G Ammo Box"),
    MED_RATION("Combat Medkit & MRE"),
    FUEL_SUPPLY("Tactical Supply Pack")
}

enum class PropKind {
    EXPLOSIVE_BARREL,
    WRECKED_CAR,
    CONCRETE_BARRIER,
    STORY_BEACON
}

data class CityProp(
    val id: String,
    val kind: PropKind,
    val x: Float,
    val z: Float,
    val yawDeg: Float = 0f,
    val hp: Float = 45f,
    val active: Boolean = true
)

data class PhysicsParticle(
    val x: Float,
    val y: Float,
    val z: Float,
    val vx: Float,
    val vy: Float,
    val vz: Float,
    val size: Float,
    val r: Float,
    val g: Float,
    val b: Float,
    val ttl: Float,
    val maxTtl: Float
)

data class ZombieState(
    val uid: Int,
    val type: ZombieType,
    val x: Float,
    val z: Float,
    val vx: Float = 0f,
    val vz: Float = 0f,
    val yaw: Float = 0f,
    val hp: Float,
    val maxHp: Float,
    val attackCooldown: Float = 0f,
    val hitFlashTimer: Float = 0f,
    val animPhase: Float = 0f,
    val isDying: Boolean = false,
    val fallAngleDeg: Float = 0f,
    val deathTimer: Float = 0f
)

data class SquadMateState(
    val id: String,
    val callsign: String,
    val operatorId: OperatorId,
    val x: Float,
    val z: Float,
    val yaw: Float,
    val hp: Float,
    val maxHp: Float,
    val kills: Int,
    val animPhase: Float = 0f,
    val fireFlashTimer: Float = 0f
)

data class WorldLootNode(
    val id: String,
    val kind: LootKind,
    val x: Float,
    val z: Float,
    val district: CityDistrict,
    val collected: Boolean = false,
    val respawnTimer: Float = 0f
)

data class BulletTracer(
    val x1: Float,
    val y1: Float,
    val z1: Float,
    val x2: Float,
    val y2: Float,
    val z2: Float,
    val colorType: Int,
    val ttl: Float
)

data class RadioCallout(
    val sender: String,
    val message: String,
    val isAlert: Boolean = false,
    val timestampMs: Long = System.currentTimeMillis()
)

/**
 * Realistic half-destroyed 32x32 modern city grid:
 * 0 = Walkable asphalt street / sidewalk / open plaza
 * 1 = Ruined High-Rise Tower (tall cracked concrete & broken glass)
 * 2 = Half-Destroyed Brick/Concrete Storefront (medium height)
 * 3 = Shattered Hospital / Bio-Lab Wing (damaged curtain wall)
 * 4 = Collapsed Overpass Pillar / Rubble / Bunker Wall (low-medium cover)
 */
object CityWorldMap {
    const val GRID_SIZE = 32
    const val CELL_SIZE = 3.2f
    const val WORLD_SPAN = GRID_SIZE * CELL_SIZE

    const val CAMP_STOVE_X = 16.5f * CELL_SIZE
    const val CAMP_STOVE_Z = 24.5f * CELL_SIZE

    private val grid: Array<IntArray> = Array(GRID_SIZE) { IntArray(GRID_SIZE) }
    private val heightVariation: Array<FloatArray> = Array(GRID_SIZE) { FloatArray(GRID_SIZE) }

    init {
        buildHalfDestroyedCity()
    }

    private fun buildHalfDestroyedCity() {
        for (i in 0 until GRID_SIZE) {
            grid[0][i] = 1
            grid[GRID_SIZE - 1][i] = 1
            grid[i][0] = 1
            grid[i][GRID_SIZE - 1] = 1
        }

        // West Sector: Half-Destroyed 5th Avenue Shops & Ruined Apartments (stepped collapse heights!)
        val avenueRuins = listOf(
            3 to 4, 4 to 4, 3 to 5,
            3 to 8, 4 to 8, 4 to 9,
            7 to 4, 8 to 4, 8 to 5,
            8 to 9, 9 to 9, 8 to 10,
            3 to 13, 3 to 14, 4 to 14,
            8 to 14, 9 to 14, 9 to 15,
            3 to 19, 4 to 19, 4 to 20,
            8 to 20, 9 to 20
        )
        for ((x, z) in avenueRuins) grid[z][x] = 2

        // North Sector: Shattered Mercy Hospital & Bio-Containment Plaza
        val plazaRuins = listOf(
            13 to 4, 14 to 4, 18 to 4, 19 to 4,
            12 to 7, 12 to 8, 20 to 7, 20 to 8,
            15 to 9, 17 to 9, 13 to 12, 19 to 12
        )
        for ((x, z) in plazaRuins) grid[z][x] = 3

        // Downtown Skyscraper Shells (half-sheared concrete towers)
        val towers = listOf(
            11 to 14, 12 to 14, 11 to 15,
            20 to 14, 21 to 14, 21 to 15,
            11 to 19, 21 to 19,
            6 to 24, 7 to 24, 6 to 25,
            24 to 24, 25 to 24, 25 to 25
        )
        for ((x, z) in towers) grid[z][x] = 1

        // East Sector: Collapsed Overpass Pillars & Highway Rubble
        val overpassRuins = listOf(
            24 to 5, 25 to 5, 27 to 5, 28 to 5,
            24 to 9, 27 to 9, 28 to 9,
            24 to 13, 28 to 13,
            24 to 17, 25 to 17, 28 to 17,
            24 to 21, 27 to 21, 28 to 21
        )
        for ((x, z) in overpassRuins) grid[z][x] = 4

        // South-Center: Fortified Helipad / Safehouse Concrete Perimeter (x=12..20, z=21..28)
        for (x in 12..20) {
            if (x !in 15..17) grid[21][x] = 4
            grid[28][x] = 4
        }
        for (z in 21..28) {
            if (z !in 24..25) {
                grid[z][12] = 4
                grid[z][20] = 4
            }
        }

        // Deterministic uneven broken-building roof heights so skyline looks half-destroyed!
        for (gz in 0 until GRID_SIZE) {
            for (gx in 0 until GRID_SIZE) {
                val base = when (grid[gz][gx]) {
                    1 -> 8.2f
                    2 -> 4.5f
                    3 -> 5.6f
                    4 -> 2.1f
                    else -> 0f
                }
                val rubbleJitter = (((gx * 17 + gz * 31) % 7) - 3) * 0.48f
                heightVariation[gz][gx] = if (base > 0f) (base + rubbleJitter).coerceAtLeast(1.4f) else 0f
            }
        }
    }

    fun cellAt(gx: Int, gz: Int): Int {
        if (gx !in 0 until GRID_SIZE || gz !in 0 until GRID_SIZE) return 1
        return grid[gz][gx]
    }

    fun wallHeightAt(gx: Int, gz: Int): Float {
        if (gx !in 0 until GRID_SIZE || gz !in 0 until GRID_SIZE) return 8.0f
        return heightVariation[gz][gx]
    }

    fun isSolidWorld(wx: Float, wz: Float): Boolean {
        val gx = (wx / CELL_SIZE).toInt()
        val gz = (wz / CELL_SIZE).toInt()
        return cellAt(gx, gz) != 0
    }

    fun districtAtWorld(wx: Float, wz: Float): CityDistrict {
        val gx = (wx / CELL_SIZE).toInt()
        val gz = (wz / CELL_SIZE).toInt()
        return when {
            gx in 12..20 && gz in 21..28 -> CityDistrict.SAFEHOUSE_CAMP
            gx <= 11 -> CityDistrict.NEON_MARKET
            gx >= 22 -> CityDistrict.SUBWAY_OVERPASS
            else -> CityDistrict.BIO_PLAZA
        }
    }

    fun isInsideSafehouse(wx: Float, wz: Float): Boolean {
        val gx = (wx / CELL_SIZE).toInt()
        val gz = (wz / CELL_SIZE).toInt()
        return gx in 13..19 && gz in 22..27
    }

    fun distToCampStove(wx: Float, wz: Float): Float {
        val dx = wx - CAMP_STOVE_X
        val dz = wz - CAMP_STOVE_Z
        return sqrt(dx * dx + dz * dz)
    }

    fun initialProps(chapter: StoryChapter?): List<CityProp> {
        val props = mutableListOf(
            // Wrecked cars & military vehicles along avenues
            CityProp("car_1", PropKind.WRECKED_CAR, 6.2f * CELL_SIZE, 11.5f * CELL_SIZE, 15f),
            CityProp("car_2", PropKind.WRECKED_CAR, 5.4f * CELL_SIZE, 18.2f * CELL_SIZE, -25f),
            CityProp("car_3", PropKind.WRECKED_CAR, 16.5f * CELL_SIZE, 13.0f * CELL_SIZE, 82f),
            CityProp("car_4", PropKind.WRECKED_CAR, 26.2f * CELL_SIZE, 11.2f * CELL_SIZE, 10f),
            CityProp("car_5", PropKind.WRECKED_CAR, 25.8f * CELL_SIZE, 19.2f * CELL_SIZE, -40f),

            // Concrete Street Barriers
            CityProp("bar_1", PropKind.CONCRETE_BARRIER, 15.2f * CELL_SIZE, 20.5f * CELL_SIZE, 0f),
            CityProp("bar_2", PropKind.CONCRETE_BARRIER, 17.8f * CELL_SIZE, 20.5f * CELL_SIZE, 0f),
            CityProp("bar_3", PropKind.CONCRETE_BARRIER, 10.2f * CELL_SIZE, 16.5f * CELL_SIZE, 90f),
            CityProp("bar_4", PropKind.CONCRETE_BARRIER, 22.4f * CELL_SIZE, 15.5f * CELL_SIZE, 90f),

            // Explosive Red Bio-Fuel Barrels (shoot to detonate!)
            CityProp("exp_1", PropKind.EXPLOSIVE_BARREL, 6.8f * CELL_SIZE, 14.5f * CELL_SIZE),
            CityProp("exp_2", PropKind.EXPLOSIVE_BARREL, 5.8f * CELL_SIZE, 7.2f * CELL_SIZE),
            CityProp("exp_3", PropKind.EXPLOSIVE_BARREL, 15.8f * CELL_SIZE, 10.5f * CELL_SIZE),
            CityProp("exp_4", PropKind.EXPLOSIVE_BARREL, 17.6f * CELL_SIZE, 6.4f * CELL_SIZE),
            CityProp("exp_5", PropKind.EXPLOSIVE_BARREL, 26.0f * CELL_SIZE, 14.8f * CELL_SIZE),
            CityProp("exp_6", PropKind.EXPLOSIVE_BARREL, 25.6f * CELL_SIZE, 8.0f * CELL_SIZE),
            CityProp("exp_7", PropKind.EXPLOSIVE_BARREL, 14.2f * CELL_SIZE, 22.8f * CELL_SIZE),
            CityProp("exp_8", PropKind.EXPLOSIVE_BARREL, 18.8f * CELL_SIZE, 22.8f * CELL_SIZE)
        )

        // Story Blackbox Beacons when in Single Player Campaign
        when (chapter) {
            StoryChapter.CHAPTER_1 -> {
                props.add(CityProp("bcn_1", PropKind.STORY_BEACON, 5.5f * CELL_SIZE, 17.5f * CELL_SIZE))
                props.add(CityProp("bcn_2", PropKind.STORY_BEACON, 6.5f * CELL_SIZE, 11.0f * CELL_SIZE))
                props.add(CityProp("bcn_3", PropKind.STORY_BEACON, 5.5f * CELL_SIZE, 5.8f * CELL_SIZE))
            }
            StoryChapter.CHAPTER_2 -> {
                props.add(CityProp("bcn_1", PropKind.STORY_BEACON, 16.5f * CELL_SIZE, 14.0f * CELL_SIZE))
                props.add(CityProp("bcn_2", PropKind.STORY_BEACON, 14.5f * CELL_SIZE, 8.5f * CELL_SIZE))
                props.add(CityProp("bcn_3", PropKind.STORY_BEACON, 16.5f * CELL_SIZE, 5.2f * CELL_SIZE))
            }
            StoryChapter.CHAPTER_3 -> {
                props.add(CityProp("bcn_1", PropKind.STORY_BEACON, 26.2f * CELL_SIZE, 16.0f * CELL_SIZE))
                props.add(CityProp("bcn_2", PropKind.STORY_BEACON, 26.2f * CELL_SIZE, 7.2f * CELL_SIZE))
            }
            StoryChapter.CHAPTER_4 -> {
                props.add(CityProp("bcn_1", PropKind.STORY_BEACON, 16.5f * CELL_SIZE, 22.5f * CELL_SIZE))
            }
            null -> Unit
        }
        return props
    }

    fun initialLootNodes(): List<WorldLootNode> = listOf(
        WorldLootNode("loot_mkt_1", LootKind.MED_RATION, 5.5f * CELL_SIZE, 6.5f * CELL_SIZE, CityDistrict.NEON_MARKET),
        WorldLootNode("loot_mkt_2", LootKind.AMMO_CRATE, 6.5f * CELL_SIZE, 12.5f * CELL_SIZE, CityDistrict.NEON_MARKET),
        WorldLootNode("loot_mkt_3", LootKind.MED_RATION, 5.5f * CELL_SIZE, 16.5f * CELL_SIZE, CityDistrict.NEON_MARKET),
        WorldLootNode("loot_bio_1", LootKind.FUEL_SUPPLY, 16.5f * CELL_SIZE, 5.5f * CELL_SIZE, CityDistrict.BIO_PLAZA),
        WorldLootNode("loot_bio_2", LootKind.MED_RATION, 14.5f * CELL_SIZE, 8.5f * CELL_SIZE, CityDistrict.BIO_PLAZA),
        WorldLootNode("loot_bio_3", LootKind.AMMO_CRATE, 16.5f * CELL_SIZE, 14.5f * CELL_SIZE, CityDistrict.BIO_PLAZA),
        WorldLootNode("loot_sub_1", LootKind.AMMO_CRATE, 26.5f * CELL_SIZE, 7.5f * CELL_SIZE, CityDistrict.SUBWAY_OVERPASS),
        WorldLootNode("loot_sub_2", LootKind.FUEL_SUPPLY, 25.5f * CELL_SIZE, 12.5f * CELL_SIZE, CityDistrict.SUBWAY_OVERPASS),
        WorldLootNode("loot_sub_3", LootKind.MED_RATION, 26.5f * CELL_SIZE, 17.5f * CELL_SIZE, CityDistrict.SUBWAY_OVERPASS)
    )

    fun zombieSpawnPoints(): List<Pair<Float, Float>> = listOf(
        5.5f * CELL_SIZE to 5.5f * CELL_SIZE,
        16.5f * CELL_SIZE to 4.5f * CELL_SIZE,
        26.5f * CELL_SIZE to 6.5f * CELL_SIZE,
        7.5f * CELL_SIZE to 15.5f * CELL_SIZE,
        16.5f * CELL_SIZE to 13.5f * CELL_SIZE,
        26.5f * CELL_SIZE to 14.5f * CELL_SIZE,
        9.5f * CELL_SIZE to 22.5f * CELL_SIZE,
        23.5f * CELL_SIZE to 22.5f * CELL_SIZE
    )

    fun hasLineOfSight(x1: Float, z1: Float, x2: Float, z2: Float): Boolean {
        val dx = x2 - x1
        val dz = z2 - z1
        val dist = sqrt(dx * dx + dz * dz)
        val steps = (dist / 0.75f).toInt().coerceAtLeast(1)
        for (i in 1 until steps) {
            val t = i.toFloat() / steps
            if (isSolidWorld(x1 + dx * t, z1 + dz * t)) return false
        }
        return true
    }

    fun clampOutsideWall(nx: Float, nz: Float, radius: Float = 0.52f): Pair<Float, Float> {
        val blocked = isSolidWorld(nx - radius, nz - radius) ||
            isSolidWorld(nx + radius, nz - radius) ||
            isSolidWorld(nx - radius, nz + radius) ||
            isSolidWorld(nx + radius, nz + radius)
        return if (!blocked) nx to nz else Float.NaN to Float.NaN
    }

    fun distManhattan(x1: Float, z1: Float, x2: Float, z2: Float): Float =
        abs(x1 - x2) + abs(z1 - z2)
}
