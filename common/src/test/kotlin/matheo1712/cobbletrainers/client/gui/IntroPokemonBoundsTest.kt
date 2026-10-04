package matheo1712.cobbletrainers.client.gui

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse

class IntroPokemonBoundsTest {
    @Test fun `projected bounds ignore depth and cover every vertex`() {
        val bounds = IntroPokemonBounds()
        bounds.addVertex(12f, -5f, 1000f)
        bounds.addVertex(-4f, 7f, -1000f)
        bounds.addVertex(0f, 0f, 0f)
        assertEquals(16f, bounds.width)
        assertEquals(12f, bounds.height)
        assertEquals(4f, bounds.centerX)
        assertEquals(1f, bounds.centerY)
        assertEquals(16f, bounds.extent)
    }

    @Test fun `empty or point geometry cannot be scaled to fill the screen`() {
        val empty = IntroPokemonBounds()
        assertFalse(empty.extent.isFinite())
        val point = IntroPokemonBounds()
        point.addVertex(2f, 3f, 4f)
        assertEquals(0f, point.extent)
    }
}
