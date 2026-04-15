package ru.cherryngine.impl.demo

import jakarta.inject.Singleton
import ru.cherryngine.engine.core.player.Player
import ru.cherryngine.engine.core.player.PlayerRouter

@Singleton
class DemoPlayerRouter : PlayerRouter {
    override fun getInitialInstance(player: Player) = "lobby"
}
