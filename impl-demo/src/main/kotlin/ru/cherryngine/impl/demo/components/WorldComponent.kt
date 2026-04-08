package ru.cherryngine.impl.demo.components

import com.github.quillraven.fleks.ComponentType
import ru.cherryngine.engine.ecs.EcsComponent

data class WorldComponent(
    var worldName: String,
    var priority: Int = 0,
) : EcsComponent<WorldComponent> {
    override fun type() = WorldComponent

    companion object : ComponentType<WorldComponent>()
}