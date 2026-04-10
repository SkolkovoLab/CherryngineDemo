package ru.cherryngine.impl.demo.components

import com.github.quillraven.fleks.ComponentType
import ru.cherryngine.engine.ecs.EcsComponent

data class PhysicsComponent(
    val bodyInfo: BodyInfo,
    val physContextIDs: Set<String> = emptySet(),
) : EcsComponent<PhysicsComponent> {
    override fun type() = PhysicsComponent

    companion object : ComponentType<PhysicsComponent>()

    sealed interface BodyInfo {
        object Cube : BodyInfo
    }
}
