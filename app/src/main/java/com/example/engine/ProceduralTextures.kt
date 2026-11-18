package com.example.engine

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RadialGradient
import android.graphics.Shader
import android.opengl.GLES20
import android.opengl.GLUtils

data class CityTextureSet(
    val wallTower: Int,
    val wallStorefront: Int,
    val wallBioLab: Int,
    val wallConcrete: Int,
    val floorAsphalt: Int,
    val floorSafehouse: Int,
    val propCar: Int,
    val propBarrel: Int,
    val propBeacon: Int,
    val lootCrate: Int,
    val solidWhite: Int
)

object ProceduralTextures {

    fun createAll(): CityTextureSet = CityTextureSet(
        wallTower = uploadBitmap(drawRuinedSkyscraperWall()),
        wallStorefront = uploadBitmap(drawRuinedStorefrontWall()),
        wallBioLab = uploadBitmap(drawDamagedHospitalWall()),
        wallConcrete = uploadBitmap(drawConcreteRubbleWall()),
        floorAsphalt = uploadBitmap(drawCityAsphaltStreet()),
        floorSafehouse = uploadBitmap(drawHelipadDeck()),
        propCar = uploadBitmap(drawWreckedCarTexture()),
        propBarrel = uploadBitmap(drawExplosiveBarrelTexture()),
        propBeacon = uploadBitmap(drawBeaconTexture()),
        lootCrate = uploadBitmap(drawSupplyCrateTexture()),
        solidWhite = uploadBitmap(drawSolidWhite())
    )

