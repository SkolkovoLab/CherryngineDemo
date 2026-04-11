package ru.cherryngine.impl.demo.components

import com.github.quillraven.fleks.ComponentType
import net.kyori.adventure.key.Key
import ru.cherryngine.engine.ecs.EcsComponent
import ru.cherryngine.lib.math.Transform
import java.util.*

data class CubeModelComponent(
    val modelId: UUID = UUID.randomUUID(),
    var material: Key,
    var transform: Transform,
) : EcsComponent<CubeModelComponent> {
    override fun type() = CubeModelComponent

    companion object : ComponentType<CubeModelComponent>()
}