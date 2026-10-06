package matheo1712.cobbletrainers.intro

import com.google.gson.Gson
import net.minecraft.resources.ResourceLocation
import kotlin.test.*

class TrainerIntroTest {
    @Test fun `name fitting preserves short names and fits long names including shadows`() {
        val layer = IntroLayer(type = "text", size = 1.5f, width = 112)
        assertEquals(1.5f, layer.fittedTextSize(42))
        for (pixels in listOf(96, 192, 500)) {
            assertTrue(layer.fittedTextSize(pixels) * (pixels + 1) <= 112.001f)
        }
        assertEquals(1.5f, layer.fittedTextSize(0))
        assertEquals(1.5f, layer.copy(width = null).fittedTextSize(500))
        assertEquals(1.5f, layer.copy(width = 0).fittedTextSize(500))
        assertEquals(112f / 192, layer.copy(shadow = false).fittedTextSize(192))
    }

    @Test fun `partial JSON retains defaults and maps the for field`() {
        val scene = Gson().fromJson("""{"layers":[{"type":"text","at":3,"for":7}]}""", TrainerIntro::class.java)
        assertEquals(100, scene.ticks())
        assertEquals(4, scene.fadeIn)
        assertEquals(10, scene.entranceEnd())
        assertEquals(0, scene.layers.single().offsetX)
        assertEquals(0, scene.layers.single().offsetY)
    }

    @Test fun `duration and skip time stay bounded even on integer overflow`() {
        assertEquals(20, TrainerIntro(duration = Int.MIN_VALUE).ticks())
        assertEquals(200, TrainerIntro(duration = Int.MAX_VALUE).ticks())
        assertEquals(0, TrainerIntro().entranceEnd())
        assertEquals(100, TrainerIntro(layers = listOf(IntroLayer(at = Int.MAX_VALUE, length = 12))).entranceEnd())
        assertEquals(0, TrainerIntro(layers = listOf(IntroLayer(at = -20, length = 12))).entranceEnd())
    }

    @Test fun `teams are only disclosed for trainer Pokemon layers`() {
        assertFalse(TrainerIntro(layers = listOf(IntroLayer(type = "figure"), IntroLayer(type = "team_balls"))).needsTeam())
        assertFalse(TrainerIntro(layers = listOf(IntroLayer(type = "pokemon", who = "PLAYER"))).needsTeam())
        assertTrue(TrainerIntro(layers = listOf(IntroLayer(type = "pokemon"))).needsTeam())
    }

    @Test fun `validation removes undrawable layers while preserving order`() {
        val valid = IntroLayer(type = "image", texture = "pack:intro.png", width = 10, height = 20)
        val fallback = IntroLayer(type = "text", anchor = "unknown", from = "unknown")
        val scene = TrainerIntro(layers = listOf(IntroLayer(type = "typo"), valid, IntroLayer(type = "image"), fallback))
        assertEquals(listOf(valid, fallback), scene.validated(ResourceLocation.parse("pack:intro")).layers)
    }
}
