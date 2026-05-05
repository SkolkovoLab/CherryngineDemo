package ru.cherryngine.impl.demo.components

import com.github.quillraven.fleks.ComponentType
import ru.cherryngine.engine.ecs.EcsComponent
import java.util.UUID

/**
 * Маркер на player-entity: сейчас держим физкуб с physicsId = [grabbedPhysicsId]
 * на расстоянии [distance] от глаза. GrabSystem каждый тик подталкивает body
 * к точке `eye + dir * distance`. Колесо мыши, пока компонент висит, меняет
 * `distance` вместо переключения инструмента.
 */
data class GrabbingComponent(
    val grabbedPhysicsId: UUID,
    var distance: Double,
) : EcsComponent<GrabbingComponent> {
    override fun type() = GrabbingComponent

    companion object : ComponentType<GrabbingComponent>()
}
