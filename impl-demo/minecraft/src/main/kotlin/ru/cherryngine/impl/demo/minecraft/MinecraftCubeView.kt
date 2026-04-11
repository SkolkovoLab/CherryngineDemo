package ru.cherryngine.impl.demo.minecraft

import net.kyori.adventure.key.Key
import ru.cherryngine.engine.minecraft.entity.McEntity
import ru.cherryngine.engine.minecraft.entity.McEntityRegistry
import ru.cherryngine.impl.demo.view.CubeView
import ru.cherryngine.lib.math.Transform
import ru.cherryngine.lib.math.Vec3D
import ru.cherryngine.lib.math.YawPitch
import ru.cherryngine.lib.minecraft.entity.ItemDisplayMeta
import ru.cherryngine.lib.minecraft.item.ItemStack
import ru.cherryngine.lib.minecraft.registry.Registries
import ru.cherryngine.lib.minecraft.registry.keys.EntityTypes
import java.util.UUID
import kotlin.random.Random

class MinecraftCubeView(
    private val mcEntityRegistry: McEntityRegistry,
) : CubeView {
    private val modelId: UUID = UUID.randomUUID()

    init {
        mcEntityRegistry.getOrCreate(modelId) {
            McEntity(Random.nextInt(1000, 1_000_000), Registries.entityType[EntityTypes.ITEM_DISPLAY].value)
        }
    }

    override fun updatePosition(position: Vec3D, yawPitch: YawPitch) {
        mcEntityRegistry.get(modelId)?.teleport(position, yawPitch)
    }

    override fun updateMaterial(material: Key) {
        val mcEntity = mcEntityRegistry.get(modelId) ?: return
        mcEntity.metadata[ItemDisplayMeta.DISPLAYED_ITEM] = ItemStack(Registries.item[material].value)
    }

    override fun updateTransform(transform: Transform) {
        val mcEntity = mcEntityRegistry.get(modelId) ?: return
        mcEntity.metadata[ItemDisplayMeta.HAS_NO_GRAVITY] = true
        mcEntity.metadata[ItemDisplayMeta.TRANSLATION] = transform.translation
        mcEntity.metadata[ItemDisplayMeta.ROTATION_LEFT] = transform.rotation
        mcEntity.metadata[ItemDisplayMeta.SCALE] = transform.scale
        mcEntity.resendMeta()
    }

    override fun setViewContextIDs(contextIDs: Set<String>) {
        mcEntityRegistry.get(modelId)?.viewContextIDs = contextIDs
    }

    override fun destroy() {
        mcEntityRegistry.remove(modelId)
    }
}
