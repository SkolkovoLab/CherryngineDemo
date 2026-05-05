package ru.cherryngine.impl.demo.input

import ru.cherryngine.engine.core.platform.PlatformHandler
import ru.cherryngine.engine.core.player.Player

/**
 * Демо-уровневый источник команд игрока. Реализации читают команды
 * из платформенного снепшота ввода (rawPackets).
 */
interface CommandSource<in P : Player> : PlatformHandler<Player> {
    fun pollCommands(player: P): List<String>
}
