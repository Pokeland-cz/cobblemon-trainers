package matheo1712.cobbletrainers.network

import io.netty.buffer.Unpooled
import net.minecraft.network.FriendlyByteBuf
import kotlin.test.Test
import kotlin.test.assertEquals

class TrainerTeamCodecTest {
    @Test fun `teams preserve order forms levels and unicode nicknames`() {
        val team = listOf(
            TrainerTeamMember("cobblemon:raichu", listOf("alolan", "shiny"), 50, "Éclair ⚡"),
            TrainerTeamMember("cobblemon:eevee", emptyList(), 1, "")
        )
        for (members in listOf(emptyList(), team)) {
            val buf = FriendlyByteBuf(Unpooled.buffer())
            try {
                TrainerTeamCodec.encode(buf, members)
                buf.writeInt(0x12345678)
                assertEquals(members, TrainerTeamCodec.decode(buf))
                assertEquals(0x12345678, buf.readInt())
                assertEquals(0, buf.readableBytes())
            } finally {
                buf.release()
            }
        }
    }

    @Test fun `wire format remains compatible with the original phone and intro packets`() {
        val buf = FriendlyByteBuf(Unpooled.buffer())
        try {
            buf.writeVarInt(1)
            buf.writeUtf("cobblemon:pikachu")
            buf.writeVarInt(1)
            buf.writeUtf("shiny")
            buf.writeVarInt(42)
            buf.writeUtf("Sparky")
            assertEquals(listOf(TrainerTeamMember("cobblemon:pikachu", listOf("shiny"), 42, "Sparky")), TrainerTeamCodec.decode(buf))
        } finally {
            buf.release()
        }
    }
}
