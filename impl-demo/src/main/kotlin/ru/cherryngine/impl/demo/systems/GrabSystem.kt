package ru.cherryngine.impl.demo.systems

import com.github.quillraven.fleks.IteratingSystem
import com.github.quillraven.fleks.World.Companion.family
import ru.cherryngine.engine.core.instance.Instance
import ru.cherryngine.engine.ecs.EcsEntity
import ru.cherryngine.engine.ecs.components.PositionComponent
import ru.cherryngine.engine.physics.PhysicsSpace
import ru.cherryngine.impl.demo.EcsSystemConfig
import ru.cherryngine.impl.demo.components.GrabbingComponent
import ru.cherryngine.impl.demo.components.findPhysicsEntity
import ru.cherryngine.lib.math.Vec3D

private const val EYE_HEIGHT = 1.62

/**
 * Каждый тик для каждого игрока с GrabbingComponent: считает целевую точку
 * `eye + dir * grab.distance` и пушит туда схваченный physics-объект (куб ИЛИ
 * машину) через physicsSpace.setLinearVelocity((target - currentPos) / delta).
 * Идёт ПЕРЕД PhysicsSimulationSystem чтобы velocity успела примениться в этом же тике.
 *
 * Если схваченный entity больше не существует (удалили REMOVE-инструментом
 * например) — GrabbingComponent снимается с игрока.
 */
class GrabSystem(
    private val physicsSpace: PhysicsSpace,
) : IteratingSystem(
    family { all(GrabbingComponent, PositionComponent) }
) {
    override fun onTickEntity(entity: EcsEntity) {
        val grab = entity[GrabbingComponent]
        val playerPos = entity[PositionComponent]

        val grabbed = world.findPhysicsEntity(grab.grabbedPhysicsId)
        if (grabbed == null) {
            entity.configure { it -= GrabbingComponent }
            return
        }

        val current = physicsSpace.getBodyTransform(grab.grabbedPhysicsId)?.translation
        if (current == null) {
            entity.configure { it -= GrabbingComponent }
            return
        }

        val eye = playerPos.position + Vec3D(0.0, EYE_HEIGHT, 0.0)
        val dir = playerPos.yawPitch.direction()
        val target = eye + dir * grab.distance

        val deltaSec = deltaTime.toDouble()
        if (deltaSec > 0.0) {
            physicsSpace.setLinearVelocity(grab.grabbedPhysicsId, (target - current) / deltaSec)
        }
        physicsSpace.setAngularVelocity(grab.grabbedPhysicsId, Vec3D.ZERO)
    }

    object Config : EcsSystemConfig {
        override fun create(instance: Instance) = GrabSystem(
            physicsSpace = instance.get(),
        )
    }
}
