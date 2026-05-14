package ru.cherryngine.impl.demo.input

import ru.cherryngine.engine.core.platform.PlatformHandler
import ru.cherryngine.engine.core.player.Player

/** Снепшот текущих нажатых клавиш driver'а (WASD + jump + shift). */
data class DriverInputSnapshot(
    val forward: Boolean = false,
    val backward: Boolean = false,
    val left: Boolean = false,
    val right: Boolean = false,
    val jump: Boolean = false,
    val shift: Boolean = false,
) {
    companion object {
        val EMPTY = DriverInputSnapshot()
    }
}

/**
 * Платформенный источник driver-input'а. У Java-Minecraft реализован поверх
 * edge-trigger `ClientInputPacket` (источник сам кэширует последний пакет
 * чтобы возвращать актуальное состояние клавиш каждый тик).
 */
interface DriverInputSource<in P : Player> : PlatformHandler<Player> {
    fun pollInput(player: P): DriverInputSnapshot

    /** Очистить кэш для игрока (вызывается при выходе из машины). */
    fun forget(player: P)
}
