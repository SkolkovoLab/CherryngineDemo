package ru.cherryngine.impl.demo.components

import com.github.quillraven.fleks.ComponentType
import ru.cherryngine.engine.ecs.EcsComponent
import java.util.*

data class PhysicsComponent(
    val physicsId: UUID = UUID.randomUUID(),
    val physContextIDs: Set<String> = emptySet(),
) : EcsComponent<PhysicsComponent> {
    override fun type() = PhysicsComponent

    companion object : ComponentType<PhysicsComponent>()
}
