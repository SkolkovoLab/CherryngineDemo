package ru.cherryngine.impl.demo.components

import com.github.quillraven.fleks.ComponentType
import ru.cherryngine.engine.ecs.EcsComponent
import ru.cherryngine.engine.physics.CarSettings
import java.util.UUID

/**
 * ECS-маркер машины. Физика — через Jolt VehicleConstraint в PhysicsSpace
 * (отдельный карта вне обычных PhysicsBody'ев). [carPhysicsId] — ключ для
 * lookup'а VehicleBody.
 *
 * Всё, что определяет «какая это машина» (размеры, mass, engine torque,
 * suspension, anti-roll и пр.) — в [settings]. Этот же CarSettings отдаётся
 * в [ru.cherryngine.engine.physics.PhysicsSpace.addCar] для создания
 * VehicleBody, и читается render-стороной для генерации chassis+wheels McEntity.
 */
data class CarComponent(
    val settings: CarSettings,
    val carPhysicsId: UUID = UUID.randomUUID(),
    val physContextIDs: Set<String> = emptySet(),
) : EcsComponent<CarComponent> {
    override fun type() = CarComponent

    companion object : ComponentType<CarComponent>()
}
