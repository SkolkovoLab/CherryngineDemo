package ru.cherryngine.impl.demo.systems

import com.github.quillraven.fleks.IteratingSystem
import com.github.quillraven.fleks.World.Companion.family
import ru.cherryngine.engine.minecraft.entity.McEntity
import ru.cherryngine.engine.minecraft.entity.McEntityRegistry
import ru.cherryngine.engine.minecraft.view.ViewableProvider
import ru.cherryngine.engine.ecs.EcsEntity
import ru.cherryngine.engine.ecs.components.PositionComponent
import ru.cherryngine.engine.ecs.events.ViewableProvidersEvent
import ru.cherryngine.impl.demo.components.CubeModelComponent
import ru.cherryngine.lib.minecraft.entity.ItemDisplayMeta
import ru.cherryngine.lib.minecraft.item.ItemStack
import ru.cherryngine.lib.minecraft.registry.Registries
import ru.cherryngine.lib.minecraft.registry.keys.EntityTypes
import kotlin.random.Random

class CubeModelSystem(
    private val mcEntityRegistry: McEntityRegistry,
) : IteratingSystem(
    family { all(CubeModelComponent) }
) {
    override fun onTickEntity(entity: EcsEntity) {
        val component = entity[CubeModelComponent]
        val transform = component.transform

        mcEntityRegistry.keepAlive(component.mcEntityId)
        val mcEntity = mcEntityRegistry.getOrCreate(component.mcEntityId) {
            McEntity(Random.nextInt(1000, 1_000_000), Registries.entityType[EntityTypes.ITEM_DISPLAY].value)
        }

        with(mcEntity) {
            metadata[ItemDisplayMeta.DISPLAYED_ITEM] = ItemStack(Registries.item[component.material].value)
            metadata[ItemDisplayMeta.HAS_NO_GRAVITY] = true
            metadata[ItemDisplayMeta.TRANSLATION] = transform.translation
            metadata[ItemDisplayMeta.ROTATION_LEFT] = transform.rotation
            metadata[ItemDisplayMeta.SCALE] = transform.scale
            resendMeta()
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
