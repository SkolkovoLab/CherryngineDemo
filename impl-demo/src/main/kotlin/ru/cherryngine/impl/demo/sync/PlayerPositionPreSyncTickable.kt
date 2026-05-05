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
 * PRE-стадия: если ECS-PositionComponent не двигался с прошлого тика
 * (равен последнему applied), доверяем клиенту — пишем его репорт в ECS.
 * Клиентский репорт читаем через MovementDispatcher.
 */
@InstanceSingleton(stage = TickStage.PRE)
class PlayerPositionPreSyncTickable(
    private val playerManager: PlayerManager,
    private val ecsWorld: EcsWorld,
    private val movementDispatcher: MovementDispatcher,
    private val shadow: PlayerPositionShadow,
) : Tickable {
    override fun tick(delta: Duration) {
        for (player in playerManager.onlinePlayers()) {
            val entity = ecsWorld.getPlayerEntityOrNull(player.uuid) ?: continue
            val ecsPos = with(ecsWorld) { entity.getOrNull(PositionComponent) } ?: continue
            val desired = PositionSnapshot(ecsPos.position, ecsPos.yawPitch)
            val applied = shadow[player.uuid]
            if (applied != null && desired == applied) {
                val client = movementDispatcher.pollMovement(player) ?: continue
                val snap = PositionSnapshot(client.position, client.yawPitch)
                ecsPos.position = snap.position
                ecsPos.yawPitch = snap.yawPitch
                shadow[player.uuid] = snap
            }
        }
    }
}
