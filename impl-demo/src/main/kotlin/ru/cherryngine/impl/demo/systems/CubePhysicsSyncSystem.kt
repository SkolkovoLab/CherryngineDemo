package ru.cherryngine.impl.demo.systems

import com.github.quillraven.fleks.IteratingSystem
import com.github.quillraven.fleks.World.Companion.family
import ru.cherryngine.engine.core.instance.Instance
import ru.cherryngine.engine.ecs.EcsEntity
import ru.cherryngine.engine.ecs.components.PositionComponent
import ru.cherryngine.engine.physics.PhysicsSpace
import ru.cherryngine.impl.demo.EcsSystemConfig
import ru.cherryngine.impl.demo.components.CubeModelComponent
import ru.cherryngine.impl.demo.components.PhysicsComponent
import ru.cherryngine.lib.math.Transform
import ru.cherryngine.lib.math.Vec3D

/**
 * После симуляции копирует transform тела в PositionComponent + CubeModelComponent.transform.
 * PositionComponent хранит translation, CubeModelComponent.transform — ротацию и масштаб.
 */
class CubePhysicsSyncSystem(
    private val physicsSpace: PhysicsSpace,
) : IteratingSystem(
    family { all(PhysicsComponent, CubeModelComponent) }
) {
    override fun onTickEntity(entity: EcsEntity) {
        val comp = entity[PhysicsComponent]
        val body = physicsSpace.getOrCreateBody(comp.physicsId, comp.physContextIDs) {
            physicsSpace.addCube(Vec3D.ZERO, comp.size)
        }
        entity.configure {
            val bodyTransform = body.getTransform()
            it.getOrNull(PositionComponent)?.position = bodyTransform.translation
            // Сохраняем scale из CubeModelComponent (задаётся при спавне и не меняется),
            // чтобы визуал slab'а оставался плоским. Body переносит только translation+rotation.
            it.getOrNull(CubeModelComponent)?.let { model ->
                model.transform = Transform(
                    translation = Vec3D.ZERO,
                    rotation = bodyTransform.rotation,
                    scale = model.transform.scale,
                )
            }
        }
    }

    object Config : EcsSystemConfig {
        override fun create(instance: Instance) = CubePhysicsSyncSystem(instance.get())
    }
}
