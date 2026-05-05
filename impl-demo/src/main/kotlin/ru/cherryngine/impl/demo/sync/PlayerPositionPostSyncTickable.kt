package ru.cherryngine.impl.demo.sync

import ru.cherryngine.engine.core.instance.InstanceSingleton
import ru.cherryngine.engine.core.instance.TickStage
import ru.cherryngine.engine.core.instance.Tickable
import ru.cherryngine.engine.core.player.PlayerManager
import ru.cherryngine.engine.ecs.EcsWorld
import ru.cherryngine.engine.ecs.components.PositionComponent
import ru.cherryngine.engine.ecs.getPlayerEntityOrNull
import ru.cherryngine.impl.demo.input.MovementDispatcher
import kotlin.time.Duration

/**
 * POST-стадия: если ECS-позиция разошлась с тем, что репортит клиент
 * (что-то двигало игрока на сервере во время тика), телепортируем клиента к ECS.
 * Сохраняем актуальную ECS-позицию в shadow для следующего PreSync.
 */
@InstanceSingleton(stage = TickStage.POST)
class PlayerPositionPostSyncTickable(
    private val playerManager: PlayerManager,
    private val ecsWorld: EcsWorld,
    private val movementDispatcher: MovementDispatcher,
    private val shadow: PlayerPositionShadow,
) : Tickable {
    override fun tick(delta: Duration) {
        for (player in playerManager.onlinePlayers()) {
            val entity = ecsWorld.getPlayerEntityOrNull(player.uuid) ?: continue
            val ecsPos = with(ecsWorld) { entity.getOrNull(PositionComponent) } ?: continue
            val client = movementDispatcher.pollMovement(player)
            if (client == null || ecsPos.position != client.position || ecsPos.yawPitch != client.yawPitch) {
                player.teleport(ecsPos.position, ecsPos.yawPitch)
            }
            shadow[player.uuid] = PositionSnapshot(ecsPos.position, ecsPos.yawPitch)
        }
    }
}
