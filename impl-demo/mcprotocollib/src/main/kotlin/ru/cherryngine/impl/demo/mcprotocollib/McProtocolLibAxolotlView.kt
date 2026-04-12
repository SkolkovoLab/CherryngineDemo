package ru.cherryngine.impl.demo.mcprotocollib

import net.kyori.adventure.text.Component
import org.geysermc.mcprotocollib.protocol.data.game.entity.metadata.MetadataTypes
import org.geysermc.mcprotocollib.protocol.data.game.entity.metadata.type.BooleanEntityMetadata
import org.geysermc.mcprotocollib.protocol.data.game.entity.metadata.type.IntEntityMetadata
import org.geysermc.mcprotocollib.protocol.data.game.entity.metadata.type.ObjectEntityMetadata
import org.geysermc.mcprotocollib.protocol.data.game.entity.type.EntityType
import ru.cherryngine.engine.core.player.PlayerManager
import ru.cherryngine.engine.mcprotocollib.McProtocolLibEntity
import ru.cherryngine.engine.mcprotocollib.McProtocolLibEntityRegistry
import ru.cherryngine.engine.mcprotocollib.McProtocolLibPlayer
import ru.cherryngine.impl.demo.view.AxolotlView
import ru.cherryngine.lib.math.Vec3D
import ru.cherryngine.lib.math.YawPitch
import ru.cherryngine.lib.minecraft.entity.AxolotlMeta
import java.util.*
import kotlin.random.Random

class McProtocolLibAxolotlView(
    private val entityRegistry: McProtocolLibEntityRegistry,
    private val playerManager: PlayerManager,
) : AxolotlView {
    private val modelId: UUID = UUID.randomUUID()

    init {
        entityRegistry.getOrCreate(modelId) {
            McProtocolLibEntity(
                Random.nextInt(1000, 1_000_000),
                EntityType.AXOLOTL
            ).apply {
                // Entity base index 5: has no gravity
                setMetadata(5, BooleanEntityMetadata(5, MetadataTypes.BOOLEAN, true))
                // Axolotl index 17: variant
                val variant = AxolotlMeta.Variant.entries.random()
                setMetadata(17, IntEntityMetadata(17, MetadataTypes.INT, variant.ordinal))
                // Entity base index 3: custom name visible
                setMetadata(3, BooleanEntityMetadata(3, MetadataTypes.BOOLEAN, true))
            }
        }
    }

    override fun updatePosition(position: Vec3D, yawPitch: YawPitch) {
        entityRegistry.get(modelId)?.teleport(position, yawPitch)
    }

    override fun setName(name: String?) {
        val entity = entityRegistry.get(modelId) ?: return
        if (name != null) {
            // Entity base index 2: custom name (Optional<Component>)
            entity.setMetadata(
                2,
                ObjectEntityMetadata(2, MetadataTypes.OPTIONAL_COMPONENT, Optional.of(Component.text(name)))
            )
        }
        entity.resendMeta()
    }

    override fun setHiddenFromPlayer(uuid: UUID?) {
        val entity = entityRegistry.get(modelId) ?: return
        if (uuid != null) {
            entity.viewerPredicate = { (it as? McProtocolLibPlayer)?.uuid != uuid }
        } else {
            entity.viewerPredicate = { true }
        }
    }

    override fun setViewContextIDs(contextIDs: Set<String>) {
        entityRegistry.get(modelId)?.viewContextIDs = contextIDs
    }

    override fun destroy() {
        entityRegistry.remove(modelId)
    }
}
