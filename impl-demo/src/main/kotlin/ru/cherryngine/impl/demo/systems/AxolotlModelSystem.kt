package ru.cherryngine.impl.demo.systems

import com.github.quillraven.fleks.IteratingSystem
import com.github.quillraven.fleks.World.Companion.family
import net.kyori.adventure.text.Component
import ru.cherryngine.engine.core.entity.McEntity
import ru.cherryngine.engine.ecs.systems.mc_entity.McEntityRegistry
import ru.cherryngine.engine.core.player.PlayerManager
import ru.cherryngine.engine.core.view.ViewableProvider
import ru.cherryngine.engine.ecs.EcsEntity
import ru.cherryngine.engine.ecs.components.PlayerComponent
import ru.cherryngine.engine.ecs.components.PositionComponent
import ru.cherryngine.engine.ecs.events.ViewableProvidersEvent
import ru.cherryngine.impl.demo.components.AxolotlModelComponent
import ru.cherryngine.lib.minecraft.entity.AxolotlMeta
import ru.cherryngine.lib.minecraft.registry.Registries
import ru.cherryngine.lib.minecraft.registry.keys.EntityTypes
import kotlin.random.Random

class AxolotlModelSystem(
    val playerManager: PlayerManager,
    private val mcEntityRegistry: McEntityRegistry,
) : IteratingSystem(
    family { all(AxolotlModelComponent) }
) {
    override fun onTickEntity(entity: EcsEntity) {
        val component = entity[AxolotlModelComponent]
        val playerComponent = entity.getOrNull(PlayerComponent)
        val name = playerComponent?.uuid?.let { playerManager.getPlayerNullable(it) }?.connection?.gameProfile?.username

        mcEntityRegistry.keepAlive(component.mcEntityId)
        val mcEntity = mcEntityRegistry.getOrCreate(component.mcEntityId) {
            McEntity(Random.nextInt(1000, 1_000_000), Registries.entityType[EntityTypes.AXOLOTL].value).apply {
                metadata[AxolotlMeta.HAS_NO_GRAVITY] = true
                metadata[AxolotlMeta.VARIANT] = AxolotlMeta.Variant.entries.random()
                if (name != null) metadata[AxolotlMeta.CUSTOM_NAME] = Component.text(name)
                metadata[AxolotlMeta.CUSTOM_NAME_VISIBLE] = true
                if (playerComponent != null) {
                    viewerPredicate = { it != playerManager.getPlayerNullable(playerComponent.uuid) }
                }
            }
        }

        entity.getOrNull(PositionComponent)?.also { posComponent ->
            mcEntity.teleport(posComponent.position, posComponent.yawPitch)
        }

        val viewableProvider = ViewableProvider.Static(setOf(mcEntity))
        entity.configure {
            val event = it.getOrAdd(ViewableProvidersEvent, ::ViewableProvidersEvent)
            event.viewableProviders += viewableProvider
        }
    }
}
