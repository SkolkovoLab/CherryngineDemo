package ru.cherryngine.impl.demo.input

import ru.cherryngine.engine.core.platform.PlatformHandler
import ru.cherryngine.engine.core.player.Player

/**
 * Демо-уровневый источник "сколько раз игрок махнул рукой в этом тике".
 * Иллюстрирует: новый тип ввода добавляется без правок движка — реализация
 * платформы читает rawPackets-снепшот через player.packets&lt;T&gt;().
 */
interface SwingSource<in P : Player> : PlatformHandler<Player> {
    fun pollSwings(player: P): Int
}
