package ru.cherryngine.impl.demo.renderer

import ru.cherryngine.engine.core.player.Player
import ru.cherryngine.engine.core.platform.EntityRenderer
import ru.cherryngine.lib.math.Vec3D
import ru.cherryngine.lib.math.YawPitch
import java.util.UUID

interface AxolotlRenderer<in P : Player> : EntityRenderer<P> {
    fun update(
        id: UUID,
        position: Vec3D,
        yawPitch: YawPitch,
        name: String?,
        hiddenFromPlayer: UUID?,
    )
}
