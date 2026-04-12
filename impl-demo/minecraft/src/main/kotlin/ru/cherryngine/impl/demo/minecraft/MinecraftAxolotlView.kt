package ru.cherryngine.impl.demo.minecraft

import net.kyori.adventure.text.Component
import ru.cherryngine.engine.core.player.PlayerManager
import ru.cherryngine.engine.minecraft.entity.McEntity
import ru.cherryngine.engine.minecraft.entity.McEntityRegistry
import ru.cherryngine.engine.minecraft.player.MinecraftPlayer
import ru.cherryngine.impl.demo.view.AxolotlView
import ru.cherryngine.lib.math.Vec3D
import ru.cherryngine.lib.math.YawPitch
import ru.cherryngine.lib.minecraft.entity.AxolotlMeta
import ru.cherryngine.lib.minecraft.registry.Registries
import ru.cherryngine.lib.minecraft.registry.keys.EntityTypes
import java.util.*
import kotlin.random.Random

class MinecraftAxolotlView(
    private val mcEntityRegistry: McEntityRegistry,
    private val playerManager: PlayerManager,
) : AxolotlView {
    private val modelId: UUID = UUID.randomUUID()

    init {
        mcEntityRegistry.getOrCreate(modelId) {
            McEntity(Random.nextInt(1000, 1_000_000), Registries.entityType[EntityTypes.AXOLOTL].value).apply {
                metadata[AxolotlMeta.HAS_NO_GRAVITY] = true
                metadata[AxolotlMeta.VARIANT] = AxolotlMeta.Variant.entries.random()
                metadata[AxolotlMeta.CUSTOM_NAME_VISIBLE] = true
            }
        }
    }

    override fun updatePosition(position: Vec3D, yawPitch: YawPitch) {
        mcEntityRegistry.get(modelId)?.teleport(position, yawPitch)
    }

    override fun setName(name: String?) {
        val mcEntity = mcEntityRegistry.get(modelId) ?: return
        if (name != null) {
            mcEntity.metadata[AxolotlMeta.CUSTOM_NAME] = Component.text(name)
        }
        mcEntity.resendMeta()
    }

    override fun setHiddenFromPlayer(uuid: UUID?) {
        val mcEntity = mcEntityRegistry.get(modelId) ?: return
        if (uuid != null) {
            mcEntity.viewerPredicate = { (it as? MinecraftPlayer)?.uuid != uuid }
        } else {
            mcEntity.viewerPredicate = { true }
        }
    }

    override fun setViewContextIDs(contextIDs: Set<String>) {
        mcEntityRegistry.get(modelId)?.viewContextIDs = contextIDs
    }

    override fun destroy() {
        mcEntityRegistry.remove(modelId)
    }
}
