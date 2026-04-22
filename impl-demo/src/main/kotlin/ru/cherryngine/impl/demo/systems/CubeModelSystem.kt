package ru.cherryngine.impl.demo.systems

import com.github.quillraven.fleks.IteratingSystem
import com.github.quillraven.fleks.World.Companion.family
import ru.cherryngine.engine.core.instance.Instance
import ru.cherryngine.engine.ecs.EcsEntity
import ru.cherryngine.engine.ecs.components.PositionComponent
import ru.cherryngine.engine.ecs.components.ViewableComponent
import ru.cherryngine.impl.demo.EcsSystemConfig
import ru.cherryngine.impl.demo.components.CubeModelComponent
import ru.cherryngine.impl.demo.renderer.CubeRendererDispatcher
import ru.cherryngine.lib.math.Vec3D
import ru.cherryngine.lib.math.YawPitch
import java.util.*

class CubeModelSystem(
    private val dispatcher: CubeRendererDispatcher,
) : IteratingSystem(
    family { all(CubeModelComponent) }
) {
    private val activeIds = HashSet<UUID>()

    override fun onTick() {
        val currentIds = mutableSetOf<UUID>()
        family.forEach { currentIds.add(it[CubeModelComponent].modelId) }

        activeIds.removeIf { id ->
            if (id !in currentIds) {
                dispatcher.onRemove(id)
                true
            } else false
        }

        currentIds.forEach { id ->
            if (activeIds.add(id)) {
                dispatcher.onAdd(id)
            }
        }

        super.onTick()
    }

    override fun onTickEntity(entity: EcsEntity) {
        val component = entity[CubeModelComponent]
        val pos = entity.getOrNull(PositionComponent)
        val viewContextIDs = entity.getOrNull(ViewableComponent)?.viewContextIDs ?: emptySet()

        dispatcher.update(
            component.modelId,
            pos?.position ?: Vec3D.ZERO,
            pos?.yawPitch ?: YawPitch.ZERO,
            component.material,
            component.transform,
            viewContextIDs,
        )
    }

    object Config : EcsSystemConfig {
        override fun create(instance: Instance) =
            CubeModelSystem(instance.get())
    }
}
