package ru.cherryngine.impl.demo.output

import ru.cherryngine.engine.core.instance.InstanceSingleton
import ru.cherryngine.engine.core.player.Player
import ru.cherryngine.lib.math.Vec3D
import ru.cherryngine.lib.math.YawPitch

@InstanceSingleton
class PlayerMoverDispatcher(private val movers: List<PlayerMover<*>>) {
    @Suppress("UNCHECKED_CAST")
    private fun pick(player: Player): PlayerMover<Player>? =
        movers.firstOrNull { it.canHandle(player) } as? PlayerMover<Player>

    fun teleport(player: Player, position: Vec3D, yawPitch: YawPitch) {
        pick(player)?.teleport(player, position, yawPitch)
    }

    fun correctClientPosition(player: Player, position: Vec3D) {
        pick(player)?.correctClientPosition(player, position)
    }

    fun setVelocity(player: Player, velocity: Vec3D) {
        pick(player)?.setVelocity(player, velocity)
    }
}
