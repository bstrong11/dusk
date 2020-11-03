package rs.dusk.engine.entity.character.npc.detail

import io.mockk.mockk
import org.junit.jupiter.api.BeforeEach
import rs.dusk.cache.definition.data.NPCDefinition
import rs.dusk.cache.definition.decoder.NPCDecoder
import rs.dusk.engine.TimedLoader
import rs.dusk.engine.data.file.FileLoader
import rs.dusk.engine.entity.DetailsDecoderTest

internal class NPCDetailsTest : DetailsDecoderTest<NPCDefinition, NPCDecoder, NPCDetails>() {

    @BeforeEach
    override fun setup() {
        decoder = mockk(relaxed = true)
        super.setup()
    }

    override fun map(id: Int): Map<String, Any> {
        return mapOf("id" to id)
    }

    override fun detail(id: Int): NPCDefinition {
        return NPCDefinition(id)
    }

    override fun details(decoder: NPCDecoder, id: Map<String, Map<String, Any>>, names: Map<Int, String>): NPCDetails {
        return NPCDetails(decoder, id, names)
    }

    override fun loader(loader: FileLoader): TimedLoader<NPCDetails> {
        return NPCDetailsLoader(loader, decoder)
    }
}