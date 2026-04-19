package ru.cherryngine.impl.demo.minecraft.renderer

import net.kyori.adventure.text.Component
import ru.cherryngine.engine.core.instance.InstanceSingleton
import ru.cherryngine.engine.core.player.PlayerManager
import ru.cherryngine.engine.minecraft.entity.McEntity
import ru.cherryngine.engine.minecraft.entity.McEntityRegistry
import ru.cherryngine.engine.minecraft.player.MinecraftPlayer
import ru.cherryngine.impl.demo.renderer.AxolotlRenderer
import ru.cherryngine.lib.math.Vec3D
import ru.cherryngine.lib.math.YawPitch
import ru.cherryngine.lib.minecraft.entity.AxolotlMeta
import ru.cherryngine.lib.minecraft.registry.Registries
import ru.cherryngine.lib.minecraft.registry.keys.EntityTypes
import java.util.*
import kotlin.random.Random

@InstanceSingleton(platform = "minecraft")
class MinecraftAxolotlRenderer(
    private val mcEntityRegistry: McEntityRegistry,
    private val playerManager: PlayerManager,
) : AxolotlRenderer {
    private val entities = HashMap<UUID, McEntity>()

    override fun onAdd(id: UUID) {
        val mcEntity = McEntity(
            Random.nextInt(1000, 1_000_000),
            Registries.entityType[EntityTypes.AXOLOTL].value
        ).apply {
            metadata[AxolotlMeta.HAS_NO_GRAVITY] = true
            metadata[AxolotlMeta.VARIANT] = AxolotlMeta.Variant.entries.random()
            metadata[AxolotlMeta.CUSTOM_NAME_VISIBLE] = true
        }
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
        name: String?,
        hiddenFromPlayer: UUID?,
        viewContextIDs: Set<String>,
    ) {
        val mcEntity = entities[id] ?: return
        mcEntity.teleport(position, yawPitch)
        mcEntity.viewContextIDs = viewContextIDs
        if (hiddenFromPlayer != null) {
            mcEntity.viewerPredicate = { (it as? MinecraftPlayer)?.uuid != hiddenFromPlayer }
        } else {
            mcEntity.viewerPredicate = { true }
        }
        if (name != null) {
            mcEntity.metadata[AxolotlMeta.CUSTOM_NAME] = Component.text(name)
        }
        mcEntity.resendMeta()
    }
}
