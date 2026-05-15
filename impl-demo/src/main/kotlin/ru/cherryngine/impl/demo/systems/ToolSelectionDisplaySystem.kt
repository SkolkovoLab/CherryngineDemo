package ru.cherryngine.impl.demo.systems

import com.github.quillraven.fleks.IntervalSystem
import ru.cherryngine.engine.core.instance.Instance
import ru.cherryngine.engine.core.player.PlayerManager
import ru.cherryngine.engine.ecs.components.PlayerComponent
import ru.cherryngine.impl.demo.EcsSystemConfig
import ru.cherryngine.impl.demo.components.DisplayNameComponent
import ru.cherryngine.impl.demo.components.InventoryComponent
import ru.cherryngine.impl.demo.renderer.ToolSelectionRendererDispatcher

class ToolSelectionDisplaySystem(
    private val playerManager: PlayerManager,
    private val dispatcher: ToolSelectionRendererDispatcher,
) : IntervalSystem() {

    override fun onTick() {
        world.family { all(PlayerComponent, InventoryComponent) }.forEach { entity ->
            val player = playerManager.getPlayerNullable(entity[PlayerComponent].uuid) ?: return@forEach
            val inventory = entity[InventoryComponent]
            val activeItem = inventory.slots[inventory.activeSlot] ?: return@forEach
            val displayName = activeItem.getOrNull(DisplayNameComponent)?.name ?: return@forEach
            dispatcher.showTool(player, displayName)
        }
    }

    object Config : EcsSystemConfig {
        override fun create(instance: Instance) = ToolSelectionDisplaySystem(
            playerManager = instance.get(),
            dispatcher = instance.get(),
        )
    }
}
