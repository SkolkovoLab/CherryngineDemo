package ru.cherryngine.impl.demo.systems

import com.github.quillraven.fleks.IteratingSystem
import com.github.quillraven.fleks.World.Companion.family
import ru.cherryngine.engine.core.instance.Instance
import ru.cherryngine.engine.ecs.EcsEntity
import ru.cherryngine.engine.ecs.components.PositionComponent
import ru.cherryngine.engine.physics.PhysicsSpace
import ru.cherryngine.impl.demo.EcsSystemConfig
import ru.cherryngine.impl.demo.components.CarComponent
import ru.cherryngine.impl.demo.components.CubeModelComponent
import ru.cherryngine.lib.math.Transform
import ru.cherryngine.lib.math.Vec3D

/**
 * После симуляции копирует chassis-transform машины в PositionComponent +
 * CubeModelComponent.transform — visual чассис ездит вместе с body.
 */
class CarPhysicsSyncSystem(
    private val physicsSpace: PhysicsSpace,
) : IteratingSystem(
    family { all(CarComponent, CubeModelComponent) }
) {
    override fun onTickEntity(entity: EcsEntity) {
        val car = entity[CarComponent]
        val vehicle = physicsSpace.getVehicleBody(car.carPhysicsId) ?: return
        val transform = vehicle.getTransform()

        entity.configure {
            it.getOrNull(PositionComponent)?.position = transform.translation
            it.getOrNull(CubeModelComponent)?.let { model ->
                model.transform = Transform(
                    translation = Vec3D.ZERO,
                    rotation = transform.rotation,
                    scale = model.transform.scale,
                )
            }
        }
    }

    object Config : EcsSystemConfig {
        override fun create(instance: Instance) = CarPhysicsSyncSystem(
            physicsSpace = instance.get(),
        )
    }
}
