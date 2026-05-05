package ru.cherryngine.impl.demo.input

import ru.cherryngine.engine.core.platform.PlatformHandler
import ru.cherryngine.engine.core.player.Player
import ru.cherryngine.lib.math.Vec3D
import ru.cherryngine.lib.math.YawPitch

/**
 * Демо-уровневый источник live-движения игрока (репорт клиента).
 * Тут интерпретация "что считать движением" — задача демо, не движка.
 */
interface MovementSource<in P : Player> : PlatformHandler<Player> {
    fun pollMovement(player: P): MovementSnapshot?
}

data class MovementSnapshot(
    val position: Vec3D,
    val yawPitch: YawPitch,
    val onGround: Boolean,
)
