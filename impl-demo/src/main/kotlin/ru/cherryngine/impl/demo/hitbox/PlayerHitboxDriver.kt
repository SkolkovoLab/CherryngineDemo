package ru.cherryngine.impl.demo.hitbox

import ru.cherryngine.engine.core.player.Player
import kotlin.time.Duration

/**
 * Платформенно/режимо-специфичный драйвер серверного хитбокса игрока.
 *
 * Причина существования: на платформах без клиентских коллизий (Minecraft Java/Bedrock)
 * сервер держит jolt-тело, следующее за клиентом, и отправляет velocity-коррекции
 * когда хитбокс упирается в terrain. На платформе с нативными коллизиями клиента
 * такой driver просто не регистрируется и диспетчер пропускает игрока.
 */
interface PlayerHitboxDriver {
    fun canHandle(player: Player): Boolean

    /** До симуляции: lifecycle хитбокса и velocity pull к [Player.clientPosition]. */
    fun preSimulate(player: Player, delta: Duration)

    /** После симуляции: meeting-point и pushback через [Player.setVelocity]. */
    fun postSimulate(player: Player, delta: Duration)
}
