package ru.cherryngine.impl.demo

import ru.cherryngine.engine.core.player.PlayerInputProvider
import ru.cherryngine.lib.math.Vec3D
import ru.cherryngine.lib.math.YawPitch
import java.util.*

class CompositePlayerInputProvider(
    private val providers: List<PlayerInputProvider>,
) : PlayerInputProvider {
    override fun getPosition(uuid: UUID): Vec3D? =
        providers.firstNotNullOfOrNull { it.getPosition(uuid) }

    override fun getYawPitch(uuid: UUID): YawPitch? =
        providers.firstNotNullOfOrNull { it.getYawPitch(uuid) }
}
