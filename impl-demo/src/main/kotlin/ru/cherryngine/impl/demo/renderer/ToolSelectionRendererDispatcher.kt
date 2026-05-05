package ru.cherryngine.impl.demo.renderer

import ru.cherryngine.engine.core.instance.InstanceSingleton
import ru.cherryngine.engine.core.player.Player
import ru.cherryngine.impl.demo.components.Tool

@InstanceSingleton
class ToolSelectionRendererDispatcher(
    private val renderers: List<ToolSelectionRenderer<*>>,
) {
    @Suppress("UNCHECKED_CAST")
    fun showTool(player: Player, tool: Tool) =
        renderers.filter { it.canHandle(player) }
            .forEach { (it as ToolSelectionRenderer<Player>).showTool(player, tool) }
}
