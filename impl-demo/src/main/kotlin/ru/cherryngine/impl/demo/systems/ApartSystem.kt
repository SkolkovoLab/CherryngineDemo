package ru.cherryngine.impl.demo.systems

import com.github.quillraven.fleks.IteratingSystem
import com.github.quillraven.fleks.World.Companion.family
import ru.cherryngine.engine.ecs.EcsEntity
import ru.cherryngine.engine.ecs.components.PlayerComponent
import ru.cherryngine.engine.ecs.components.PositionComponent
import ru.cherryngine.engine.ecs.components.ViewableComponent
import ru.cherryngine.impl.demo.components.ApartComponent
import ru.cherryngine.lib.math.Cuboid
import ru.cherryngine.lib.math.Vec3D

class ApartSystem : IteratingSystem(
    family { all(PlayerComponent, PositionComponent, ApartComponent) }
) {
    val enterCuboid = Cuboid(Vec3D(-1.0, -1.0, 7.0), Vec3D(1.0, 3.0, 8.0))
    val exitCuboid = Cuboid(Vec3D(-1.0, -1.0, 8.0), Vec3D(1.0, 3.0, 9.0))
    val enterPosition = Vec3D(0.0, 0.0, 9.5)
    val exitPosition = Vec3D(0.0, 0.0, 6.5)

    override fun onTickEntity(entity: EcsEntity) {
        val playerComponent = entity[PlayerComponent]
        val positionComponent = entity[PositionComponent]

        if (enterCuboid.isInside(positionComponent.position)) {
            val apartName = entity.getOrNull(ApartComponent)?.apartName
            if (apartName != null) {
                positionComponent.position = enterPosition
                playerComponent.viewContextIDs = setOf("street", apartName)
                entity.getOrNull(ViewableComponent)?.viewContextIDs = setOf(apartName)
            }
        }

        if (exitCuboid.isInside(positionComponent.position)) {
            positionComponent.position = exitPosition

            playerComponent.viewContextIDs = setOf("street")
            entity.getOrNull(ViewableComponent)?.viewContextIDs = setOf("street")
        }
    }
}