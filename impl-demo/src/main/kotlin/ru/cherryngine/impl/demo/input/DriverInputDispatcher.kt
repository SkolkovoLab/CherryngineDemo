package ru.cherryngine.impl.demo.input

import ru.cherryngine.engine.core.instance.InstanceSingleton
import ru.cherryngine.engine.core.player.Player

@InstanceSingleton
class DriverInputDispatcher(private val sources: List<DriverInputSource<*>>) {
    @Suppress("UNCHECKED_CAST")
    private fun pick(player: Player): DriverInputSource<Player>? =
        sources.firstOrNull { it.canHandle(player) } as? DriverInputSource<Player>

    fun pollInput(player: Player): DriverInputSnapshot =
        pick(player)?.pollInput(player) ?: DriverInputSnapshot.EMPTY

    fun forget(player: Player) {
        pick(player)?.forget(player)
    }
}
