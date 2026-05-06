package ru.cherryngine.impl.demo.systems

import com.github.quillraven.fleks.IteratingSystem
import com.github.quillraven.fleks.World.Companion.family
import ru.cherryngine.engine.core.instance.Instance
import ru.cherryngine.engine.ecs.EcsEntity
import ru.cherryngine.engine.ecs.components.PositionComponent
import ru.cherryngine.engine.physics.PhysicsSpace
import ru.cherryngine.impl.demo.EcsSystemConfig
import ru.cherryngine.impl.demo.components.CubeModelComponent
import ru.cherryngine.impl.demo.components.WheelComponent
import ru.cherryngine.lib.math.Transform
import ru.cherryngine.lib.math.Vec3D

/**
 * Синхронизирует визуальные колёса с трансформами от Jolt VehicleConstraint.
 * Колёса как физические тела не существуют — Jolt держит их положение/ротацию
 * как часть constraint'а. Этот sync копирует [VehicleBody.getWheelTransform]
 * в PositionComponent + CubeModelComponent для рендера.
 *
 * Если машина уехала из ECS (entity удалили или не keepAlive'или) — wheel
 * entity самоуничтожается.
 */
class CarWheelSyncSystem(
    private val physicsSpace: PhysicsSpace,
) : IteratingSystem(
    family { all(WheelComponent, PositionComponent, CubeModelComponent) }
) {
    override fun onTickEntity(entity: EcsEntity) {
        val wheel = entity[WheelComponent]
        val vehicle = physicsSpace.getVehicleBody(wheel.carPhysicsId)
        if (vehicle == null) {
            entity.remove()
            return
        }

        val transform = vehicle.getWheelTransform(wheel.wheelIndex)
        entity.configure {
            it[PositionComponent].position = transform.translation
            val model = it[CubeModelComponent]
            model.transform = Transform(
                translation = Vec3D.ZERO,
                rotation = transform.rotation,
                scale = model.transform.scale,
            )
        }
    }

    object Config : EcsSystemConfig {
        override fun create(instance: Instance) = CarWheelSyncSystem(
            physicsSpace = instance.get(),
        )
    }
}
