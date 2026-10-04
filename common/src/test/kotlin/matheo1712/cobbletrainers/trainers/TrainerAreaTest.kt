package matheo1712.cobbletrainers.trainers

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class TrainerAreaTest {
    @Test fun `corners are inclusive and their order does not matter`() {
        for (area in listOf(TrainerArea(listOf(-10, -20), listOf(30, 40)), TrainerArea(listOf(30, 40), listOf(-10, -20)))) {
            assertTrue(area.contains(-10, -20))
            assertTrue(area.contains(30, 40))
            assertTrue(area.contains(0, 0))
            assertFalse(area.contains(-11, 0))
            assertFalse(area.contains(31, 0))
            assertFalse(area.contains(0, -21))
            assertFalse(area.contains(0, 41))
        }
    }

    @Test fun `missing or oversized coordinates never match`() {
        for (coordinates in listOf(emptyList(), listOf(1), listOf(1, 2, 3))) {
            assertFalse(TrainerArea(coordinates, listOf(0, 0)).contains(0, 0))
            assertFalse(TrainerArea(listOf(0, 0), coordinates).contains(0, 0))
        }
    }

    @Test fun `a single block and extreme coordinates are supported`() {
        assertTrue(TrainerArea(listOf(4, -3), listOf(4, -3)).contains(4, -3))
        assertFalse(TrainerArea(listOf(4, -3), listOf(4, -3)).contains(4, -2))
        assertTrue(TrainerArea(listOf(Int.MIN_VALUE, Int.MAX_VALUE), listOf(Int.MAX_VALUE, Int.MIN_VALUE)).contains(0, 0))
    }
}
