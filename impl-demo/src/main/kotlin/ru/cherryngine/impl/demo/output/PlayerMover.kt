package ru.cherryngine.impl.demo.output

import ru.cherryngine.engine.core.platform.PlatformHandler
import ru.cherryngine.engine.core.player.Player
import ru.cherryngine.lib.math.Vec3D
import ru.cherryngine.lib.math.YawPitch

/**
 * Демо-уровневые исходящие действия движения над игроком.
 * Реализации шлют платформенные пакеты (Java — relative-флаги, Bedrock — absolute teleport).
 */
interface PlayerMover<in P : Player> : PlatformHandler<Player> {
    /** Абсолютный телепорт: position + yawPitch применяются как есть, камера клиента поворачивается. */
    fun teleport(player: P, position: Vec3D, yawPitch: YawPitch) = Unit

    /**
     * Мягко переместить клиента в [position] без смены направления взгляда и без модификации velocity.
     * Каждая платформа выбирает оптимальный путь.
     */
    fun correctClientPosition(player: P, position: Vec3D) = Unit

    fun setVelocity(player: P, velocity: Vec3D) = Unit
}
