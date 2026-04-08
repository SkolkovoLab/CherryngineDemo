package ru.cherryngine.impl.demo.systems

import com.github.quillraven.fleks.IteratingSystem
import com.github.quillraven.fleks.World.Companion.family
import ru.cherryngine.engine.ecs.EcsEntity
import ru.cherryngine.engine.ecs.events.ViewableProvidersEvent
import ru.cherryngine.impl.demo.DemoWorlds
import ru.cherryngine.impl.demo.components.WorldComponent
import ru.cherryngine.lib.world.LayerEntry

class WorldSystem(
    val demoWorlds: DemoWorlds,
) : IteratingSystem(
    family { all(WorldComponent) }
) {
    override fun onTickEntity(entity: EcsEntity) {
        val worldName = entity[WorldComponent].worldName
        val layer = demoWorlds.layers[worldName] ?: return
        entity.configure {
            val event = it.getOrAdd(ViewableProvidersEvent, ::ViewableProvidersEvent)
            event.layers += LayerEntry(layer, 0)
            if (event.dimensionType == null) event.dimensionType = demoWorlds.overworld
        }
    }
}
