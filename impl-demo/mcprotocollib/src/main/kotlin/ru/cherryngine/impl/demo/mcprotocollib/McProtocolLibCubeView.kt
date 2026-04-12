package ru.cherryngine.impl.demo.mcprotocollib

import net.kyori.adventure.key.Key
import org.cloudburstmc.math.imaginary.Quaternionf
import org.cloudburstmc.math.vector.Vector3f
import org.geysermc.mcprotocollib.protocol.data.game.entity.metadata.MetadataTypes
import org.geysermc.mcprotocollib.protocol.data.game.entity.metadata.type.BooleanEntityMetadata
import org.geysermc.mcprotocollib.protocol.data.game.entity.metadata.type.ObjectEntityMetadata
import org.geysermc.mcprotocollib.protocol.data.game.entity.type.EntityType
import ru.cherryngine.engine.mcprotocollib.McProtocolLibEntity
import ru.cherryngine.engine.mcprotocollib.McProtocolLibEntityRegistry
import ru.cherryngine.impl.demo.view.CubeView
import ru.cherryngine.lib.math.Transform
import ru.cherryngine.lib.math.Vec3D
import ru.cherryngine.lib.math.YawPitch
import ru.cherryngine.lib.minecraft.registry.Registries
import java.util.*
import kotlin.random.Random
import org.geysermc.mcprotocollib.protocol.data.game.item.ItemStack as McplItemStack

class McProtocolLibCubeView(
    private val entityRegistry: McProtocolLibEntityRegistry,
) : CubeView {
    private val modelId: UUID = UUID.randomUUID()

    init {
        entityRegistry.getOrCreate(modelId) {
            McProtocolLibEntity(
                Random.nextInt(1000, 1_000_000),
                EntityType.ITEM_DISPLAY
            )
        }
    }

    override fun updatePosition(position: Vec3D, yawPitch: YawPitch) {
        entityRegistry.get(modelId)?.teleport(position, yawPitch)
    }

    override fun updateMaterial(material: Key) {
        val entity = entityRegistry.get(modelId) ?: return
        val itemId = Registries.item[material].value.id
        // Item Display index 23: displayed item
        entity.setMetadata(
            23,
            ObjectEntityMetadata(23, MetadataTypes.ITEM_STACK, McplItemStack(itemId, 1))
        )
        entity.resendMeta()
    }

    override fun updateTransform(transform: Transform) {
        val entity = entityRegistry.get(modelId) ?: return
        // Entity base index 5: has no gravity
        entity.setMetadata(5, BooleanEntityMetadata(5, MetadataTypes.BOOLEAN, true))
        // Display index 11: translation (Vector3f)
        entity.setMetadata(
            11,
            ObjectEntityMetadata(
                11, MetadataTypes.VECTOR3,
                Vector3f.from(
                    transform.translation.x.toFloat(),
                    transform.translation.y.toFloat(),
                    transform.translation.z.toFloat()
                )
            )
        )
        // Display index 13: rotation left (Quaternionf)
        entity.setMetadata(
            13,
            ObjectEntityMetadata(
                13, MetadataTypes.QUATERNION,
                Quaternionf.from(
                    transform.rotation.x.toFloat(),
                    transform.rotation.y.toFloat(),
                    transform.rotation.z.toFloat(),
                    transform.rotation.w.toFloat()
                )
            )
        )
        // Display index 12: scale (Vector3f)
        entity.setMetadata(
            12,
            ObjectEntityMetadata(
                12, MetadataTypes.VECTOR3,
                Vector3f.from(
                    transform.scale.x.toFloat(),
                    transform.scale.y.toFloat(),
                    transform.scale.z.toFloat()
                )
            )
        )
        entity.resendMeta()
    }

    override fun setViewContextIDs(contextIDs: Set<String>) {
        entityRegistry.get(modelId)?.viewContextIDs = contextIDs
    }

    override fun destroy() {
        entityRegistry.remove(modelId)
    }
}
