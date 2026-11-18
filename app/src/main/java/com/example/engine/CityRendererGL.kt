package com.example.engine

import android.opengl.GLES20
import android.opengl.GLSurfaceView
import android.opengl.Matrix
import com.example.model.CityProp
import com.example.model.CityWorldMap
import com.example.model.PropKind
import com.example.model.SquadMateState
import com.example.model.ZombieState
import com.example.model.ZombieType
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10
import kotlin.math.cos
import kotlin.math.sin

class CityRendererGL(
    private val snapshotProvider: () -> CombatRenderSnapshot
) : GLSurfaceView.Renderer {

    private var program = 0
    private var aPosition = 0
    private var aTexCoord = 0
    private var uMvpMatrix = 0
    private var uTexture = 0
    private var uMuzzleFlash = 0
    private var uTintColor = 0
    private var uFogColor = 0

    private lateinit var textures: CityTextureSet

    private var floorBuffer: FloatBuffer? = null
    private var floorVertexCount = 0
    private var safehouseFloorBuffer: FloatBuffer? = null
    private var safehouseVertexCount = 0

    private val wallBuffers = mutableMapOf<Int, Pair<FloatBuffer, Int>>()
    private lateinit var unitBoxBuffer: FloatBuffer
    private lateinit var tracerBuffer: FloatBuffer

    private val projMatrix = FloatArray(16)
    private val viewMatrix = FloatArray(16)
    private val modelMatrix = FloatArray(16)
    private val vpMatrix = FloatArray(16)
    private val mvpMatrix = FloatArray(16)
    private val partMatrix = FloatArray(16)

    private var aspect = 1.77f

    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        GLES20.glClearColor(0.055f, 0.068f, 0.082f, 1.0f)
        GLES20.glEnable(GLES20.GL_DEPTH_TEST)
        GLES20.glEnable(GLES20.GL_BLEND)
        GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA)

        program = buildProgram(VERTEX_SHADER, FRAGMENT_SHADER)
        aPosition = GLES20.glGetAttribLocation(program, "aPosition")
        aTexCoord = GLES20.glGetAttribLocation(program, "aTexCoord")
        uMvpMatrix = GLES20.glGetUniformLocation(program, "uMvpMatrix")
        uTexture = GLES20.glGetUniformLocation(program, "uTexture")
        uMuzzleFlash = GLES20.glGetUniformLocation(program, "uMuzzleFlash")
        uTintColor = GLES20.glGetUniformLocation(program, "uTintColor")
        uFogColor = GLES20.glGetUniformLocation(program, "uFogColor")

        textures = ProceduralTextures.createAll()
        buildStaticWorldGeometry()
        unitBoxBuffer = createFloatBuffer(buildUnitBoxVertices())
        tracerBuffer = createFloatBuffer(FloatArray(10))
    }

    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        GLES20.glViewport(0, 0, width, height)
        aspect = if (height > 0) width.toFloat() / height.toFloat() else 1.77f
    }

    override fun onDrawFrame(gl: GL10?) {
        val snap = snapshotProvider()
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT or GLES20.GL_DEPTH_BUFFER_BIT)
        GLES20.glUseProgram(program)

        val fov = snap.fovDegrees.coerceIn(38f, 76f)
        Matrix.perspectiveM(projMatrix, 0, fov, aspect, 0.14f, 125f)

        val bobOffset = sin(snap.walkCycle) * 0.042f * snap.moveIntensity
        val shakeX = snap.screenShake * sin(snap.walkCycle * 11f + 1.3f) * 0.14f
        val shakeY = snap.screenShake * cos(snap.walkCycle * 13f) * 0.14f
        val eyeY = snap.playerY + (if (snap.isSliding) 1.02f else 1.66f) + bobOffset + shakeY
        val eyeX = snap.playerX + shakeX

        val lookDist = 12f
        val cosP = cos(snap.pitch)
        val lookX = eyeX + sin(snap.yaw) * cosP * lookDist
        val lookY = eyeY + sin(snap.pitch) * lookDist
        val lookZ = snap.playerZ - cos(snap.yaw) * cosP * lookDist

        Matrix.setLookAtM(
            viewMatrix, 0,
            eyeX, eyeY, snap.playerZ,
            lookX, lookY, lookZ,
            0f, 1f, 0f
        )
        Matrix.multiplyMM(vpMatrix, 0, projMatrix, 0, viewMatrix, 0)

        GLES20.glUniform1f(uMuzzleFlash, snap.muzzleFlash)
        GLES20.glUniform3f(uFogColor, 0.06f, 0.075f, 0.09f)
        GLES20.glUniform4f(uTintColor, 1f, 1f, 1f, 1f)

        // 1. Streets & Safehouse Helipad
        Matrix.setIdentityM(modelMatrix, 0)
        Matrix.multiplyMM(mvpMatrix, 0, vpMatrix, 0, modelMatrix, 0)
        floorBuffer?.let { drawTexturedMesh(it, floorVertexCount, textures.floorAsphalt) }
        safehouseFloorBuffer?.let { drawTexturedMesh(it, safehouseVertexCount, textures.floorSafehouse) }

        // 2. Half-Destroyed City Buildings & Ruins
        wallBuffers[1]?.let { drawTexturedMesh(it.first, it.second, textures.wallTower) }
        wallBuffers[2]?.let { drawTexturedMesh(it.first, it.second, textures.wallStorefront) }
        wallBuffers[3]?.let { drawTexturedMesh(it.first, it.second, textures.wallBioLab) }
        wallBuffers[4]?.let { drawTexturedMesh(it.first, it.second, textures.wallConcrete) }

        // 3. 3D Interactive Physics Props (Wrecked Cars, Explosive Barrels, Barriers, Beacons)
        for (prop in snap.props) {
            if (!prop.active) continue
            drawCityProp(prop, snap.walkCycle)
        }

        // 4. 3D Loot Crates & Camp Stove
        drawBox(CityWorldMap.CAMP_STOVE_X, 0.35f, CityWorldMap.CAMP_STOVE_Z, 0f, 0.85f, 0.7f, 0.85f, textures.propBarrel, 1f, 0.55f, 0.15f, 1f)
        for (crate in snap.lootNodes) {
            if (crate.collected) continue
            drawBox(crate.x, 0.36f, crate.z, snap.walkCycle * 8f, 0.72f, 0.72f, 0.72f, textures.lootCrate, 1f, 1f, 1f, 1f)
        }

        // 5. 3D Articulated Co-Op Squadmates
        for (mate in snap.squadMates) {
            drawArticulatedSquadMate(mate)
        }

        // 6. 3D Articulated Zombies (with walking joints + ragdoll collapse!)
        for (zmb in snap.zombies) {
            drawArticulatedZombie(zmb)
        }

        // 7. 3D Physics Particles (Debris, Fireballs, Sparks)
        for (p in snap.particles) {
            val alpha = (p.ttl / p.maxTtl).coerceIn(0f, 1f)
            drawBox(p.x, p.y, p.z, p.ttl * 240f, p.size, p.size, p.size, textures.solidWhite, p.r, p.g, p.b, alpha)
        }

        // 8. 3D Bullet Tracers
        Matrix.setIdentityM(modelMatrix, 0)
        Matrix.multiplyMM(mvpMatrix, 0, vpMatrix, 0, modelMatrix, 0)
        GLES20.glUniformMatrix4fv(uMvpMatrix, 1, false, mvpMatrix, 0)
        GLES20.glLineWidth(4.5f)
        for (tr in snap.tracers) {
            val data = floatArrayOf(
                tr.x1, tr.y1, tr.z1, 0.5f, 0.5f,
                tr.x2, tr.y2, tr.z2, 0.5f, 0.5f
            )
            tracerBuffer.clear()
            tracerBuffer.put(data)
            tracerBuffer.position(0)
            val alpha = (tr.ttl / 0.12f).coerceIn(0f, 1f)
            when (tr.colorType) {
                0 -> GLES20.glUniform4f(uTintColor, 1.0f, 0.85f, 0.35f, alpha)
                1 -> GLES20.glUniform4f(uTintColor, 0.15f, 0.92f, 1.0f, alpha)
                else -> GLES20.glUniform4f(uTintColor, 0.55f, 1.0f, 0.15f, alpha)
            }
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textures.solidWhite)
            tracerBuffer.position(0)
            GLES20.glEnableVertexAttribArray(aPosition)
            GLES20.glVertexAttribPointer(aPosition, 3, GLES20.GL_FLOAT, false, 20, tracerBuffer)
            tracerBuffer.position(3)
            GLES20.glEnableVertexAttribArray(aTexCoord)
            GLES20.glVertexAttribPointer(aTexCoord, 2, GLES20.GL_FLOAT, false, 20, tracerBuffer)
            GLES20.glDrawArrays(GLES20.GL_LINES, 0, 2)
        }
    }

    private fun drawCityProp(prop: CityProp, pulsePhase: Float) {
        when (prop.kind) {
            PropKind.EXPLOSIVE_BARREL -> {
                drawBox(prop.x, 0.52f, prop.z, 0f, 0.62f, 1.04f, 0.62f, textures.propBarrel, 1f, 1f, 1f, 1f)
            }
            PropKind.CONCRETE_BARRIER -> {
                drawBox(prop.x, 0.45f, prop.z, prop.yawDeg, 2.2f, 0.90f, 0.55f, textures.wallConcrete, 0.9f, 0.9f, 0.9f, 1f)
            }
            PropKind.WRECKED_CAR -> {
                // Multi-part 3D Vehicle: lower chassis + upper cab + 4 wheels
                drawBox(prop.x, 0.52f, prop.z, prop.yawDeg, 1.9f, 0.65f, 3.8f, textures.propCar, 0.95f, 0.95f, 0.95f, 1f)
                drawBox(prop.x, 1.08f, prop.z, prop.yawDeg, 1.6f, 0.55f, 1.9f, textures.propCar, 0.65f, 0.72f, 0.78f, 1f)
                // Dark rubber wheels
                val rad = Math.toRadians(prop.yawDeg.toDouble()).toFloat()
                val cosA = cos(rad)
                val sinA = sin(rad)
                val wheelOffsets = listOf(-0.88f to 1.15f, 0.88f to 1.15f, -0.88f to -1.15f, 0.88f to -1.15f)
                for ((ox, oz) in wheelOffsets) {
                    val wx = prop.x + ox * cosA - oz * sinA
                    val wz = prop.z + ox * sinA + oz * cosA
                    drawBox(wx, 0.28f, wz, prop.yawDeg, 0.28f, 0.56f, 0.56f, textures.solidWhite, 0.08f, 0.09f, 0.11f, 1f)
                }
            }
            PropKind.STORY_BEACON -> {
                val hover = 0.75f + sin(pulsePhase * 0.8f) * 0.12f
                drawBox(prop.x, 0.2f, prop.z, 0f, 0.7f, 0.4f, 0.7f, textures.wallConcrete, 1f, 1f, 1f, 1f)
                drawBox(prop.x, hover, prop.z, pulsePhase * 40f, 0.42f, 0.65f, 0.42f, textures.propBeacon, 1f, 1f, 1f, 0.95f)
            }
        }
    }

    private fun drawArticulatedZombie(z: ZombieState) {
        val scale = z.type.scaleHeight / 1.85f
        val swing = if (z.isDying) 0f else sin(z.animPhase) * 28f
        val hitR = if (z.hitFlashTimer > 0f) 1.7f else 1f
        val hitG = if (z.hitFlashTimer > 0f) 0.45f else 1f
        val hitB = if (z.hitFlashTimer > 0f) 0.45f else 1f
        val alpha = if (z.isDying) (1f - z.deathTimer / 1.4f).coerceIn(0.15f, 1f) else 1f

        val (skinR, skinG, skinB) = when (z.type) {
            ZombieType.SHAMBLER -> Triple(0.32f, 0.44f, 0.34f)
            ZombieType.STALKER -> Triple(0.18f, 0.42f, 0.50f)
            ZombieType.SPITTER -> Triple(0.38f, 0.62f, 0.18f)
            ZombieType.BRUTE -> Triple(0.38f, 0.24f, 0.26f)
            ZombieType.BOSS -> Triple(0.45f, 0.12f, 0.22f)
        }
        val (eyeR, eyeG, eyeB) = when (z.type) {
            ZombieType.SHAMBLER -> Triple(0.75f, 1.0f, 0.2f)
            ZombieType.STALKER -> Triple(0.0f, 0.92f, 1.0f)
            ZombieType.SPITTER -> Triple(0.65f, 1.0f, 0.0f)
            ZombieType.BRUTE, ZombieType.BOSS -> Triple(1.0f, 0.16f, 0.32f)
        }

        // Character root transform (includes ragdoll collapse tip-over!)
        Matrix.setIdentityM(modelMatrix, 0)
        Matrix.translateM(modelMatrix, 0, z.x, 0f, z.z)
        Matrix.rotateM(modelMatrix, 0, Math.toDegrees((-z.yaw).toDouble()).toFloat(), 0f, 1f, 0f)
        if (z.fallAngleDeg > 0f) {
            Matrix.rotateM(modelMatrix, 0, z.fallAngleDeg, 1f, 0f, 0f)
        }
        Matrix.scaleM(modelMatrix, 0, scale, scale, scale)

        // Legs (pivoting opposite stride)
        drawCharacterPart(modelMatrix, -0.16f, 0.42f, 0f, swing, 0f, 0.22f, 0.78f, 0.24f, 0.15f * hitR, 0.18f * hitG, 0.20f * hitB, alpha)
        drawCharacterPart(modelMatrix, 0.16f, 0.42f, 0f, -swing, 0f, 0.22f, 0.78f, 0.24f, 0.15f * hitR, 0.18f * hitG, 0.20f * hitB, alpha)

        // Torso
        val torsoW = if (z.type == ZombieType.BRUTE || z.type == ZombieType.BOSS) 0.72f else 0.52f
        drawCharacterPart(modelMatrix, 0f, 1.12f, 0f, 0f, 0f, torsoW, 0.68f, 0.32f, skinR * hitR, skinG * hitG, skinB * hitB, alpha)

        // Outstretched Zombie Arms (lunging forward)
        drawCharacterPart(modelMatrix, -(torsoW * 0.55f + 0.08f), 1.24f, -0.22f, -72f + swing * 0.4f, 0f, 0.18f, 0.62f, 0.18f, skinR * hitR, skinG * hitG, skinB * hitB, alpha)
        drawCharacterPart(modelMatrix, (torsoW * 0.55f + 0.08f), 1.24f, -0.22f, -72f - swing * 0.4f, 0f, 0.18f, 0.62f, 0.18f, skinR * hitR, skinG * hitG, skinB * hitB, alpha)

        // Head
        drawCharacterPart(modelMatrix, 0f, 1.66f, 0f, 0f, 0f, 0.36f, 0.36f, 0.36f, skinR * 1.1f * hitR, skinG * 1.1f * hitG, skinB * 1.1f * hitB, alpha)

        // Glowing Visor / Eyes strip on front face (-Z is forward!)
        drawCharacterPart(modelMatrix, 0f, 1.68f, -0.185f, 0f, 0f, 0.24f, 0.08f, 0.04f, eyeR, eyeG, eyeB, alpha)
    }

    private fun drawArticulatedSquadMate(m: SquadMateState) {
        val swing = sin(m.animPhase) * 24f
        Matrix.setIdentityM(modelMatrix, 0)
        Matrix.translateM(modelMatrix, 0, m.x, 0f, m.z)
        Matrix.rotateM(modelMatrix, 0, Math.toDegrees((-m.yaw).toDouble()).toFloat(), 0f, 1f, 0f)

        // Legs
        drawCharacterPart(modelMatrix, -0.15f, 0.42f, 0f, swing, 0f, 0.21f, 0.78f, 0.22f, 0.14f, 0.18f, 0.22f, 1f)
        drawCharacterPart(modelMatrix, 0.15f, 0.42f, 0f, -swing, 0f, 0.21f, 0.78f, 0.22f, 0.14f, 0.18f, 0.22f, 1f)

        // Ballistic Plate Carrier Torso
        drawCharacterPart(modelMatrix, 0f, 1.12f, 0f, 0f, 0f, 0.52f, 0.68f, 0.32f, 0.20f, 0.26f, 0.32f, 1f)

        // Arms holding rifle
        drawCharacterPart(modelMatrix, -0.34f, 1.18f, -0.16f, -55f, 0f, 0.17f, 0.55f, 0.17f, 0.22f, 0.28f, 0.34f, 1f)
        drawCharacterPart(modelMatrix, 0.34f, 1.18f, -0.16f, -55f, 0f, 0.17f, 0.55f, 0.17f, 0.22f, 0.28f, 0.34f, 1f)

        // Realistic Rifle Mesh
        drawCharacterPart(modelMatrix, 0.12f, 1.18f, -0.45f, 0f, 0f, 0.08f, 0.12f, 0.72f, 0.08f, 0.09f, 0.11f, 1f)

        // Helmet & Cyan Tactical Visor
        drawCharacterPart(modelMatrix, 0f, 1.66f, 0f, 0f, 0f, 0.36f, 0.36f, 0.36f, 0.16f, 0.20f, 0.24f, 1f)
        drawCharacterPart(modelMatrix, 0f, 1.66f, -0.19f, 0f, 0f, 0.26f, 0.09f, 0.04f, 0.0f, 0.90f, 1.0f, 1f)
    }

    private fun drawCharacterPart(
        parentMat: FloatArray,
        ox: Float, oy: Float, oz: Float,
        rotXDeg: Float, rotYDeg: Float,
        sx: Float, sy: Float, sz: Float,
        r: Float, g: Float, b: Float, a: Float
    ) {
        System.arraycopy(parentMat, 0, partMatrix, 0, 16)
        Matrix.translateM(partMatrix, 0, ox, oy, oz)
        if (rotYDeg != 0f) Matrix.rotateM(partMatrix, 0, rotYDeg, 0f, 1f, 0f)
        if (rotXDeg != 0f) Matrix.rotateM(partMatrix, 0, rotXDeg, 1f, 0f, 0f)
        Matrix.scaleM(partMatrix, 0, sx, sy, sz)
        Matrix.multiplyMM(mvpMatrix, 0, vpMatrix, 0, partMatrix, 0)
        GLES20.glUniform4f(uTintColor, r, g, b, a)
        drawTexturedMesh(unitBoxBuffer, 36, textures.solidWhite)
    }

    private fun drawBox(
        x: Float, y: Float, z: Float,
        yawDeg: Float,
        sx: Float, sy: Float, sz: Float,
        texId: Int,
        r: Float, g: Float, b: Float, a: Float
    ) {
        Matrix.setIdentityM(modelMatrix, 0)
        Matrix.translateM(modelMatrix, 0, x, y, z)
        if (yawDeg != 0f) Matrix.rotateM(modelMatrix, 0, yawDeg, 0f, 1f, 0f)
        Matrix.scaleM(modelMatrix, 0, sx, sy, sz)
        Matrix.multiplyMM(mvpMatrix, 0, vpMatrix, 0, modelMatrix, 0)
        GLES20.glUniform4f(uTintColor, r, g, b, a)
        drawTexturedMesh(unitBoxBuffer, 36, texId)
    }

    private fun drawTexturedMesh(buffer: FloatBuffer, vertexCount: Int, texId: Int) {
        GLES20.glUniformMatrix4fv(uMvpMatrix, 1, false, mvpMatrix, 0)
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0)
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, texId)
        GLES20.glUniform1i(uTexture, 0)

        buffer.position(0)
        GLES20.glEnableVertexAttribArray(aPosition)
        GLES20.glVertexAttribPointer(aPosition, 3, GLES20.GL_FLOAT, false, 20, buffer)

        buffer.position(3)
        GLES20.glEnableVertexAttribArray(aTexCoord)
        GLES20.glVertexAttribPointer(aTexCoord, 2, GLES20.GL_FLOAT, false, 20, buffer)

        GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, vertexCount)
    }

    private fun buildStaticWorldGeometry() {
        val c = CityWorldMap.CELL_SIZE
        val span = CityWorldMap.WORLD_SPAN
        val tileRepeats = CityWorldMap.GRID_SIZE.toFloat()

        floorBuffer = createFloatBuffer(
            floatArrayOf(
                0f, 0f, 0f, 0f, 0f,
                span, 0f, 0f, tileRepeats, 0f,
                span, 0f, span, tileRepeats, tileRepeats,
                0f, 0f, 0f, 0f, 0f,
                span, 0f, span, tileRepeats, tileRepeats,
                0f, 0f, span, 0f, tileRepeats
            )
        )
        floorVertexCount = 6

        val sx1 = 13f * c
        val sx2 = 20f * c
        val sz1 = 22f * c
        val sz2 = 28f * c
        safehouseFloorBuffer = createFloatBuffer(
            floatArrayOf(
                sx1, 0.015f, sz1, 0f, 0f,
                sx2, 0.015f, sz1, 1f, 0f,
                sx2, 0.015f, sz2, 1f, 1f,
                sx1, 0.015f, sz1, 0f, 0f,
                sx2, 0.015f, sz2, 1f, 1f,
                sx1, 0.015f, sz2, 0f, 1f
            )
        )
        safehouseVertexCount = 6

        val lists = mapOf(
            1 to ArrayList<Float>(),
            2 to ArrayList<Float>(),
            3 to ArrayList<Float>(),
            4 to ArrayList<Float>()
        )

        for (gz in 0 until CityWorldMap.GRID_SIZE) {
            for (gx in 0 until CityWorldMap.GRID_SIZE) {
                val cell = CityWorldMap.cellAt(gx, gz)
                if (cell == 0) continue
                val target = lists[cell] ?: continue
                val h = CityWorldMap.wallHeightAt(gx, gz)
                val vRep = (h / 3.2f).coerceAtLeast(1f)
                val x0 = gx * c
                val x1 = x0 + c
                val z0 = gz * c
                val z1 = z0 + c

                if (CityWorldMap.cellAt(gx, gz - 1) == 0) addQuad(target, x1, 0f, z0, x0, 0f, z0, x0, h, z0, x1, h, z0, vRep)
                if (CityWorldMap.cellAt(gx, gz + 1) == 0) addQuad(target, x0, 0f, z1, x1, 0f, z1, x1, h, z1, x0, h, z1, vRep)
                if (CityWorldMap.cellAt(gx - 1, gz) == 0) addQuad(target, x0, 0f, z0, x0, 0f, z1, x0, h, z1, x0, h, z0, vRep)
                if (CityWorldMap.cellAt(gx + 1, gz) == 0) addQuad(target, x1, 0f, z1, x1, 0f, z0, x1, h, z0, x1, h, z1, vRep)
                // Broken jagged roof cap so half-destroyed buildings look complete from elevated jump angles
                addQuad(target, x0, h, z1, x1, h, z1, x1, h, z0, x0, h, z0, 1f)
            }
        }

        wallBuffers.clear()
        for ((type, floats) in lists) {
            if (floats.isNotEmpty()) {
                wallBuffers[type] = createFloatBuffer(floats.toFloatArray()) to (floats.size / 5)
            }
        }
    }

    private fun buildUnitBoxVertices(): FloatArray {
        val out = ArrayList<Float>(36 * 5)
        val n = -0.5f
        val p = 0.5f
        addQuad(out, n, n, p, p, n, p, p, p, p, n, p, p, 1f) // Front
        addQuad(out, p, n, n, n, n, n, n, p, n, p, p, n, 1f) // Back
        addQuad(out, n, n, n, n, n, p, n, p, p, n, p, n, 1f) // Left
        addQuad(out, p, n, p, p, n, n, p, p, n, p, p, p, 1f) // Right
        addQuad(out, n, p, p, p, p, p, p, p, n, n, p, n, 1f) // Top
        addQuad(out, n, n, n, p, n, n, p, n, p, n, n, p, 1f) // Bottom
        return out.toFloatArray()
    }

    private fun addQuad(
        out: ArrayList<Float>,
        x0: Float, y0: Float, z0: Float,
        x1: Float, y1: Float, z1: Float,
        x2: Float, y2: Float, z2: Float,
        x3: Float, y3: Float, z3: Float,
        vRep: Float
    ) {
        out.addAll(
            listOf(
                x0, y0, z0, 0f, vRep,
                x1, y1, z1, 1f, vRep,
                x2, y2, z2, 1f, 0f,
                x0, y0, z0, 0f, vRep,
                x2, y2, z2, 1f, 0f,
                x3, y3, z3, 0f, 0f
            )
        )
    }

    private fun createFloatBuffer(data: FloatArray): FloatBuffer =
        ByteBuffer.allocateDirect(data.size * 4)
            .order(ByteOrder.nativeOrder())
            .asFloatBuffer()
            .apply {
                put(data)
                position(0)
            }

    private fun buildProgram(vertSrc: String, fragSrc: String): Int {
        val vShader = compileShader(GLES20.GL_VERTEX_SHADER, vertSrc)
        val fShader = compileShader(GLES20.GL_FRAGMENT_SHADER, fragSrc)
        return GLES20.glCreateProgram().also { prog ->
            GLES20.glAttachShader(prog, vShader)
            GLES20.glAttachShader(prog, fShader)
            GLES20.glLinkProgram(prog)
        }
    }

    private fun compileShader(type: Int, src: String): Int =
        GLES20.glCreateShader(type).also { shader ->
            GLES20.glShaderSource(shader, src)
            GLES20.glCompileShader(shader)
        }

    companion object {
        private const val VERTEX_SHADER = """
            uniform mat4 uMvpMatrix;
            attribute vec4 aPosition;
            attribute vec2 aTexCoord;
            varying vec2 vTexCoord;
            varying float vDepth;
            void main() {
                gl_Position = uMvpMatrix * aPosition;
                vTexCoord = aTexCoord;
                vDepth = gl_Position.w;
            }
        """

        private const val FRAGMENT_SHADER = """
            precision mediump float;
            uniform sampler2D uTexture;
            uniform float uMuzzleFlash;
            uniform vec4 uTintColor;
            uniform vec3 uFogColor;
            varying vec2 vTexCoord;
            varying float vDepth;
            void main() {
                vec4 tex = texture2D(uTexture, vTexCoord);
                if (tex.a < 0.12) discard;
                float flashBoost = uMuzzleFlash * max(0.0, 1.0 - vDepth / 18.0) * 0.55;
                vec3 lit = tex.rgb * uTintColor.rgb + vec3(0.95 * flashBoost, 0.72 * flashBoost, 0.35 * flashBoost);
                float fogFactor = clamp((vDepth - 14.0) / 62.0, 0.0, 0.86);
                vec3 finalRgb = mix(lit, uFogColor, fogFactor);
                gl_FragColor = vec4(finalRgb, tex.a * uTintColor.a);
            }
        """
    }
}
