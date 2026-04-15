package ru.cherryngine.impl.demo.bedrock.renderer

import net.kyori.adventure.key.Key
import ru.cherryngine.engine.bedrock.entity.BedrockEntity
import ru.cherryngine.engine.bedrock.entity.BedrockEntityRegistry
import ru.cherryngine.engine.bedrock.entity.Fmbe
import ru.cherryngine.engine.bedrock.world.BedrockBlockMapping
import ru.cherryngine.impl.demo.renderer.CubeRenderer
import ru.cherryngine.lib.math.Transform
import ru.cherryngine.lib.math.Vec3D
import ru.cherryngine.lib.math.YawPitch
import java.util.*

/**
 * FMBE (Fox MBE) cube renderer.
 * Invisible fox holds a block in its mouth, FMBE animations control transform.
 */
class BedrockCubeRenderer(
    private val entityRegistry: BedrockEntityRegistry,
    private val blockMapping: BedrockBlockMapping,
) : CubeRenderer {
    private val entities = HashMap<UUID, BedrockEntity>()
    private val entityMaterials = HashMap<UUID, Key>()
    private val sentAnimations = HashMap<UUID, Fmbe.State>()

    override fun onAdd(id: UUID) {
        val entity = Fmbe.createEntity()
        entities[id] = entity
        entityRegistry.add(entity)
    }

    override fun onRemove(id: UUID) {
        val entity = entities.remove(id) ?: return
        entityMaterials.remove(id)
        sentAnimations.remove(id)
        entityRegistry.remove(entity)
    }

    override fun update(
        id: UUID,
        position: Vec3D,
        yawPitch: YawPitch,
        material: Key,
        transform: Transform,
        viewContextIDs: Set<String>,
    ) {
        val entity = entities[id] ?: return
        entity.teleport(position, yawPitch)
        entity.viewContextIDs = viewContextIDs

        if (entity.viewers.isEmpty()) return

        if (entityMaterials[id] != material) {
            Fmbe.sendEquipment(entity, material.toString(), blockMapping)
            entityMaterials[id] = material
        }

        val state = Fmbe.stateFromTransform(transform)
        if (sentAnimations[id] != state) {
            val packets = Fmbe.buildPackets(entity.runtimeEntityId, state)
            packets.forEach { pkt -> entity.viewers.forEach { it.session.sendPacket(pkt) } }
            sentAnimations[id] = state
        }
    }

}
