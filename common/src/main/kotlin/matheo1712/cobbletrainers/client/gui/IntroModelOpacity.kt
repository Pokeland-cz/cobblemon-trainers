package matheo1712.cobbletrainers.client.gui

import com.mojang.blaze3d.pipeline.TextureTarget
import com.mojang.blaze3d.systems.RenderSystem
import com.mojang.blaze3d.vertex.BufferUploader
import com.mojang.blaze3d.vertex.DefaultVertexFormat
import com.mojang.blaze3d.vertex.Tesselator
import com.mojang.blaze3d.vertex.VertexFormat
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.renderer.GameRenderer
import org.lwjgl.opengl.GL11
import org.lwjgl.opengl.GL14
import org.lwjgl.opengl.GL30

/** Fades the completed model, including cutout materials and equipment, as one layer. */
internal class IntroModelOpacity : AutoCloseable {
    private var backdrop: TextureTarget? = null

    fun draw(graphics: GuiGraphics, alpha: Float, render: () -> Unit) {
        if (alpha <= 0f) return
        graphics.setColor(1f, 1f, 1f, 1f)
        if (alpha >= 1f) {
            render()
            return
        }

        val client = Minecraft.getInstance()
        val main = client.mainRenderTarget
        val target = backdrop?.also {
            if (it.width != main.width || it.height != main.height) {
                it.resize(main.width, main.height, Minecraft.ON_OSX)
            }
        } ?: TextureTarget(main.width, main.height, false, Minecraft.ON_OSX).also { backdrop = it }

        // Keep the renderer on the real target: third-party render layers may bind it too.
        // Capture the background, draw normally, then mix that background back over the model.
        // This also avoids translucent clothes revealing surfaces hidden inside the model.
        try {
            GL30.glBindFramebuffer(GL30.GL_READ_FRAMEBUFFER, main.frameBufferId)
            GL30.glBindFramebuffer(GL30.GL_DRAW_FRAMEBUFFER, target.frameBufferId)
            GL30.glBlitFramebuffer(
                0, 0, main.width, main.height, 0, 0, target.width, target.height,
                GL11.GL_COLOR_BUFFER_BIT, GL11.GL_NEAREST
            )
        } finally {
            main.bindWrite(true)
        }

        render()
        graphics.setColor(1f, 1f, 1f, 1f)
        val matrix = graphics.pose().last().pose()
        val width = client.window.guiScaledWidth.toFloat()
        val height = client.window.guiScaledHeight.toFloat()
        RenderSystem.setShader(GameRenderer::getPositionTexShader)
        RenderSystem.setShaderTexture(0, target.colorTextureId)
        RenderSystem.disableDepthTest()
        RenderSystem.depthMask(false)
        RenderSystem.enableBlend()
        // Constant blending ignores the framebuffer's alpha, which entity materials can overwrite.
        GL14.glBlendColor(0f, 0f, 0f, 1f - alpha)
        RenderSystem.blendFuncSeparate(
            GL14.GL_CONSTANT_ALPHA, GL14.GL_ONE_MINUS_CONSTANT_ALPHA, GL11.GL_ZERO, GL11.GL_ONE
        )
        try {
            val buffer = Tesselator.getInstance().begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX)
            buffer.addVertex(matrix, 0f, 0f, 0f).setUv(0f, 1f)
            buffer.addVertex(matrix, 0f, height, 0f).setUv(0f, 0f)
            buffer.addVertex(matrix, width, height, 0f).setUv(1f, 0f)
            buffer.addVertex(matrix, width, 0f, 0f).setUv(1f, 1f)
            BufferUploader.drawWithShader(buffer.buildOrThrow())
        } finally {
            GL14.glBlendColor(0f, 0f, 0f, 0f)
            RenderSystem.defaultBlendFunc()
            RenderSystem.disableBlend()
            RenderSystem.depthMask(true)
            RenderSystem.enableDepthTest()
        }
    }

    override fun close() {
        val target = backdrop ?: return
        backdrop = null
        try {
            target.destroyBuffers()
        } finally {
            // removed() can run inside render() when the intro's timer expires.
            Minecraft.getInstance().mainRenderTarget.bindWrite(true)
        }
    }
}
