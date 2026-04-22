package ru.cherryngine.impl.demo.renderer

import ru.cherryngine.engine.core.instance.InstanceSingleton
import ru.cherryngine.engine.core.player.Player

@InstanceSingleton
class PlayerRendererDispatcher(
    private val renderers: List<PlayerRenderer<*>>,
) {
    @Suppress("UNCHECKED_CAST")
    fun onJoin(player: Player) =
        renderers.filter { it.canHandle(player) }
            .forEach { (it as PlayerRenderer<Player>).onJoin(player) }

    @Suppress("UNCHECKED_CAST")
    fun onLeave(player: Player) =
        renderers.filter { it.canHandle(player) }
            .forEach { (it as PlayerRenderer<Player>).onLeave(player) }

    @Suppress("UNCHECKED_CAST")
    fun onViewContextChanged(player: Player, contextIDs: Set<String>) =
        renderers.filter { it.canHandle(player) }
            .forEach { (it as PlayerRenderer<Player>).onViewContextChanged(player, contextIDs) }
}
