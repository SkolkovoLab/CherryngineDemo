package ru.cherryngine.impl.demo.components

import com.github.quillraven.fleks.ComponentType
import ru.cherryngine.engine.ecs.EcsComponent

data class ApartComponent(
    var apartName: String,
) : EcsComponent<ApartComponent> {
    override fun type() = ApartComponent

    companion object : ComponentType<ApartComponent>()
}