package matheo1712.cobbletrainers.client.cache

import matheo1712.cobbletrainers.client.platform.ClientPlatform

import com.mojang.blaze3d.platform.NativeImage
import matheo1712.cobbletrainers.CobblemonTrainers
import matheo1712.cobbletrainers.network.RequestTrainerSkinPayload
import matheo1712.cobbletrainers.network.TrainerSkinPayload
import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.texture.DynamicTexture
import net.minecraft.resources.ResourceLocation

/**
 * The trainer skins the battle phone has been sent, kept as textures the screen can draw.
 *
 * Skins are asked for one at a time, when a row first needs one: a world may hold a hundred
 * trainers and each image is a few kilobytes, so sending them all with the listing would mean
 * a heavy packet for the handful the player actually looks at. [get] is therefore allowed to
 * answer null - it means "asked for, not here yet", and the screen draws a placeholder until
 * the answer lands.
 *
 * A skin that could not be resolved server-side is cached too, as an entry with no texture:
 * without that, every frame would ask again.
 *
 * Everything here runs on the client thread, which is both where the screen renders and where
 * the network handler is dispatched, so no synchronisation is needed.
 */
object TrainerSkinCache {

    /**
     * @param texture Null when the trainer has no resolvable skin.
     * @param slim Which player rig the image is drawn on, Alex when true.
     * @param width Size of the image, needed to blit out of it: a legacy skin is 64×32.
     */
    class Skin(
        val texture: ResourceLocation?,
        val slim: Boolean,
        val width: Int,
        val height: Int,
        val bytes: ByteArray?,
        val modelResource: ResourceLocation? = null,
        val aspects: Set<String> = emptySet()
    )

    private val skins = mutableMapOf<String, Skin>()
    private val pending = mutableSetOf<String>()

    /**
     * The skin of that trainer if it is already here, without asking for it.
     *
     * What the battle intro reads: the server pushes its trainer's skin as the screen goes up,
     * and a request from here would send a second question - one whose answer is empty for a
     * trainer that is not `listed`, and which would land after the good one and replace it.
     */
    fun peek(trainerId: String): Skin? = skins[trainerId]

    /** The skin of that trainer, asking the server for it the first time around. */
    fun get(trainerId: String): Skin? {
        peek(trainerId)?.let { return it }

        if (pending.add(trainerId)) {
            ClientPlatform.current.send(RequestTrainerSkinPayload(trainerId))
        }
        return null
    }

    /** Takes in a server answer, turning its bytes into a texture. */
    fun accept(payload: TrainerSkinPayload) {
        pending.remove(payload.trainerId)
        val next = build(payload)
        val previous = skins.put(payload.trainerId, next)
        // A model has no dynamic player texture. Release the old image when a reload switches
        // from a player skin to a model (same-location image replacement is handled by register).
        previous?.texture?.takeIf { it != next.texture }?.let {
            Minecraft.getInstance().textureManager.release(it)
        }
    }

    /** Drops every texture. Called when leaving a world: the next one may not have the same packs. */
    fun clear() {
        val textureManager = Minecraft.getInstance().textureManager
        skins.values.forEach { skin -> skin.texture?.let { textureManager.release(it) } }
        skins.clear()
        pending.clear()
    }

    private fun build(payload: TrainerSkinPayload): Skin {
        val slim = payload.model.equals("slim", ignoreCase = true)
        val modelResource = payload.modelResource.takeIf { it.isNotEmpty() }?.let(ResourceLocation::tryParse)
        if (modelResource != null) {
            return Skin(null, false, 64, 64, null, modelResource, payload.aspects.toSet())
        }
        if (payload.texture.isEmpty()) return Skin(null, slim, 64, 64, null)

        return try {
            val image = NativeImage.read(payload.texture)
            val location = textureLocation(payload.trainerId)
            // register() replaces whatever sat under that location, so a reloaded skin simply
            // takes the place of the previous one.
            Minecraft.getInstance().textureManager.register(location, DynamicTexture(image))
            Skin(location, slim, image.width, image.height, payload.texture.copyOf())
        } catch (e: Exception) {
            CobblemonTrainers.LOGGER.warn("Unreadable skin for trainer {}: {}", payload.trainerId, e.message)
            Skin(null, slim, 64, 64, null)
        }
    }

    /**
     * A texture path of our own for that trainer. The trainer ID is already made of characters
     * a [ResourceLocation] accepts, so its namespace and path only have to be joined back into
     * one path.
     */
    private fun textureLocation(trainerId: String): ResourceLocation =
        CobblemonTrainers.id("trainer_skin/" + trainerId.replace(':', '/'))
}
