package ru.cherryngine.impl.demo.systems

import com.github.quillraven.fleks.IteratingSystem
import com.github.quillraven.fleks.World.Companion.family
import ru.cherryngine.engine.core.instance.Instance
import ru.cherryngine.engine.ecs.EcsEntity
import ru.cherryngine.engine.ecs.components.PositionComponent
import ru.cherryngine.engine.physics.PhysicsSpace
import ru.cherryngine.impl.demo.EcsSystemConfig
import ru.cherryngine.impl.demo.components.PhysicsComponent
import ru.cherryngine.lib.math.Vec3D

/**
 * Поддерживает lifecycle Jolt-тел для кубов: keepAlive каждый тик,
 * ленивое создание через factory при первом встрече.
 * Игрок-хитбоксы создаются отдельно платформенным driver'ом — сюда не попадают.
 */
class CubePhysicsLifecycleSystem(
    private val physicsSpace: PhysicsSpace,
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
    }

    object Config : EcsSystemConfig {
        override fun create(instance: Instance) = CubePhysicsLifecycleSystem(instance.get())
    }
}
