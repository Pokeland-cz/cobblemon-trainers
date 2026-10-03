package matheo1712.cobbletrainers.client.gui

import com.mojang.blaze3d.vertex.VertexConsumer

/** Measures projected model geometry without allocating a GPU buffer. */
internal class IntroPokemonBounds : VertexConsumer {
    private var minX = Float.POSITIVE_INFINITY
    private var minY = Float.POSITIVE_INFINITY
    private var maxX = Float.NEGATIVE_INFINITY
    private var maxY = Float.NEGATIVE_INFINITY

    val width get() = maxX - minX
    val height get() = maxY - minY
    val centerX get() = (minX + maxX) / 2f
    val centerY get() = (minY + maxY) / 2f
    val extent get() = maxOf(width, height)

    override fun addVertex(x: Float, y: Float, z: Float): VertexConsumer {
        minX = minOf(minX, x)
        minY = minOf(minY, y)
        maxX = maxOf(maxX, x)
        maxY = maxOf(maxY, y)
        return this
    }

    override fun setColor(red: Int, green: Int, blue: Int, alpha: Int): VertexConsumer = this
    override fun setUv(u: Float, v: Float): VertexConsumer = this
    override fun setUv1(u: Int, v: Int): VertexConsumer = this
    override fun setUv2(u: Int, v: Int): VertexConsumer = this
    override fun setNormal(x: Float, y: Float, z: Float): VertexConsumer = this
}
