package ru.cherryngine.impl.demo.components

import com.github.quillraven.fleks.ComponentType
import ru.cherryngine.engine.ecs.EcsComponent
import java.util.UUID

/**
 * Маркер на player-entity: игрок сидит в машине с физкорпусом [carPhysicsId].
 * CarDriveSystem каждый тик читает WASD-инпут и пушит body, плюс
 * телепортирует игрока на корпус. Sneak — снимает компонент.
 */
data class RidingCarComponent(
    val carPhysicsId: UUID,
) : EcsComponent<RidingCarComponent> {
    override fun type() = RidingCarComponent

    companion object : ComponentType<RidingCarComponent>()
}
