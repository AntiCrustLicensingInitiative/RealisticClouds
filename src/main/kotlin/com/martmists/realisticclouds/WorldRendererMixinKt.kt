package com.martmists.realisticclouds

import net.minecraft.client.render.BufferBuilder
import net.minecraft.client.render.Tessellator
import net.minecraft.client.render.VertexFormats
import net.minecraft.client.world.ClientWorld
import net.minecraft.util.math.Vec3d
import net.minecraft.util.math.noise.PerlinNoiseSampler
import net.minecraft.world.Heightmap
import org.lwjgl.opengl.GL11.GL_QUADS
import java.util.*
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

object WorldRendererMixinKt {
    lateinit var tessellator: Tessellator
    lateinit var builder: BufferBuilder
    lateinit var playerPosition: Vec3d

    val noiseSampler = PerlinNoiseSampler(Random())
    var tickDelta: Float = 0f

    @JvmStatic
    fun renderClouds(tickDelta: Float, x: Double, y: Double, z: Double, world: ClientWorld) {
        this.tickDelta = tickDelta
        playerPosition = Vec3d(x, y, z)
        builder = tessellator.bufferBuilder
        builder.begin(GL_QUADS, VertexFormats.POSITION_COLOR)

        // xyz is player position
        val r = 127  // radius around player
        for (dx in -r..r) {
            val xPos = x + dx
            for (dz in -r..r) {
                val zPos = z + dz
                spawnCloud(world, xPos.roundToInt(), zPos.roundToInt())
            }
        }
    }

    private fun spawnCloud(world: ClientWorld, x: Int, z: Int) {
        val y = world.dimension.cloudHeight  // cloud height
        for (ddx in -1..1) for (ddz in -1..1) {
            val yPos = world.getTop(Heightmap.Type.WORLD_SURFACE, (x+ddx), (z+ddz))
            if (yPos + 1 >= y || noiseSampler.sample((x+tickDelta).toDouble() / 100, yPos.toDouble(), (z+tickDelta).toDouble() / 100, 5.0, 2.0) > 0) {
                return
            }
        }
        renderCloud((x+tickDelta).toDouble()- playerPosition.x, y.toDouble()- playerPosition.y, (z+tickDelta).toDouble()- playerPosition.z)
    }

    private fun renderCloud(x: Double, y: Double, z: Double) {
        // TODO: Offset by tickDelta
        val transparency = (200f / 255)
        val d = 1
        // Face 1 (x, y)
        builder.vertex(x,y,z).color(1.0f, 1.0f, 1.0f, transparency).next()
        builder.vertex(x+d,y,z).color(1.0f, 1.0f, 1.0f, transparency).next()
        builder.vertex(x+d,y+d,z).color(1.0f, 1.0f, 1.0f, transparency).next()
        builder.vertex(x,y+d,z).color(1.0f, 1.0f, 1.0f, transparency).next()
        // Face 2 (x, y, +d)
        builder.vertex(x+d,y+d,z+d).color(1.0f, 1.0f, 1.0f, transparency).next()
        builder.vertex(x+d,y,z+d).color(1.0f, 1.0f, 1.0f, transparency).next()
        builder.vertex(x,y,z+d).color(1.0f, 1.0f, 1.0f, transparency).next()
        builder.vertex(x,y+d,z+d).color(1.0f, 1.0f, 1.0f, transparency).next()
        // Face 3 (x, z)
        builder.vertex(x,y,z).color(1.0f, 1.0f, 1.0f, transparency).next()
        builder.vertex(x,y,z+d).color(1.0f, 1.0f, 1.0f, transparency).next()
        builder.vertex(x+d,y,z+d).color(1.0f, 1.0f, 1.0f, transparency).next()
        builder.vertex(x+d,y,z).color(1.0f, 1.0f, 1.0f, transparency).next()
        // Face 4 (x, z, +d)
        builder.vertex(x,y+d,z).color(1.0f, 1.0f, 1.0f, transparency).next()
        builder.vertex(x+d,y+d,z).color(1.0f, 1.0f, 1.0f, transparency).next()
        builder.vertex(x+d,y+d,z+d).color(1.0f, 1.0f, 1.0f, transparency).next()
        builder.vertex(x,y+d,z+d).color(1.0f, 1.0f, 1.0f, transparency).next()
        // Face 5 (y, z)
        builder.vertex(x,y,z).color(1.0f, 1.0f, 1.0f, transparency).next()
        builder.vertex(x,y+d,z).color(1.0f, 1.0f, 1.0f, transparency).next()
        builder.vertex(x,y+d,z+d).color(1.0f, 1.0f, 1.0f, transparency).next()
        builder.vertex(x,y,z+d).color(1.0f, 1.0f, 1.0f, transparency).next()
        // Face 6 (y, z, +d)
        builder.vertex(x+d,y,z).color(1.0f, 1.0f, 1.0f, transparency).next()
        builder.vertex(x+d,y,z+d).color(1.0f, 1.0f, 1.0f, transparency).next()
        builder.vertex(x+d,y+d,z+d).color(1.0f, 1.0f, 1.0f, transparency).next()
        builder.vertex(x+d,y+d,z).color(1.0f, 1.0f, 1.0f, transparency).next()
    }
}