    private fun uploadBitmap(bitmap: Bitmap): Int {
        val ids = IntArray(1)
        GLES20.glGenTextures(1, ids, 0)
        val texId = ids[0]
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, texId)
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR_MIPMAP_LINEAR)
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR)
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_REPEAT)
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_REPEAT)
        GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bitmap, 0)
        GLES20.glGenerateMipmap(GLES20.GL_TEXTURE_2D)
        bitmap.recycle()
        return texId
    }

    private fun drawRuinedSkyscraperWall(): Bitmap {
        val bmp = Bitmap.createBitmap(256, 256, Bitmap.Config.ARGB_8888)
        val c = Canvas(bmp)
        c.drawColor(Color.rgb(34, 38, 44))
        val p = Paint(Paint.ANTI_ALIAS_FLAG)

        // Weathered concrete floor slabs
        p.color = Color.rgb(46, 52, 58)
        for (y in 0 until 256 step 64) {
            c.drawRect(0f, y.toFloat(), 256f, y + 10f, p)
        }

        // Shattered & burnt office windows
        for (row in 0 until 4) {
            for (col in 0 until 4) {
                val wx = 14f + col * 60f
                val wy = 14f + row * 64f
                val broken = (row * 5 + col * 7) % 3 == 0
                val lit = (row * 3 + col * 11) % 7 == 0
                p.color = when {
                    lit -> Color.rgb(190, 145, 70) // Dim emergency amber light
                    broken -> Color.rgb(10, 12, 16) // Pitch dark blown-out interior
                    else -> Color.rgb(22, 34, 42) // Intact tinted glass
                }
                c.drawRect(wx, wy, wx + 46f, wy + 42f, p)
                if (broken) {
                    // Jagged glass shards
                    p.color = Color.rgb(70, 90, 102)
                    val shard = Path().apply {
                        moveTo(wx, wy)
                        lineTo(wx + 16f, wy)
                        lineTo(wx, wy + 20f)
                        close()
                    }
                    c.drawPath(shard, p)
                }
            }
        }
        // Scorch marks & exposed cracks
        p.color = Color.argb(125, 14, 14, 16)
        c.drawCircle(185f, 125f, 44f, p)
        return bmp
    }

    private fun drawRuinedStorefrontWall(): Bitmap {
        val bmp = Bitmap.createBitmap(256, 256, Bitmap.Config.ARGB_8888)
        val c = Canvas(bmp)
        c.drawColor(Color.rgb(58, 44, 40)) // Gritty urban brick
        val p = Paint(Paint.ANTI_ALIAS_FLAG)

        // Brick courses
        p.color = Color.rgb(46, 34, 31)
        for (y in 0 until 120 step 16) {
            c.drawRect(0f, y.toFloat(), 256f, y + 3f, p)
        }

        // Broken shop sign (half-flickering neon)
        p.color = Color.rgb(18, 22, 26)
        c.drawRect(14f, 82f, 242f, 124f, p)
        p.color = Color.rgb(0, 210, 240)
        c.drawRect(24f, 92f, 128f, 108f, p)
        p.color = Color.rgb(48, 54, 60) // Dead half of sign
        c.drawRect(140f, 94f, 232f, 108f, p)

        // Damaged roll-up shutter & dark broken doorway
        p.color = Color.rgb(28, 32, 38)
        c.drawRect(20f, 134f, 236f, 256f, p)
        p.color = Color.rgb(44, 50, 58)
        for (y in 138 until 205 step 12) {
            c.drawRect(24f, y.toFloat(), 232f, y + 5f, p)
        }
        p.color = Color.rgb(10, 12, 14)
        c.drawRect(44f, 198f, 160f, 256f, p)
        return bmp
    }

    private fun drawDamagedHospitalWall(): Bitmap {
        val bmp = Bitmap.createBitmap(256, 256, Bitmap.Config.ARGB_8888)
        val c = Canvas(bmp)
        c.drawColor(Color.rgb(52, 60, 64))
        val p = Paint(Paint.ANTI_ALIAS_FLAG)

        // Medical concrete cladding + shattered glass bays
        p.shader = LinearGradient(0f, 0f, 0f, 256f, Color.rgb(68, 78, 84), Color.rgb(34, 40, 44), Shader.TileMode.CLAMP)
        c.drawRect(10f, 10f, 246f, 246f, p)
        p.shader = null

        // Red cross / Quarantine stripe
        p.color = Color.rgb(195, 40, 58)
        c.drawRect(112f, 34f, 144f, 94f, p)
        c.drawRect(98f, 48f, 158f, 80f, p)

        // Yellow quarantine tape stripe
        p.color = Color.rgb(235, 175, 30)
        c.drawRect(0f, 136f, 256f, 162f, p)
        p.color = Color.rgb(20, 22, 24)
        for (x in -24 until 280 step 36) {
            val stripe = Path().apply {
                moveTo(x.toFloat(), 136f)
                lineTo(x + 16f, 136f)
                lineTo(x + 32f, 162f)
                lineTo(x + 16f, 162f)
                close()
            }
            c.drawPath(stripe, p)
        }
        return bmp
    }

    private fun drawConcreteRubbleWall(): Bitmap {
        val bmp = Bitmap.createBitmap(256, 256, Bitmap.Config.ARGB_8888)
        val c = Canvas(bmp)
        c.drawColor(Color.rgb(58, 62, 66))
        val p = Paint(Paint.ANTI_ALIAS_FLAG)
        p.color = Color.rgb(72, 76, 80)
        c.drawRect(6f, 6f, 120f, 122f, p)
        c.drawRect(132f, 6f, 250f, 122f, p)
        c.drawRect(6f, 134f, 250f, 250f, p)

        // Cracks & rebar
        p.color = Color.rgb(28, 30, 34)
        p.strokeWidth = 5f
        c.drawLine(32f, 40f, 110f, 180f, p)
        c.drawLine(170f, 10f, 210f, 150f, p)
        p.color = Color.rgb(225, 165, 32)
        c.drawRect(0f, 22f, 256f, 40f, p)
        return bmp
    }

    private fun drawCityAsphaltStreet(): Bitmap {
        val bmp = Bitmap.createBitmap(256, 256, Bitmap.Config.ARGB_8888)
        val c = Canvas(bmp)
        c.drawColor(Color.rgb(24, 27, 32)) // Realistic dark asphalt
        val p = Paint(Paint.ANTI_ALIAS_FLAG)

        // Subtle asphalt grain & puddle sheen
        p.shader = RadialGradient(92f, 110f, 74f, Color.argb(45, 70, 110, 130), Color.TRANSPARENT, Shader.TileMode.CLAMP)
        c.drawCircle(92f, 110f, 74f, p)
        p.shader = null

        // Sidewalk curb lines & dashed street center markings
        p.color = Color.rgb(44, 48, 54)
        c.drawRect(0f, 0f, 256f, 14f, p)
        c.drawRect(0f, 242f, 256f, 256f, p)

        p.color = Color.argb(145, 220, 175, 52)
        c.drawRect(122f, 28f, 134f, 92f, p)
        c.drawRect(122f, 164f, 134f, 228f, p)

        // Road cracks
        p.color = Color.rgb(12, 14, 18)
        p.strokeWidth = 3f
        c.drawLine(30f, 60f, 110f, 138f, p)
        c.drawLine(150f, 190f, 225f, 140f, p)
        return bmp
    }

    private fun drawHelipadDeck(): Bitmap {
        val bmp = Bitmap.createBitmap(256, 256, Bitmap.Config.ARGB_8888)
        val c = Canvas(bmp)
        c.drawColor(Color.rgb(34, 40, 44))
        val p = Paint(Paint.ANTI_ALIAS_FLAG)
        p.style = Paint.Style.STROKE
        p.strokeWidth = 8f
        p.color = Color.rgb(225, 175, 35)
        c.drawCircle(128f, 128f, 96f, p)
        p.style = Paint.Style.FILL
        c.drawRect(82f, 74f, 98f, 182f, p)
        c.drawRect(158f, 74f, 174f, 182f, p)
        c.drawRect(98f, 120f, 158f, 136f, p)
        return bmp
    }

    private fun drawWreckedCarTexture(): Bitmap {
        val bmp = Bitmap.createBitmap(128, 128, Bitmap.Config.ARGB_8888)
        val c = Canvas(bmp)
        c.drawColor(Color.rgb(66, 74, 82))
        val p = Paint(Paint.ANTI_ALIAS_FLAG)
        // Windows + scorched hood
        p.color = Color.rgb(22, 26, 30)
        c.drawRect(16f, 16f, 112f, 56f, p)
        p.color = Color.rgb(120, 42, 34) // Rust/scorch
        c.drawCircle(90f, 95f, 26f, p)
        return bmp
    }

    private fun drawExplosiveBarrelTexture(): Bitmap {
        val bmp = Bitmap.createBitmap(128, 128, Bitmap.Config.ARGB_8888)
        val c = Canvas(bmp)
        c.drawColor(Color.rgb(198, 34, 42))
        val p = Paint(Paint.ANTI_ALIAS_FLAG)
        p.color = Color.rgb(245, 190, 28)
        c.drawRect(0f, 44f, 128f, 84f, p)
        p.color = Color.rgb(20, 22, 24)
        c.drawRect(0f, 14f, 128f, 22f, p)
        c.drawRect(0f, 106f, 128f, 114f, p)
        return bmp
    }

    private fun drawBeaconTexture(): Bitmap {
        val bmp = Bitmap.createBitmap(128, 128, Bitmap.Config.ARGB_8888)
        val c = Canvas(bmp)
        c.drawColor(Color.rgb(14, 34, 44))
        val p = Paint(Paint.ANTI_ALIAS_FLAG)
        p.color = Color.rgb(0, 229, 255)
        c.drawRect(16f, 16f, 112f, 112f, p)
        p.color = Color.rgb(184, 255, 46)
        c.drawCircle(64f, 64f, 24f, p)
        return bmp
    }

    private fun drawSupplyCrateTexture(): Bitmap {
        val bmp = Bitmap.createBitmap(128, 128, Bitmap.Config.ARGB_8888)
        val c = Canvas(bmp)
        c.drawColor(Color.rgb(44, 62, 46))
        val p = Paint(Paint.ANTI_ALIAS_FLAG)
        p.color = Color.rgb(184, 255, 46)
        c.drawRect(12f, 12f, 116f, 26f, p)
        c.drawRect(54f, 48f, 74f, 96f, p)
        c.drawRect(40f, 62f, 88f, 82f, p)
        return bmp
    }

    private fun drawSolidWhite(): Bitmap {
        val bmp = Bitmap.createBitmap(8, 8, Bitmap.Config.ARGB_8888)
        bmp.eraseColor(Color.WHITE)
        return bmp
    }
}
