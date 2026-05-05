package ru.cherryngine.impl.demo.systems

import com.github.quillraven.fleks.IntervalSystem
import ru.cherryngine.engine.core.instance.Instance
import ru.cherryngine.engine.core.player.PlayerManager
import ru.cherryngine.engine.ecs.components.PlayerComponent
import ru.cherryngine.impl.demo.EcsSystemConfig
import ru.cherryngine.impl.demo.components.SelectedToolComponent
import ru.cherryngine.impl.demo.renderer.ToolSelectionRendererDispatcher

class ToolSelectionDisplaySystem(
    private val playerManager: PlayerManager,
    private val dispatcher: ToolSelectionRendererDispatcher,
) : IntervalSystem() {

    override fun onTick() {
        world.family { all(PlayerComponent, SelectedToolComponent) }.forEach { entity ->
            val player = playerManager.getPlayerNullable(entity[PlayerComponent].uuid) ?: return@forEach
            dispatcher.showTool(player, entity[SelectedToolComponent].tool)
        }
    }

    object Config : EcsSystemConfig {
        override fun create(instance: Instance) = ToolSelectionDisplaySystem(
            playerManager = instance.get(),
            dispatcher = instance.get(),
        )
    }
}
