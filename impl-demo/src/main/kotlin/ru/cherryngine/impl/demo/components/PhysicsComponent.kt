package ru.cherryngine.impl.demo.components

import com.github.quillraven.fleks.ComponentType
import ru.cherryngine.engine.ecs.EcsComponent
import ru.cherryngine.lib.math.Vec3D
import java.util.*

data class PhysicsComponent(
    val physicsId: UUID = UUID.randomUUID(),
    val physContextIDs: Set<String> = emptySet(),
    /** Полный размер коллайдера (BoxShape). Default 1×1×1 — обычный куб. */
    val size: Vec3D = Vec3D.ONE,
) : EcsComponent<PhysicsComponent> {
    override fun type() = PhysicsComponent

    companion object : ComponentType<PhysicsComponent>()
}
