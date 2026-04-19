package ru.cherryngine.impl.demo.mcprotocollib.renderer

import net.kyori.adventure.text.Component
import org.geysermc.mcprotocollib.protocol.data.game.entity.metadata.MetadataTypes
import org.geysermc.mcprotocollib.protocol.data.game.entity.metadata.type.BooleanEntityMetadata
import org.geysermc.mcprotocollib.protocol.data.game.entity.metadata.type.IntEntityMetadata
import org.geysermc.mcprotocollib.protocol.data.game.entity.metadata.type.ObjectEntityMetadata
import org.geysermc.mcprotocollib.protocol.data.game.entity.type.EntityType
import ru.cherryngine.engine.core.instance.InstanceSingleton
import ru.cherryngine.engine.core.player.PlayerManager
import ru.cherryngine.engine.mcprotocollib.McProtocolLibEntity
import ru.cherryngine.engine.mcprotocollib.McProtocolLibEntityRegistry
import ru.cherryngine.engine.mcprotocollib.McProtocolLibPlayer
import ru.cherryngine.impl.demo.renderer.AxolotlRenderer
import ru.cherryngine.lib.math.Vec3D
import ru.cherryngine.lib.math.YawPitch
import ru.cherryngine.lib.minecraft.entity.AxolotlMeta
import java.util.*
import kotlin.random.Random

@InstanceSingleton(platform = "mcprotocollib")
class McProtocolLibAxolotlRenderer(
    private val entityRegistry: McProtocolLibEntityRegistry,
    private val playerManager: PlayerManager,
) : AxolotlRenderer {
    private val entities = HashMap<UUID, McProtocolLibEntity>()

    override fun onAdd(id: UUID) {
        val entity = McProtocolLibEntity(
            Random.nextInt(1000, 1_000_000),
            EntityType.AXOLOTL
        ).apply {
            setMetadata(5, BooleanEntityMetadata(5, MetadataTypes.BOOLEAN, true))
            val variant = AxolotlMeta.Variant.entries.random()
            setMetadata(17, IntEntityMetadata(17, MetadataTypes.INT, variant.ordinal))
            setMetadata(3, BooleanEntityMetadata(3, MetadataTypes.BOOLEAN, true))
        }
        entities[id] = entity
        entityRegistry.add(entity)
    }

    override fun onRemove(id: UUID) {
        val entity = entities.remove(id) ?: return
        entityRegistry.remove(entity)
    }

    override fun update(
        id: UUID,
        position: Vec3D,
        yawPitch: YawPitch,
        name: String?,
        hiddenFromPlayer: UUID?,
        viewContextIDs: Set<String>,
    ) {
        val entity = entities[id] ?: return
        entity.teleport(position, yawPitch)
        entity.viewContextIDs = viewContextIDs
        if (hiddenFromPlayer != null) {
            entity.viewerPredicate = { (it as? McProtocolLibPlayer)?.uuid != hiddenFromPlayer }
        } else {
            entity.viewerPredicate = { true }
        }
        if (name != null) {
            entity.setMetadata(
                2,
                ObjectEntityMetadata(2, MetadataTypes.OPTIONAL_COMPONENT, Optional.of(Component.text(name)))
            )
        }
        entity.resendMeta()
    }
}
