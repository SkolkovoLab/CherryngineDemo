package ru.cherryngine.impl.demo.components

import com.github.quillraven.fleks.ComponentType
import ru.cherryngine.engine.ecs.EcsComponent
import java.util.*

data class AxolotlModelComponent(
    val modelId: UUID = UUID.randomUUID(),
) : EcsComponent<AxolotlModelComponent> {
    override fun type() = AxolotlModelComponent

    companion object : ComponentType<AxolotlModelComponent>()
}
