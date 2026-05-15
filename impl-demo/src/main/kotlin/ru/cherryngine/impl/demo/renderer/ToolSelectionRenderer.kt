package ru.cherryngine.impl.demo.renderer

import ru.cherryngine.engine.core.platform.PlatformHandler
import ru.cherryngine.engine.core.player.Player

interface ToolSelectionRenderer<in P : Player> : PlatformHandler<Player> {
    fun showTool(player: P, displayName: String)
}
