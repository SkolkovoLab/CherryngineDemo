package ru.cherryngine.impl.demo.renderer

import ru.cherryngine.engine.core.platform.PlatformHandler
import ru.cherryngine.engine.core.player.Player
import ru.cherryngine.impl.demo.components.Tool

interface ToolSelectionRenderer<in P : Player> : PlatformHandler<Player> {
    fun showTool(player: P, tool: Tool)
}
