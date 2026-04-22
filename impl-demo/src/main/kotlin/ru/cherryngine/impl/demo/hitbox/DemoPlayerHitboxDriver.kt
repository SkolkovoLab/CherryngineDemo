package ru.cherryngine.impl.demo.hitbox

import ru.cherryngine.engine.core.instance.InstanceSingleton
import ru.cherryngine.engine.core.player.Player
import ru.cherryngine.engine.physics.PhysicsSpace
import ru.cherryngine.impl.demo.PlayerPhysicsState
import ru.cherryngine.lib.math.Vec3D
import ru.cherryngine.platform.minecraft.bedrock.BedrockPlayer
import ru.cherryngine.platform.minecraft.java.player.MinecraftPlayer
import java.util.UUID
import kotlin.time.Duration
import kotlin.time.DurationUnit

@InstanceSingleton
class DemoPlayerHitboxDriver(
    private val physicsSpace: PhysicsSpace,
    private val playerPhysicsState: PlayerPhysicsState,
) : PlayerHitboxDriver {
    companion object {
        // Смещение центра хитбокса (box 0.6x1.8x0.6) относительно ног игрока.
        private val PLAYER_HITBOX_OFFSET = Vec3D(0.0, 0.9, 0.0)
        // Порог телепорта: если хитбокс отстаёт дальше этого — моментальный перенос.
        private const val TELEPORT_THRESHOLD = 2.0
        // Порог "застрял": если даже после симуляции diff больше этого — включаем meeting-point.
        private const val STUCK_THRESHOLD = 0.1
        // Множитель для player.setVelocity (Minecraft: клиент интерпретирует velocity в блоках/тик).
        private const val PLAYER_PUSH_SPEED = 20.0
        // Доля пути к точке встречи (0.5 = середина между игроком и хитбоксом).
        private const val MEETING_POINT_RATIO = 0.5
        // Небольшая Y-прибавка к meeting-point — помогает выбраться из terrain.
        private val MEETING_POINT_LIFT = Vec3D(0.0, 0.1, 0.0)
    }

    override fun canHandle(player: Player): Boolean =
        player is MinecraftPlayer || player is BedrockPlayer

    override fun preSimulate(player: Player, delta: Duration) {
        val deltaSec = delta.toDouble(DurationUnit.SECONDS)
        val targetPos = player.clientPosition + PLAYER_HITBOX_OFFSET
        val physicsId = resolvePhysicsId(player.uuid)

        physicsSpace.keepAlive(physicsId)
        val body = physicsSpace.getOrCreateBody(physicsId, player.viewContextIDs) {
            physicsSpace.addPlayer(targetPos)
        }
        physicsSpace.updateBodyContexts(body, player.viewContextIDs)

        val hitboxPos = body.getTransform().translation
        val diff = targetPos - hitboxPos
        if (diff.length() > TELEPORT_THRESHOLD) {
            body.teleport(targetPos)
            return
        }
        body.setLinearVelocity(diff * (1.0 / deltaSec))
        body.setAngularVelocity(Vec3D.ZERO)
    }

    override fun postSimulate(player: Player, delta: Duration) {
        val deltaSec = delta.toDouble(DurationUnit.SECONDS)
        val physicsId = playerPhysicsState.getPhysicsId(player.uuid) ?: return
        val body = physicsSpace.getOrCreateBody(physicsId, player.viewContextIDs) {
            physicsSpace.addPlayer(player.clientPosition + PLAYER_HITBOX_OFFSET)
        }

        val targetPos = player.clientPosition + PLAYER_HITBOX_OFFSET
        val hitboxPos = body.getTransform().translation
        val diff = targetPos - hitboxPos
        if (diff.length() <= STUCK_THRESHOLD) return

        val meetingPoint = hitboxPos + diff * MEETING_POINT_RATIO + MEETING_POINT_LIFT

        // Тянем игрока к точке встречи через клиентский velocity
        val pushToPlayer = (meetingPoint - PLAYER_HITBOX_OFFSET) - player.clientPosition
        player.setVelocity(pushToPlayer * PLAYER_PUSH_SPEED)

        // Тянем хитбокс к точке встречи (скорость сработает на следующем pre-sim)
        val pushToHitbox = meetingPoint - hitboxPos
        body.setLinearVelocity(pushToHitbox * (1.0 / deltaSec))
    }

    private fun resolvePhysicsId(playerUuid: UUID): UUID {
        playerPhysicsState.getPhysicsId(playerUuid)?.let { return it }
        val newId = UUID.randomUUID()
        playerPhysicsState.register(playerUuid, newId)
        return newId
    }
}
