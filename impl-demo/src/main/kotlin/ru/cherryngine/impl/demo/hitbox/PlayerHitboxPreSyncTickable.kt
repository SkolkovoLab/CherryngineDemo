package ru.cherryngine.impl.demo.hitbox

import ru.cherryngine.engine.core.instance.InstanceSingleton
import ru.cherryngine.engine.core.instance.TickStage
import ru.cherryngine.engine.core.instance.Tickable
import ru.cherryngine.engine.core.player.PlayerManager
import kotlin.time.Duration

@InstanceSingleton(stage = TickStage.PRE)
class PlayerHitboxPreSyncTickable(
    private val playerManager: PlayerManager,
    private val drivers: List<PlayerHitboxDriver>,
) : Tickable {
    override fun tick(delta: Duration) {
        for (player in playerManager.onlinePlayers()) {
            drivers.firstOrNull { it.canHandle(player) }?.preSimulate(player, delta)
        }
    }
}
