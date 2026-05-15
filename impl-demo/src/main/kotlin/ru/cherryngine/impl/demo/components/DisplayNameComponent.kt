package ru.cherryngine.impl.demo.components

import com.github.quillraven.fleks.ComponentType
import ru.cherryngine.engine.ecs.EcsComponent

data class DisplayNameComponent(val name: String) : EcsComponent<DisplayNameComponent> {
    override fun type() = DisplayNameComponent

    companion object : ComponentType<DisplayNameComponent>()
}
