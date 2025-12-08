package ru.cherryngine.impl.demo.components

import com.github.quillraven.fleks.ComponentType
import ru.cherryngine.engine.ecs.EcsComponent

data class PhysicsComponent(
    var spaceName: String,
    val bodyInfo: BodyInfo,
) : EcsComponent<PhysicsComponent> {
    override fun type() = PhysicsComponent

    companion object : ComponentType<PhysicsComponent>()

    sealed interface BodyInfo {
        object Cube : BodyInfo
        data class Floor(
            val y: Double,
        ) : BodyInfo
    }
}