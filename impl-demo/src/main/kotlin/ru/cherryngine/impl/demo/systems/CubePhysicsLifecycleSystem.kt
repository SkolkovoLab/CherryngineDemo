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
import ru.cherryngine.impl.demo.components.PhysicsComponent
import ru.cherryngine.impl.demo.components.ShapeRegistrationComponent
import ru.cherryngine.impl.demo.shape.PhysicsCubeShape
import ru.cherryngine.lib.math.Transform
import ru.cherryngine.lib.math.Vec3D

/**
 * Поддерживает lifecycle Jolt-тел для кубов: keepAlive каждый тик,
 * ленивое создание через factory при первом встрече.
 * Игрок-хитбоксы создаются отдельно платформенным driver'ом — сюда не попадают.
 *
 * Также при первом тике entity регистрирует физический шейп в ShapeWorld
 * (Box(half = comp.size/2), getTransform читает текущий transform jolt-тела).
 * Регистрация хранится в ShapeRegistrationComponent — Component.onRemove
 * закрывает её автоматически при удалении entity.
 */
class CubePhysicsLifecycleSystem(
    private val physicsSpace: PhysicsSpace,
    private val shapeWorld: ShapeWorld,
) : IteratingSystem(
    family { all(PhysicsComponent) }
) {
    override fun onTickEntity(entity: EcsEntity) {
        val comp = entity[PhysicsComponent]
        val pos = entity.getOrNull(PositionComponent)?.position ?: Vec3D.ZERO
        physicsSpace.keepAlive(comp.physicsId)
        physicsSpace.getOrCreateBody(comp.physicsId, comp.physContextIDs) {
            physicsSpace.addCube(pos, comp.size)
        }

        if (entity.getOrNull(ShapeRegistrationComponent) == null) {
            val cubeShape = PhysicsCubeShape(
                geometry = ShapeGeometry.Box(comp.size / 2.0),
                getTransform = {
                    physicsSpace.getBodyTransform(comp.physicsId) ?: Transform.ZERO
                },
                physicsId = comp.physicsId,
                entityId = comp.physicsId,  // используем physicsId как entityId
            )
            val registration = shapeWorld.registerGroup(listOf(cubeShape))
            entity.configure { it += ShapeRegistrationComponent(registration) }
        }
    }

    object Config : EcsSystemConfig {
        override fun create(instance: Instance) = CubePhysicsLifecycleSystem(
            physicsSpace = instance.get(),
            shapeWorld = instance.get(),
        )
    }
}
