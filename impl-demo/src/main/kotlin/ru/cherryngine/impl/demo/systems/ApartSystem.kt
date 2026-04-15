package ru.cherryngine.impl.demo.systems

import com.github.quillraven.fleks.IteratingSystem
import com.github.quillraven.fleks.World.Companion.family
import ru.cherryngine.engine.ecs.EcsEntity
import ru.cherryngine.impl.demo.EcsSystemConfig
import ru.cherryngine.impl.demo.InstanceScope
import ru.cherryngine.engine.ecs.components.PlayerComponent
import ru.cherryngine.engine.ecs.components.PositionComponent
import ru.cherryngine.engine.ecs.components.ViewableComponent
import ru.cherryngine.impl.demo.components.ApartComponent
import ru.cherryngine.lib.math.Cuboid
import ru.cherryngine.lib.math.Vec3D

class ApartSystem : IteratingSystem(
    family { all(PlayerComponent, PositionComponent, ApartComponent) }
) {
    val enterCuboid = Cuboid(Vec3D(274.0, 56.0, 172.0), Vec3D(276.0, 58.0, 173.0))
    val exitCuboid = Cuboid(Vec3D(274.0, 60.0, 177.0), Vec3D(276.0, 62.0, 178.0))
    val enterPosition = Vec3D(275.0, 60.0, 176.5)
    val exitPosition = Vec3D(275.0, 56.0, 173.5)

    override fun onTickEntity(entity: EcsEntity) {
        val playerComponent = entity[PlayerComponent]
        val positionComponent = entity[PositionComponent]

        if (enterCuboid.isInside(positionComponent.position)) {
            val apartName = entity.getOrNull(ApartComponent)?.apartName
            if (apartName != null) {
                positionComponent.position = enterPosition
                playerComponent.viewContextIDs = setOf("gm_construct", apartName)
                entity.getOrNull(ViewableComponent)?.viewContextIDs = setOf(apartName)
            }
        }

        if (exitCuboid.isInside(positionComponent.position)) {
            positionComponent.position = exitPosition

            playerComponent.viewContextIDs = setOf("gm_construct")
            entity.getOrNull(ViewableComponent)?.viewContextIDs = setOf("gm_construct")
        }
    }

    object Config : EcsSystemConfig {
        override fun create(scope: InstanceScope) = ApartSystem()
    }
}