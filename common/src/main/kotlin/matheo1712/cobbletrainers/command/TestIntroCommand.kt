package matheo1712.cobbletrainers.command

import com.cobblemon.mod.common.util.isInBattle
import com.mojang.brigadier.arguments.IntegerArgumentType
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType
import matheo1712.cobbletrainers.CobblemonTrainers
import matheo1712.cobbletrainers.battle.TrainerBattleIntro
import matheo1712.cobbletrainers.intro.TrainerIntros
import matheo1712.cobbletrainers.network.BattleIntroNetworking
import matheo1712.cobbletrainers.network.BattleIntroPayload
import matheo1712.cobbletrainers.platform.Platform
import net.minecraft.commands.CommandSourceStack
import net.minecraft.commands.Commands
import net.minecraft.commands.SharedSuggestionProvider
import net.minecraft.commands.arguments.ResourceLocationArgument

/** Plays a scene without creating an NPC or scheduling a battle. */
object TestIntroCommand {
    fun node() = Commands.literal("testintro")
        .then(Commands.argument("intro", ResourceLocationArgument.id())
            .suggests { _, builder -> SharedSuggestionProvider.suggestResource(TrainerIntros.ids(), builder) }
            .executes { execute(it) }
            .then(Commands.argument("pokemon_count", IntegerArgumentType.integer(0, 6))
                .executes { execute(it, IntegerArgumentType.getInteger(it, "pokemon_count")) }
                .then(Commands.argument("level", IntegerArgumentType.integer(1, 100))
                    .executes { execute(it, IntegerArgumentType.getInteger(it, "pokemon_count"),
                        IntegerArgumentType.getInteger(it, "level")) })))

    private fun execute(context: CommandContext<CommandSourceStack>, count: Int = -1, level: Int = 50): Int {
        val player = context.source.playerOrException
        val input = ResourceLocationArgument.getId(context, "intro")
        val id = if (input.namespace == "minecraft") TrainerIntros.idOf(input.path)!! else input
        val scene = TrainerIntros.get(id) ?: throw error("unknown").create()
        if (player.isInBattle() || TrainerBattleIntro.isPending(player)) throw error("busy").create()
        if (!BattleIntroNetworking.canOpen(player)) throw error("client_required").create()
        Platform.current.send(player, BattleIntroPayload(
            introId = id.toString(), trainerId = "", trainerName = player.gameProfile.name,
            category = "", level = level, teamSize = count, npcId = player.id,
            scene = scene, team = emptyList(), preview = true
        ))
        return 1
    }

    private fun error(key: String) = SimpleCommandExceptionType(CobblemonTrainers.lang("command.testintro.$key"))
}
