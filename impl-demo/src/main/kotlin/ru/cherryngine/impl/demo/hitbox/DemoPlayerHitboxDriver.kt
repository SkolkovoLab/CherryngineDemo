package ru.cherryngine.impl.demo.hitbox

import ru.cherryngine.engine.core.instance.InstanceSingleton
import ru.cherryngine.engine.core.player.Player
import ru.cherryngine.engine.core.player.PlayerPositionSource
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
    private val positionSources: List<PlayerPositionSource>,
) : PlayerHitboxDriver {
    companion object {
        // Смещение центра хитбокса (box 0.6x1.8x0.6) относительно ног игрока.
        private val PLAYER_HITBOX_OFFSET = Vec3D(0.0, 0.9, 0.0)
        // Порог телепорта: если хитбокс отстаёт дальше этого — моментальный перенос.
        private const val TELEPORT_THRESHOLD = 2.0
        // Порог "застрял": если diff больше — возвращаем клиента к хитбоксу.
        private const val STUCK_THRESHOLD = 0.1
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
        val physicsId = playerPhysicsState.getPhysicsId(player.uuid) ?: return
        val body = physicsSpace.getOrCreateBody(physicsId, player.viewContextIDs) {
            physicsSpace.addPlayer(player.clientPosition + PLAYER_HITBOX_OFFSET)
        }

        val targetPos = player.clientPosition + PLAYER_HITBOX_OFFSET
        val hitboxPos = body.getTransform().translation
        val diff = targetPos - hitboxPos
        if (diff.length() <= STUCK_THRESHOLD) return

        val serverFeetPos = hitboxPos - PLAYER_HITBOX_OFFSET
        // Если клиента поднимает (новая Y > старой), добавляем 1/16 блока — чтобы клиент
        // гарантированно оказался ВЫШЕ платформы шалкера под ногами, а не впритык к ней
        // (иначе клиент может провалиться обратно и шалкер не появится).
        val finalFeetPos = if (serverFeetPos.y > player.clientPosition.y) {
            serverFeetPos + Vec3D(0.0, 1.0 / 16.0, 0.0)
        } else {
            serverFeetPos
        }

        // 1. Обновляем активный PositionSource (обычно ECS PositionComponent) — иначе
        //    PlayerPositionPostSyncTickable может запуститься после нас в POST-стадии
        //    и откатить клиента к старому PositionComponent.position (порядок Tickable'ов
        //    внутри одной stage не гарантирован).
        positionSources.firstOrNull { it.canHandle(player) }
            ?.acceptClientMovement(player, finalFeetPos, player.clientYawPitch)

        // 2. Физически возвращаем клиента на серверно-корректную позицию (ноги хитбокса).
        //    correctClientPosition — платформо-специфичная мягкая коррекция: Java шлёт relative
        //    teleport через RelativeFlags.ALL, Bedrock — absolute с сохранённым yawPitch.
        player.correctClientPosition(finalFeetPos)
    }

    private fun resolvePhysicsId(playerUuid: UUID): UUID {
        playerPhysicsState.getPhysicsId(playerUuid)?.let { return it }
        val newId = UUID.randomUUID()
        playerPhysicsState.register(playerUuid, newId)
        return newId
    }
}
