package ru.cherryngine.impl.demo.renderer

import ru.cherryngine.engine.core.player.Player

interface PlayerRenderer {
    fun onJoin(player: Player) = Unit
    fun onLeave(player: Player) = Unit
    fun onViewContextChanged(player: Player, contextIDs: Set<String>) = Unit
}