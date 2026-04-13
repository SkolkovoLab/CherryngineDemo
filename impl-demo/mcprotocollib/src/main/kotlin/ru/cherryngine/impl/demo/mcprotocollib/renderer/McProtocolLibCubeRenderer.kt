package ru.cherryngine.impl.demo.mcprotocollib.renderer

import net.kyori.adventure.key.Key
import org.cloudburstmc.math.imaginary.Quaternionf
import org.cloudburstmc.math.vector.Vector3f
import org.geysermc.mcprotocollib.protocol.data.game.entity.metadata.MetadataTypes
import org.geysermc.mcprotocollib.protocol.data.game.entity.metadata.type.BooleanEntityMetadata
import org.geysermc.mcprotocollib.protocol.data.game.entity.metadata.type.IntEntityMetadata
import org.geysermc.mcprotocollib.protocol.data.game.entity.metadata.type.ObjectEntityMetadata
import org.geysermc.mcprotocollib.protocol.data.game.entity.type.EntityType
import ru.cherryngine.engine.mcprotocollib.McProtocolLibEntity
import ru.cherryngine.engine.mcprotocollib.McProtocolLibEntityRegistry
import ru.cherryngine.impl.demo.renderer.CubeRenderer
import ru.cherryngine.lib.math.Transform
import ru.cherryngine.lib.math.Vec3D
import ru.cherryngine.lib.math.YawPitch
import ru.cherryngine.lib.minecraft.registry.Registries
import java.util.*
import kotlin.random.Random
import org.geysermc.mcprotocollib.protocol.data.game.item.ItemStack as McplItemStack

class McProtocolLibCubeRenderer(
    private val entityRegistry: McProtocolLibEntityRegistry,
) : CubeRenderer {
    private val entities = HashMap<UUID, McProtocolLibEntity>()

    override fun onAdd(id: UUID) {
        val entity = McProtocolLibEntity(
            Random.nextInt(1000, 1_000_000),
            EntityType.ITEM_DISPLAY
        )
        entity.setMetadata(5, BooleanEntityMetadata(5, MetadataTypes.BOOLEAN, true))
        entity.setMetadata(9, IntEntityMetadata(9, MetadataTypes.INT, 1))
        entity.setMetadata(10, IntEntityMetadata(10, MetadataTypes.INT, 1))
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
        material: Key,
        transform: Transform,
        viewContextIDs: Set<String>,
    ) {
        val entity = entities[id] ?: return
        entity.teleport(position, yawPitch)
        entity.viewContextIDs = viewContextIDs
        val itemId = Registries.item[material].value.id
        entity.setMetadata(23, ObjectEntityMetadata(23, MetadataTypes.ITEM_STACK, McplItemStack(itemId, 1)))
        entity.setMetadata(
            11, ObjectEntityMetadata(11, MetadataTypes.VECTOR3,
                Vector3f.from(transform.translation.x.toFloat(), transform.translation.y.toFloat(), transform.translation.z.toFloat()))
        )
        entity.setMetadata(
            13, ObjectEntityMetadata(13, MetadataTypes.QUATERNION,
                Quaternionf.from(transform.rotation.x.toFloat(), transform.rotation.y.toFloat(), transform.rotation.z.toFloat(), transform.rotation.w.toFloat()))
        )
        entity.setMetadata(
            12, ObjectEntityMetadata(12, MetadataTypes.VECTOR3,
                Vector3f.from(transform.scale.x.toFloat(), transform.scale.y.toFloat(), transform.scale.z.toFloat()))
        )
        entity.setMetadata(8, IntEntityMetadata(8, MetadataTypes.INT, 0))
        entity.resendMeta()
    }
}
