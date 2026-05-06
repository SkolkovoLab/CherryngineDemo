package ru.cherryngine.impl.demo.components

import com.github.quillraven.fleks.ComponentType
import ru.cherryngine.engine.ecs.EcsComponent
import java.util.UUID

/**
 * Маркер визуального колеса. Само колесо — собственность Jolt VehicleConstraint
 * у машины [carPhysicsId], отдельного physics-body не имеет. CarWheelSyncSystem
 * каждый тик копирует transform из vehicleBody.getWheelTransform([wheelIndex])
 * в PositionComponent + CubeModelComponent.transform.
 */
data class WheelComponent(
    val carPhysicsId: UUID,
    val wheelIndex: Int,
) : EcsComponent<WheelComponent> {
    override fun type() = WheelComponent

    companion object : ComponentType<WheelComponent>()
}
