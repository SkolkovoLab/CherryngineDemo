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
        // Максимальная высота ступеньки, на которую может "шагнуть" хитбокс (в блоках).
        // 0.6 — как в Minecraft (игрок сам шагает на 0.6).
        private const val STEP_HEIGHT = 0.6
        // Расстояние down-cast'а: чуть больше STEP_HEIGHT, чтобы на boundary (ровный пол)
        // Jolt не вернул null из-за точного касания.
        private const val STEP_DOWN_DIST = STEP_HEIGHT + 0.1
        // Минимальное горизонтальное движение, при котором проверяется step-up.
        private const val STEP_MIN_HORIZONTAL = 0.01
        // Минимальный подъём хитбокса для срабатывания step-up.
        private const val STEP_Y_THRESHOLD = 0.05
        // Если shape-cast вернул fraction >= этого, считаем путь свободным.
        private const val STEP_CLEAR_FRACTION = 0.99
        // Минимальная доля down-cast'а — защита от penetration: если fraction ≈ 0,
        // значит lifted-позиция уже внутри препятствия (cube выше STEP_HEIGHT), step-up невозможен.
        private const val STEP_MIN_DOWN_FRAC = 0.001
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

        // Server-driven step-up: независимо от того, шагнул ли клиент. Хитбокс сам пытается
        // взобраться на препятствие ≤ STEP_HEIGHT, клиент подтянется через postSimulate.
        //
        // 1. up-cast: проверить, что над хитбоксом свободно на STEP_HEIGHT (нет низкого потолка).
        // 2. down-cast из (lifted + horizontal): найти верх препятствия в точке, куда хочет идти клиент.
        // 3. Валидация: newCenterY реально выше текущего hitboxPos.y (ступенька, не ровный пол)
        //    и downFrac > 0 (lifted-позиция не в penetration — препятствие не выше STEP_HEIGHT).
        var steppedUp = false
        val horizontalOffset = Vec3D(diff.x, 0.0, diff.z)
        if (horizontalOffset.length() > STEP_MIN_HORIZONTAL) {
            val upOffset = Vec3D(0.0, STEP_HEIGHT, 0.0)
            val upFrac = physicsSpace.castShapeFrom(physicsId, hitboxPos, upOffset)
            if (upFrac == null || upFrac >= STEP_CLEAR_FRACTION) {
                val lifted = hitboxPos + upOffset
                val afterMove = lifted + horizontalOffset
                val downFrac = physicsSpace.castShapeFrom(physicsId, afterMove, Vec3D(0.0, -STEP_DOWN_DIST, 0.0))
                if (downFrac != null && downFrac > STEP_MIN_DOWN_FRAC) {
                    val newCenterY = afterMove.y - STEP_DOWN_DIST * downFrac
                    if (newCenterY > hitboxPos.y + STEP_Y_THRESHOLD) {
                        body.teleport(Vec3D(afterMove.x, newCenterY, afterMove.z))
                        steppedUp = true
                    }
                }
            }
        }

        // После step-up хитбокс поставлен точно на верх ступеньки — velocity нулевая,
        // иначе симуляция ещё что-то сдвинет. Без step-up — обычная velocity-интеграция.
        if (steppedUp) {
            body.setLinearVelocity(Vec3D.ZERO)
        } else {
            body.setLinearVelocity(diff * (1.0 / deltaSec))
        }
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
