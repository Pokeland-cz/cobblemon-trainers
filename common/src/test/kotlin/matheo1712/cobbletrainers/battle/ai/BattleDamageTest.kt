package matheo1712.cobbletrainers.battle.ai

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class BattleDamageTest {
    @Test fun `stat stages follow the battle formula and saturate at six`() {
        assertEquals(1.0, BattleDamage.stageMultiplier(0))
        assertEquals(1.5, BattleDamage.stageMultiplier(1))
        assertEquals(2.0 / 3.0, BattleDamage.stageMultiplier(-1))
        assertEquals(4.0, BattleDamage.stageMultiplier(Int.MAX_VALUE))
        assertEquals(0.25, BattleDamage.stageMultiplier(Int.MIN_VALUE))
        for (stage in 1..6) {
            assertEquals(1.0, BattleDamage.stageMultiplier(stage) * BattleDamage.stageMultiplier(-stage), 1e-12)
            assertTrue(BattleDamage.stageMultiplier(stage) > BattleDamage.stageMultiplier(stage - 1))
        }
    }
}
