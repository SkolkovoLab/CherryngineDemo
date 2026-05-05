package ru.cherryngine.impl.demo.input

import ru.cherryngine.engine.core.platform.PlatformHandler
import ru.cherryngine.engine.core.player.Player

/**
 * Демо-уровневый источник tab-complete запросов игрока.
 */
interface SuggestionSource<in P : Player> : PlatformHandler<Player> {
    fun pollSuggestions(player: P): List<SuggestionRequest>
}

data class SuggestionRequest(val transactionId: Int, val input: String)
