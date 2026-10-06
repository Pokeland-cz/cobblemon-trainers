package matheo1712.cobbletrainers.parser

import kotlin.test.Test
import kotlin.test.assertEquals

class ShowdownTeamParserTest {
    @Test fun `empty entries do not declare Pokemon`() {
        assertEquals(0, ShowdownTeamParser.countPokemon(emptyList()))
        assertEquals(0, ShowdownTeamParser.countPokemon(listOf("", " \r\n\t\n")))
    }

    @Test fun `each array entry starts a new Pokemon`() {
        assertEquals(2, ShowdownTeamParser.countPokemon(listOf("Pikachu\n- Thunderbolt", "Eevee")))
    }

    @Test fun `pasted exports accept all line endings and whitespace separators`() {
        for (newline in listOf("\n", "\r\n", "\r")) {
            val export = listOf("", "Pikachu", "- Thunderbolt", " \t", "", "Eevee", "- Tackle", "")
                .joinToString(newline)
            assertEquals(2, ShowdownTeamParser.countPokemon(listOf(export)), newline)
        }
    }

    @Test fun `large listings count blocks without parsing species or moves`() {
        assertEquals(6000, ShowdownTeamParser.countPokemon(List(1000) { List(6) { "Unknown\n- Unknown" }.joinToString("\n\n") }))
    }
}
