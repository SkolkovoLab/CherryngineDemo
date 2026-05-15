package ru.cherryngine.impl.demo.components

import com.github.quillraven.fleks.ComponentType
import ru.cherryngine.engine.ecs.EcsComponent
import ru.cherryngine.engine.ecs.EcsEntity

data class InventoryComponent(
    val slots: Array<EcsEntity?> = arrayOfNulls(9),
    val size: Int = 9,
    var activeSlot: Int = 0,
) : EcsComponent<InventoryComponent> {
    override fun type() = InventoryComponent

    companion object : ComponentType<InventoryComponent>()
}
