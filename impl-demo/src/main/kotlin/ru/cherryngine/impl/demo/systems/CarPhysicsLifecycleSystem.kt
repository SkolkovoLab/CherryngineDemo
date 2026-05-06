package ru.cherryngine.impl.demo.systems

import com.github.quillraven.fleks.IteratingSystem
import com.github.quillraven.fleks.World.Companion.family
import ru.cherryngine.engine.core.instance.Instance
import ru.cherryngine.engine.core.shape.ShapeGeometry
import ru.cherryngine.engine.core.shape.ShapeWorld
import ru.cherryngine.engine.ecs.EcsEntity
import ru.cherryngine.engine.ecs.components.PositionComponent
import ru.cherryngine.engine.physics.PhysicsSpace
import ru.cherryngine.impl.demo.EcsSystemConfig
import ru.cherryngine.impl.demo.components.CarComponent
import ru.cherryngine.impl.demo.components.ShapeRegistrationComponent
import ru.cherryngine.impl.demo.shape.PhysicsCubeShape
import ru.cherryngine.lib.math.Transform
import ru.cherryngine.lib.math.Vec3D

/**
 * Lifecycle для машин: keepAlive + ленивое создание VehicleBody через
 * PhysicsSpace.addCar. При первом тике entity регистрирует чассис как
 * PhysicsCubeShape (для raycast'а REMOVE/INTERACT) — Jolt внутри держит
 * 4 колеса в VehicleConstraint.
 */
class CarPhysicsLifecycleSystem(
    private val physicsSpace: PhysicsSpace,
    private val shapeWorld: ShapeWorld,
) : IteratingSystem(
    family { all(CarComponent) }
) {
    override fun onTickEntity(entity: EcsEntity) {
        val car = entity[CarComponent]
        val pos = entity.getOrNull(PositionComponent)?.position ?: Vec3D.ZERO

        physicsSpace.keepAlive(car.carPhysicsId)
        physicsSpace.getOrCreateVehicleBody(car.carPhysicsId, car.physContextIDs) {
            physicsSpace.addCar(
                position = pos,
                chassisSize = car.chassisSize,
            )
        }

        if (entity.getOrNull(ShapeRegistrationComponent) == null) {
            val chassisShape = PhysicsCubeShape(
                geometry = ShapeGeometry.Box(car.chassisSize / 2.0),
                getTransform = {
                    physicsSpace.getBodyTransform(car.carPhysicsId) ?: Transform.ZERO
                },
                physicsId = car.carPhysicsId,
                entityId = car.carPhysicsId,
            )
            val registration = shapeWorld.registerGroup(listOf(chassisShape))
            entity.configure { it += ShapeRegistrationComponent(registration) }
        }
    }

    object Config : EcsSystemConfig {
        override fun create(instance: Instance) = CarPhysicsLifecycleSystem(
            physicsSpace = instance.get(),
            shapeWorld = instance.get(),
        )
    }
}
