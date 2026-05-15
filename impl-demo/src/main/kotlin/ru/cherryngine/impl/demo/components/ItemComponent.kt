package ru.cherryngine.impl.demo.components

import com.github.quillraven.fleks.ComponentType
import ru.cherryngine.engine.ecs.EcsComponent

class ItemComponent : EcsComponent<ItemComponent> {
    override fun type() = ItemComponent

    companion object : ComponentType<ItemComponent>()
}
