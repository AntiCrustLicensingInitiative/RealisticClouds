package com.martmists.realisticclouds

import net.minecraft.client.render.BufferBuilder
import net.minecraft.client.render.Tessellator
import net.minecraft.client.render.VertexFormats
import net.minecraft.client.world.ClientWorld
import net.minecraft.util.math.Vec3d
import net.minecraft.world.Heightmap
import org.lwjgl.opengl.GL11.GL_QUADS
import kotlin.math.min
import kotlin.math.roundToInt

object WorldRendererMixinKt {
    lateinit var tessellator: Tessellator
    lateinit var builder: BufferBuilder
    lateinit var playerPosition: Vec3d

    @JvmStatic
    fun renderClouds(tickDelta: Float, x: Double, y: Double, z: Double, world: ClientWorld) {
        playerPosition = Vec3d(x, y, z)
        builder = tessellator.bufferBuilder
        // builder.begin(GL_QUADS, VertexFormats.POSITION_UV_COLOR)

        // xyz is player position
        val r = 2  // radius around player
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
            if (yPos + 1 >= y) {
                return
            }
        }
        val top = world.getTop(Heightmap.Type.WORLD_SURFACE, x, z)
        val maxDistance = y - world.seaLevel
        val currentDistance = top - world.seaLevel
        val transparency = min(currentDistance / maxDistance, 1.0f)
        renderCloud(x.toDouble()- playerPosition.x, y.toDouble()- playerPosition.y, z.toDouble()- playerPosition.z, transparency)
    }

    private fun renderCloud(x: Double, y: Double, z: Double, transparency: Float) {
        // TODO: Offset by tickDelta
        // Face 1 (x, y)
        val transparency = (transparency*255).roundToInt()
        val d = 1
        builder.vertex(x,y,z).color(255, 255, 255, transparency).next()
        builder.vertex(x+d,y,z).color(255, 255, 255, transparency).next()
        builder.vertex(x+d,y+d,z).color(255, 255, 255, transparency).next()
        builder.vertex(x,y+d,z).color(255, 255, 255, transparency).next()
        // Face 2 (x, y, +d)
        builder.vertex(x,y,z+d).color(255, 255, 255, transparency).next()
        builder.vertex(x+d,y,z+d).color(255, 255, 255, transparency).next()
        builder.vertex(x+d,y+d,z+d).color(255, 255, 255, transparency).next()
        builder.vertex(x,y+d,z+d).color(255, 255, 255, transparency).next()
        // Face 3 (x, z)
        builder.vertex(x,y,z).color(255, 255, 255, transparency).next()
        builder.vertex(x+d,y,z).color(255, 255, 255, transparency).next()
        builder.vertex(x+d,y,z+d).color(255, 255, 255, transparency).next()
        builder.vertex(x,y,z+d).color(255, 255, 255, transparency).next()
        // Face 4 (x, z, +d)
        builder.vertex(x,y+d,z).color(255, 255, 255, transparency).next()
        builder.vertex(x+d,y+d,z).color(255, 255, 255, transparency).next()
        builder.vertex(x+d,y+d,z+d).color(255, 255, 255, transparency).next()
        builder.vertex(x,y+d,z+d).color(255, 255, 255, transparency).next()
        // Face 5 (y, z)
        builder.vertex(x,y,z).color(255, 255, 255, transparency).next()
        builder.vertex(x,y+d,z).color(255, 255, 255, transparency).next()
        builder.vertex(x,y+d,z+d).color(255, 255, 255, transparency).next()
        builder.vertex(x,y,z+d).color(255, 255, 255, transparency).next()
        // Face 6 (y, z, +d)
        builder.vertex(x+d,y,z).color(255, 255, 255, transparency).next()
        builder.vertex(x+d,y+d,z).color(255, 255, 255, transparency).next()
        builder.vertex(x+d,y+d,z+d).color(255, 255, 255, transparency).next()
        builder.vertex(x+d,y,z+d).color(255, 255, 255, transparency).next()
    }
}
