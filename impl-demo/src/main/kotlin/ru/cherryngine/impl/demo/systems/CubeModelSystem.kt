package ru.cherryngine.impl.demo.systems

import com.github.quillraven.fleks.IteratingSystem
import com.github.quillraven.fleks.World.Companion.family
import ru.cherryngine.engine.ecs.EcsEntity
import ru.cherryngine.engine.ecs.components.PositionComponent
import ru.cherryngine.engine.ecs.components.ViewableComponent
import ru.cherryngine.impl.demo.components.CubeModelComponent
import ru.cherryngine.impl.demo.view.CubeView
import ru.cherryngine.impl.demo.view.CubeViewFactory
import java.util.*

class CubeModelSystem(
    private val viewFactory: CubeViewFactory,
) : IteratingSystem(
    family { all(CubeModelComponent) }
) {
    private val views = HashMap<UUID, CubeView>()

    override fun onTick() {
        val activeIds = mutableSetOf<UUID>()
        family.forEach { activeIds.add(it[CubeModelComponent].modelId) }
        views.keys.removeIf { uuid ->
            if (uuid !in activeIds) { views[uuid]?.destroy(); true } else false
        }
        super.onTick()
    }

    override fun onTickEntity(entity: EcsEntity) {
        val component = entity[CubeModelComponent]
        val view = views.getOrPut(component.modelId) { viewFactory.create() }

        view.updateMaterial(component.material)
        view.updateTransform(component.transform)
        entity.getOrNull(PositionComponent)?.also {
            view.updatePosition(it.position, it.yawPitch)
        }
        entity.getOrNull(ViewableComponent)?.also {
            view.setViewContextIDs(it.viewContextIDs)
        }
    }
}
