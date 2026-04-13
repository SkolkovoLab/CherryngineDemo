package ru.cherryngine.impl.demo.minecraft.renderer

import net.kyori.adventure.key.Key
import ru.cherryngine.engine.minecraft.entity.McEntity
import ru.cherryngine.engine.minecraft.entity.McEntityRegistry
import ru.cherryngine.impl.demo.renderer.CubeRenderer
import ru.cherryngine.lib.math.Transform
import ru.cherryngine.lib.math.Vec3D
import ru.cherryngine.lib.math.YawPitch
import ru.cherryngine.lib.minecraft.entity.ItemDisplayMeta
import ru.cherryngine.lib.minecraft.item.ItemStack
import ru.cherryngine.lib.minecraft.registry.Registries
import ru.cherryngine.lib.minecraft.registry.keys.EntityTypes
import java.util.*
import kotlin.random.Random

class MinecraftCubeRenderer(
    private val mcEntityRegistry: McEntityRegistry,
) : CubeRenderer {
    private val entities = HashMap<UUID, McEntity>()

    override fun onAdd(id: UUID) {
        val mcEntity = McEntity(
            Random.nextInt(1000, 1_000_000),
            Registries.entityType[EntityTypes.ITEM_DISPLAY].value
        )
        mcEntity.metadata[ItemDisplayMeta.HAS_NO_GRAVITY] = true
        mcEntity.metadata[ItemDisplayMeta.TRANSFORMATION_INTERPOLATION_DURATION] = 1
        mcEntity.metadata[ItemDisplayMeta.POSITION_ROTATION_INTERPOLATION_DURATION] = 1
        entities[id] = mcEntity
        mcEntityRegistry.add(mcEntity)
    }

    override fun onRemove(id: UUID) {
        val mcEntity = entities.remove(id) ?: return
        mcEntityRegistry.remove(mcEntity)
    }

    override fun update(
        id: UUID,
        position: Vec3D,
        yawPitch: YawPitch,
        material: Key,
        transform: Transform,
        viewContextIDs: Set<String>,
    ) {
        val mcEntity = entities[id] ?: return
        mcEntity.teleport(position, yawPitch)
        mcEntity.viewContextIDs = viewContextIDs
        mcEntity.metadata[ItemDisplayMeta.DISPLAYED_ITEM] = ItemStack(Registries.item[material].value)
        mcEntity.metadata[ItemDisplayMeta.TRANSLATION] = transform.translation
        mcEntity.metadata[ItemDisplayMeta.ROTATION_LEFT] = transform.rotation
        mcEntity.metadata[ItemDisplayMeta.SCALE] = transform.scale
        mcEntity.metadata[ItemDisplayMeta.INTERPOLATION_DELAY] = 0
        mcEntity.resendMeta()
    }
}
