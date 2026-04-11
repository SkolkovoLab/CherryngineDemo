package ru.cherryngine.impl.demo

import net.kyori.adventure.text.Component
import ru.cherryngine.engine.core.PlayerOutputProvider
import ru.cherryngine.lib.math.Vec3D
import ru.cherryngine.lib.math.YawPitch
import java.util.UUID

class CompositePlayerOutputProvider(
    private val providers: List<PlayerOutputProvider>,
) : PlayerOutputProvider {
    override fun teleport(uuid: UUID, position: Vec3D, yawPitch: YawPitch) {
        providers.forEach { it.teleport(uuid, position, yawPitch) }
    }

    override fun sendMessage(uuid: UUID, message: Component) {
        providers.forEach { it.sendMessage(uuid, message) }
    }
}
