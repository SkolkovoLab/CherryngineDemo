package ru.cherryngine.impl.demo.components

import com.github.quillraven.fleks.ComponentType
import ru.cherryngine.engine.ecs.EcsComponent
import ru.cherryngine.lib.math.Vec3D
import java.util.UUID

/**
 * ECS-маркер машины. Физика — через Jolt VehicleConstraint в PhysicsSpace
 * (отдельный карта вне обычных PhysicsBody'ев). [carPhysicsId] — ключ для
 * lookup'а VehicleBody. [chassisSize] нужен для рендера + регистрации шейпа.
 */
data class CarComponent(
    val carPhysicsId: UUID = UUID.randomUUID(),
    val physContextIDs: Set<String> = emptySet(),
    val chassisSize: Vec3D,
) : EcsComponent<CarComponent> {
    override fun type() = CarComponent

    companion object : ComponentType<CarComponent>()
}
