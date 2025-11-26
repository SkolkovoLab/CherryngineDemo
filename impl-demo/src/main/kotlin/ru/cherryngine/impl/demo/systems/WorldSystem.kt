package ru.cherryngine.impl.demo.systems

import com.github.quillraven.fleks.IteratingSystem
import com.github.quillraven.fleks.World.Companion.family
import ru.cherryngine.engine.ecs.EcsEntity
import ru.cherryngine.engine.ecs.events.ViewableProvidersEvent
import ru.cherryngine.impl.demo.DemoWorlds
import ru.cherryngine.impl.demo.components.WorldComponent

class WorldSystem(
    val demoWorlds: DemoWorlds,
) : IteratingSystem(
    family { all(WorldComponent) }
) {
    override fun onTickEntity(entity: EcsEntity) {
        val worldComponent = entity[WorldComponent]
        val worldName = worldComponent.worldName
        val world = demoWorlds.worlds[worldName] ?: return
        entity.configure {
            val event = it.getOrAdd(ViewableProvidersEvent, ::ViewableProvidersEvent)
            event.staticViewableProviders += world
        }
    }
}