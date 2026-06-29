package ru.cherryngine.impl.demo.sync

import ru.cherryngine.engine.core.instance.InstanceSingleton
import ru.cherryngine.engine.core.instance.TickStage
import ru.cherryngine.engine.core.instance.TickablePriority
import ru.cherryngine.engine.core.instance.Tickable
import ru.cherryngine.engine.core.player.PlayerManager
import ru.cherryngine.engine.ecs.EcsWorld
import ru.cherryngine.engine.ecs.components.InputTargetComponent
import ru.cherryngine.engine.ecs.components.PositionComponent
import ru.cherryngine.engine.ecs.getPlayerEntityOrNull
import ru.cherryngine.impl.demo.input.MovementDispatcher
import kotlin.time.Duration

/**
 * PRE-стадия: синхронизирует ECS-позицию игрока с клиентским репортом.
 *
 * Если player-entity сам себе InputTarget (первое лицо) — синхронизируем
 * и position, и yawPitch (но только если ECS-позиция не двигалась с прошлого
 * тика, т.е. равна applied — иначе серверная физика перезатёрла бы клиентский
 * репорт).
 *
 * Если игрок в third-person (например в машине) — позицию не трогаем
 * (управляет другая система), но yawPitch всё равно обновляем: камеру нужно
 * вращать вслед за мышкой клиента.
 */
@InstanceSingleton
@TickablePriority(stage = TickStage.PRE)
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
            val client = movementDispatcher.pollMovement(player) ?: continue

            val hasInputTarget = with(ecsWorld) {
                entity.getOrNull(InputTargetComponent)?.playerUuid == player.uuid
            }

            if (hasInputTarget) {
                val desired = PositionSnapshot(ecsPos.position, ecsPos.yawPitch)
                val applied = shadow[player.uuid]
                if (applied != null && desired == applied) {
                    val snap = PositionSnapshot(client.position, client.yawPitch)
                    ecsPos.position = snap.position
                    ecsPos.yawPitch = snap.yawPitch
                    shadow[player.uuid] = snap
                }
            } else {
                ecsPos.yawPitch = client.yawPitch
            }
        }
    }
}
