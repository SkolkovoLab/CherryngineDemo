package ru.cherryngine.impl.demo.components

import com.github.quillraven.fleks.ComponentType
import ru.cherryngine.engine.ecs.EcsComponent
import ru.cherryngine.engine.ecs.EcsEntity

/**
 * Location-маркер item-entity: предмет лежит в [slot] инвентаря [owner].
 * Денормализованный чек инварианта «у item-entity ровно одна локация» —
 * source of truth по слотам это InventoryComponent.slots на [owner].
 */
data class InInventoryComponent(
    val owner: EcsEntity,
    val slot: Int,
) : EcsComponent<InInventoryComponent> {
    override fun type() = InInventoryComponent

    companion object : ComponentType<InInventoryComponent>()
}
