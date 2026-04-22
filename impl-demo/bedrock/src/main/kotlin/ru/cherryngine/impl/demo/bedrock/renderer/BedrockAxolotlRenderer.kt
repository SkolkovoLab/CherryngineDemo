package ru.cherryngine.impl.demo.bedrock.renderer

import org.cloudburstmc.protocol.bedrock.data.entity.EntityDataTypes
import org.cloudburstmc.protocol.bedrock.data.entity.EntityFlag
import ru.cherryngine.engine.core.instance.InstanceSingleton
import ru.cherryngine.engine.core.player.Player
import ru.cherryngine.impl.demo.renderer.AxolotlRenderer
import ru.cherryngine.lib.math.Vec3D
import ru.cherryngine.lib.math.YawPitch
import ru.cherryngine.platform.minecraft.bedrock.BedrockPlayer
import ru.cherryngine.platform.minecraft.bedrock.entity.BedrockEntity
import ru.cherryngine.platform.minecraft.bedrock.entity.BedrockEntityRegistry
import java.util.*

@InstanceSingleton(platform = "bedrock")
class BedrockAxolotlRenderer(
    private val entityRegistry: BedrockEntityRegistry,
) : AxolotlRenderer<BedrockPlayer> {
    private val entities = HashMap<UUID, BedrockEntity>()

    override fun canHandle(target: Player): Boolean = target is BedrockPlayer

    override fun onAdd(id: UUID) {
        val entity = BedrockEntity(identifier = "minecraft:axolotl")
        entity.metadata.getOrCreateFlags().apply {
            put(EntityFlag.HAS_GRAVITY, false)
        }
        entity.metadata[EntityDataTypes.SCALE] = 1.0f
        entity.metadata[EntityDataTypes.VARIANT] = 0 // random variant
        entity.metadata.getOrCreateFlags()[EntityFlag.CAN_SHOW_NAME] = true
        entity.metadata.getOrCreateFlags()[EntityFlag.ALWAYS_SHOW_NAME] = true
        entities[id] = entity
        entityRegistry.add(entity)
    }

    override fun onRemove(id: UUID) {
        val entity = entities.remove(id) ?: return
        entityRegistry.remove(entity)
    }

    override fun show(id: UUID, player: BedrockPlayer) {
        entities[id]?.subscribers?.add(player)
    }

    override fun hide(id: UUID, player: BedrockPlayer) {
        entities[id]?.subscribers?.remove(player)
    }

    override fun update(
        id: UUID,
        position: Vec3D,
        yawPitch: YawPitch,
        name: String?,
        hiddenFromPlayer: UUID?,
    ) {
        val entity = entities[id] ?: return
        entity.teleport(position, yawPitch)
        entity.viewerPredicate = if (hiddenFromPlayer != null) {
            { it.uuid != hiddenFromPlayer }
        } else {
            { true }
        }
        if (name != null) {
            entity.metadata[EntityDataTypes.NAME] = name
        }
        entity.resendMeta()
    }
}
