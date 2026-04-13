package ru.cherryngine.impl.demo.components

import com.github.quillraven.fleks.ComponentType
import ru.cherryngine.engine.ecs.EcsComponent
import java.util.*

data class HitboxVisualizationComponent(
    val ownerUuid: UUID,
) : EcsComponent<HitboxVisualizationComponent> {
    override fun type() = HitboxVisualizationComponent

    companion object : ComponentType<HitboxVisualizationComponent>()
}
