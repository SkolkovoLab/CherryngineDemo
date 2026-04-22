package ru.cherryngine.impl.demo.renderer

import ru.cherryngine.engine.core.player.Player
import ru.cherryngine.engine.core.platform.PlatformHandler

interface PlayerRenderer<in P : Player> : PlatformHandler<Player> {
    fun onJoin(player: P) = Unit
    fun onLeave(player: P) = Unit
    fun onViewContextChanged(player: P, contextIDs: Set<String>) = Unit
}
