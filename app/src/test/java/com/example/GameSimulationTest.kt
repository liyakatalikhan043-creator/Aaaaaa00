package com.example

import com.example.data.local.SurvivalStashEntity
import com.example.engine.GameSimulation
import com.example.model.CityDistrict
import com.example.model.CityWorldMap
import com.example.model.GameMode
import com.example.model.OperatorId
import com.example.model.StoryChapter
import com.example.model.WeaponId
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class GameSimulationTest {

    @Test
    fun `sprinting drains stamina faster than standing idle`() {
        val simIdle = GameSimulation(
            gameMode = GameMode.COOP_SQUAD,
            storyChapter = null,
            operator = OperatorId.GHOSTLINE,
            primaryWeapon = WeaponId.M4A1_SOPMOD,
            secondaryWeapon = WeaponId.P226_TAC,
            startDistrict = CityDistrict.SAFEHOUSE_CAMP,
            initialStash = SurvivalStashEntity(hunger = 90f)
        )
        val simSprint = GameSimulation(
            gameMode = GameMode.COOP_SQUAD,
            storyChapter = null,
            operator = OperatorId.GHOSTLINE,
            primaryWeapon = WeaponId.M4A1_SOPMOD,
            secondaryWeapon = WeaponId.P226_TAC,
            startDistrict = CityDistrict.SAFEHOUSE_CAMP,
            initialStash = SurvivalStashEntity(hunger = 90f)
        )

        var idleState = simIdle.step(0.05f)
        var sprintState = simSprint.step(0.05f)
        simSprint.setMoveInput(0f, 1f, sprint = true)

        repeat(30) {
            idleState = simIdle.step(0.05f)
            sprintState = simSprint.step(0.05f)
        }

        assertTrue(sprintState.stamina < idleState.stamina)
        assertTrue(sprintState.hunger < idleState.hunger)
    }

    @Test
    fun `jump launches player into air and gravity returns player to ground`() {
        val sim = GameSimulation(
            gameMode = GameMode.SINGLE_PLAYER,
            storyChapter = StoryChapter.CHAPTER_1,
            operator = OperatorId.VIPER,
            primaryWeapon = WeaponId.M4A1_SOPMOD,
            secondaryWeapon = WeaponId.P226_TAC,
            startDistrict = CityDistrict.NEON_MARKET,
            initialStash = SurvivalStashEntity()
        )
        assertTrue(sim.step(0.016f).isGrounded)

        sim.jump()
        val airborneHud = sim.step(0.05f)
        assertFalse(airborneHud.isGrounded)
        assertTrue(sim.renderSnapshot.playerY > 0.1f)

        repeat(30) { sim.step(0.04f) }
        val landedHud = sim.step(0.04f)
        assertTrue(landedHud.isGrounded)
        assertEquals(0f, sim.renderSnapshot.playerY, 0.001f)
    }

    @Test
    fun `consuming prepared ration restores health and energy`() {
        val sim = GameSimulation(
            gameMode = GameMode.SINGLE_PLAYER,
            storyChapter = StoryChapter.CHAPTER_1,
            operator = OperatorId.KESTREL,
            primaryWeapon = WeaponId.M4A1_SOPMOD,
            secondaryWeapon = WeaponId.P226_TAC,
            startDistrict = CityDistrict.SAFEHOUSE_CAMP,
            initialStash = SurvivalStashEntity(hunger = 60f, stewCount = 1, stimBrothCount = 0)
        )
        assertTrue(sim.consumePreparedRation())
        val afterEat = sim.step(0.016f)
        assertTrue(afterEat.hunger > 90f)
        assertEquals(0, afterEat.preparedRationsCount)
    }

    @Test
    fun `firing realistic weapon consumes clip ammo and swapping equips sidearm`() {
        val sim = GameSimulation(
            gameMode = GameMode.SINGLE_PLAYER,
            storyChapter = StoryChapter.CHAPTER_2,
            operator = OperatorId.GHOSTLINE,
            primaryWeapon = WeaponId.M4A1_SOPMOD,
            secondaryWeapon = WeaponId.P226_TAC,
            startDistrict = CityDistrict.BIO_PLAZA,
            initialStash = SurvivalStashEntity()
        )
        sim.setFiring(true)
        repeat(6) { sim.step(0.05f) }
        sim.setFiring(false)

        val stateAfterFire = sim.step(0.016f)
        assertTrue(stateAfterFire.clipAmmo < WeaponId.M4A1_SOPMOD.magazineSize)

        sim.swapWeapon()
        val stateAfterSwap = sim.step(0.016f)
        assertEquals(WeaponId.P226_TAC, stateAfterSwap.activeWeapon)
    }

    @Test
    fun `story chapters and districts are walkable with varied half-destroyed building heights`() {
        for (district in CityDistrict.entries) {
            assertEquals(0, CityWorldMap.cellAt(district.spawnGridX, district.spawnGridZ))
        }
        for (ch in StoryChapter.entries) {
            assertEquals(0, CityWorldMap.cellAt(ch.spawnGridX, ch.spawnGridZ))
        }
        assertTrue(CityWorldMap.wallHeightAt(11, 14) > 5f)
        assertEquals(0f, CityWorldMap.wallHeightAt(16, 24), 0.001f)
    }
}
