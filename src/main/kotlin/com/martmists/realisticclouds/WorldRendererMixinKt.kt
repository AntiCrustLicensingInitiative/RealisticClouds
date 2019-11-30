package com.martmists.realisticclouds

import net.minecraft.client.MinecraftClient
import net.minecraft.client.render.BufferBuilder
import net.minecraft.client.render.Tessellator
import net.minecraft.client.render.VertexFormats
import net.minecraft.client.world.ClientWorld
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Vec3d
import net.minecraft.util.math.noise.PerlinNoiseSampler
import net.minecraft.util.math.noise.SimplexNoiseSampler
import net.minecraft.world.Heightmap
import net.minecraft.world.biome.Biome
import net.minecraft.world.biome.DesertBiome
import org.lwjgl.opengl.GL11.GL_QUADS
import java.util.*
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt
import kotlin.random.Random

object WorldRendererMixinKt {
    lateinit var tessellator: Tessellator
    lateinit var builder: BufferBuilder
    lateinit var playerPosition: Vec3d

    private val noiseSampler = PerlinNoiseSampler(java.util.Random())
    private var partialOffset: Float = 0.0f
    private var positionOffset: Int = 0
    private val clientOptions = MinecraftClient.getInstance().options;
    private var raining = false

    @JvmStatic
    fun renderClouds(tickDelta: Float, x: Double, y: Double, z: Double, world: ClientWorld) {
        raining = world.isRaining
        partialOffset += tickDelta / 100.0f;
        if (partialOffset > 1){
            positionOffset += partialOffset.roundToInt()
            partialOffset %= 1.0f
        }

        playerPosition = Vec3d(x, y, z)
        builder = tessellator.buffer
        builder.begin(GL_QUADS, VertexFormats.POSITION_COLOR)
        // xyz is player position
        val r = (clientOptions.viewDistance-1) * 16
        // radius around player

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

        val temperatures = arrayOf(Biome.TemperatureGroup.OCEAN, Biome.TemperatureGroup.WARM)
        if (temperatures.contains(world.getBiome(BlockPos(x+2, y.roundToInt(), z+2)).temperatureGroup)){
            return
        }

        if (world.isChunkLoaded(x / 16, (z+positionOffset) / 16)) {
            var highest = 0
            for (ddx in -2..2) for (ddz in -2..2) {
                val yPos = world.getTopY(Heightmap.Type.WORLD_SURFACE, (x+ddx), (z+ddz))
                highest = max(highest, yPos)
                if (yPos + 5 - abs(ddx) - abs(ddz) >= y) {
                    return
                }
            }
            val seaCloudDistance = y - world.seaLevel
            val worldCloudsDistance = y - highest
            val noise = noiseSampler.sample(x.toDouble() / 10, (z.toDouble()+positionOffset) / 10, 2.0, 0.5, 3.0)
            val dr = if (raining) 0.6 else 0.0
            if (noise > 0 -(0.9*worldCloudsDistance / seaCloudDistance) + dr) {
                return
            }
            renderCloud((x).toDouble() - playerPosition.x, y.toDouble() - playerPosition.y, (z).toDouble() - playerPosition.z - partialOffset)
        }
    }

    private fun renderCloud(x: Double, y: Double, z: Double) {
        val transparency = (200f / 255)
        val colorscale = if (raining) 0.7f else 0.9f
        val rx = x + playerPosition.x
        val ry = y + playerPosition.y
        val rz = z + playerPosition.z + partialOffset
        val d = 1 + Random((rx*ry-ry*rz+rx*.2-rz+rx*ry*rz).roundToInt()).nextDouble(-0.3, 1.0)
        // Face 1 (x, y)
        builder.vertex(x,y,z).color(colorscale, colorscale, colorscale, transparency).next()
        builder.vertex(x+d,y,z).color(colorscale, colorscale, colorscale, transparency).next()
        builder.vertex(x+d,y+d,z).color(colorscale, colorscale, colorscale, transparency).next()
        builder.vertex(x,y+d,z).color(colorscale, colorscale, colorscale, transparency).next()
        // Face 2 (x, y, +d)
        builder.vertex(x+d,y+d,z+d).color(colorscale, colorscale, colorscale, transparency).next()
        builder.vertex(x+d,y,z+d).color(colorscale, colorscale, colorscale, transparency).next()
        builder.vertex(x,y,z+d).color(colorscale, colorscale, colorscale, transparency).next()
        builder.vertex(x,y+d,z+d).color(colorscale, colorscale, colorscale, transparency).next()
        // Face 3 (x, z)
        builder.vertex(x,y,z).color(colorscale, colorscale, colorscale, transparency).next()
        builder.vertex(x,y,z+d).color(colorscale, colorscale, colorscale, transparency).next()
        builder.vertex(x+d,y,z+d).color(colorscale, colorscale, colorscale, transparency).next()
        builder.vertex(x+d,y,z).color(colorscale, colorscale, colorscale, transparency).next()
        // Face 4 (x, z, +d)
        builder.vertex(x,y+d,z).color(colorscale, colorscale, colorscale, transparency).next()
        builder.vertex(x+d,y+d,z).color(colorscale, colorscale, colorscale, transparency).next()
        builder.vertex(x+d,y+d,z+d).color(colorscale, colorscale, colorscale, transparency).next()
        builder.vertex(x,y+d,z+d).color(colorscale, colorscale, colorscale, transparency).next()
        // Face 5 (y, z)
        builder.vertex(x,y,z).color(colorscale, colorscale, colorscale, transparency).next()
        builder.vertex(x,y+d,z).color(colorscale, colorscale, colorscale, transparency).next()
        builder.vertex(x,y+d,z+d).color(colorscale, colorscale, colorscale, transparency).next()
        builder.vertex(x,y,z+d).color(colorscale, colorscale, colorscale, transparency).next()
        // Face 6 (y, z, +d)
        builder.vertex(x+d,y,z).color(colorscale, colorscale, colorscale, transparency).next()
        builder.vertex(x+d,y,z+d).color(colorscale, colorscale, colorscale, transparency).next()
        builder.vertex(x+d,y+d,z+d).color(colorscale, colorscale, colorscale, transparency).next()
        builder.vertex(x+d,y+d,z).color(colorscale, colorscale, colorscale, transparency).next()
    }
}
