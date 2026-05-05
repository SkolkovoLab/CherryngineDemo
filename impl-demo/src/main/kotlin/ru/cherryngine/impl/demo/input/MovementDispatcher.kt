package ru.cherryngine.impl.demo.input

import ru.cherryngine.engine.core.instance.InstanceSingleton
import ru.cherryngine.engine.core.player.Player

@InstanceSingleton
class MovementDispatcher(private val sources: List<MovementSource<*>>) {
    @Suppress("UNCHECKED_CAST")
    fun pollMovement(player: Player): MovementSnapshot? =
        (sources.firstOrNull { it.canHandle(player) } as? MovementSource<Player>)
            ?.pollMovement(player)
}
