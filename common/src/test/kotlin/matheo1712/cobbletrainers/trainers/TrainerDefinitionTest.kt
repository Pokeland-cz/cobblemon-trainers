package matheo1712.cobbletrainers.trainers

import com.google.gson.Gson
import net.minecraft.resources.ResourceLocation
import kotlin.test.*

class TrainerDefinitionTest {
    @Test fun `partial trainer JSON preserves nested defaults and explicit silence`() {
        val gson = Gson()
        val trainer = gson.fromJson("{}", TrainerDefinition::class.java)
        assertEquals(TrainerDefinition(), trainer)
        assertTrue(trainer.callable())
        assertNull(trainer.requirements())
        val silent = gson.fromJson("""{"battle":{"music":null,"level":50},"notCallable":true}""", TrainerDefinition::class.java)
        assertNull(silent.battle.music)
        assertEquals(50, silent.battle.level)
        assertEquals(5, silent.battle.difficulty)
        assertFalse(silent.callable())
    }

    @Test fun `presentation alone never locks a trainer but real requirements do`() {
        assertNull(TrainerDefinition(requires = TrainerRequirements(hidden = true, message = "Locked")).requirements())
        assertNull(TrainerDefinition(requires = TrainerRequirements(victories = TrainerVictoriesRequirement())).requirements())
        assertNotNull(TrainerDefinition(requires = TrainerRequirements(defeated = listOf("pack:erika"))).requirements())
        val itemLocked = TrainerDefinition(requires = TrainerRequirements(
            items = listOf(TrainerItemRequirement(item = "minecraft:diamond"))
        ))
        assertNotNull(itemLocked.requirements())
    }

    @Test fun `rematch rules accept case variants and keep unknown values permissive`() {
        assertFalse(TrainerProgressRules(rematch = "NEVER").allowsRematch)
        assertTrue(TrainerProgressRules().allowsRematch)
        assertTrue(TrainerProgressRules(rematch = "typo").allowsRematch)
    }

    @Test fun `nested category and persistent trainer identity do not require loaded packs`() {
        val trainer = ResourceLocation.parse("pack:league/elite/erika")
        assertEquals(ResourceLocation.parse("pack:league/elite"), TrainerRegistry.categoryOf(trainer))
        assertNull(TrainerRegistry.categoryOf(ResourceLocation.parse("pack:erika")))
        assertEquals(trainer, TrainerRegistry.idFromAspects(listOf("model-slim", "trainer_id:$trainer")))
        assertNull(TrainerRegistry.idFromAspects(listOf("trainer_id:INVALID ID")))
        assertNull(TrainerRegistry.idFromAspects(listOf("model-slim")))
    }
}
