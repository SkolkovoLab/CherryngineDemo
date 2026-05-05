package ru.cherryngine.impl.demo.input

import ru.cherryngine.engine.core.instance.InstanceSingleton
import ru.cherryngine.engine.core.player.Player

@InstanceSingleton
class SwingDispatcher(private val sources: List<SwingSource<*>>) {
    @Suppress("UNCHECKED_CAST")
    fun pollSwings(player: Player): Int =
        (sources.firstOrNull { it.canHandle(player) } as? SwingSource<Player>)
            ?.pollSwings(player) ?: 0
}
