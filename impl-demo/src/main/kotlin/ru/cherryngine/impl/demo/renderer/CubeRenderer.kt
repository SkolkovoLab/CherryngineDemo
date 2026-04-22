package ru.cherryngine.impl.demo.renderer

import net.kyori.adventure.key.Key
import ru.cherryngine.engine.core.player.Player
import ru.cherryngine.engine.core.platform.EntityRenderer
import ru.cherryngine.lib.math.Transform
import ru.cherryngine.lib.math.Vec3D
import ru.cherryngine.lib.math.YawPitch
import java.util.UUID

interface CubeRenderer<in P : Player> : EntityRenderer<P> {
    fun update(
        id: UUID,
        position: Vec3D,
        yawPitch: YawPitch,
        material: Key,
        transform: Transform,
    )
}
