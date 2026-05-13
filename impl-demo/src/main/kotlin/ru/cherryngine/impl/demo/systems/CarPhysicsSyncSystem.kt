package ru.cherryngine.impl.demo.systems

import com.github.quillraven.fleks.IteratingSystem
import com.github.quillraven.fleks.World.Companion.family
import ru.cherryngine.engine.core.instance.Instance
import ru.cherryngine.engine.ecs.EcsEntity
import ru.cherryngine.engine.ecs.components.PositionComponent
import ru.cherryngine.engine.physics.PhysicsSpace
import ru.cherryngine.impl.demo.EcsSystemConfig
import ru.cherryngine.impl.demo.components.CarComponent

/**
 * После симуляции копирует chassis-translation в PositionComponent машины.
 * Используется view-culling'ом (рендерер машины + камера третьего лица читают
 * это поле) и shape-raycast'ом (PhysicsCubeShape по chassisTransform). Сам
 * визуальный рендер машины (chassis + колёса) уже идёт через CarRenderer и
 * читает трансформы напрямую из VehicleBody.
 */
class CarPhysicsSyncSystem(
    private val physicsSpace: PhysicsSpace,
) : IteratingSystem(
    family { all(CarComponent, PositionComponent) }
) {
    override fun onTickEntity(entity: EcsEntity) {
        val car = entity[CarComponent]
        val vehicle = physicsSpace.getVehicleBody(car.carPhysicsId) ?: return
        entity[PositionComponent].position = vehicle.getTransform().translation
    }

    object Config : EcsSystemConfig {
        override fun create(instance: Instance) = CarPhysicsSyncSystem(
            physicsSpace = instance.get(),
        )
    }
}
