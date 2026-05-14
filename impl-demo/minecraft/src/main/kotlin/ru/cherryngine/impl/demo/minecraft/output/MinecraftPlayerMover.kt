package ru.cherryngine.impl.demo.minecraft.output

import net.minestom.server.coordinate.Vec
import net.minestom.server.entity.RelativeFlags
import net.minestom.server.network.packet.server.play.EntityVelocityPacket
import net.minestom.server.network.packet.server.play.PlayerPositionAndLookPacket
import ru.cherryngine.engine.core.instance.InstanceSingleton
import ru.cherryngine.engine.core.player.Player
import ru.cherryngine.impl.demo.output.PlayerMover
import ru.cherryngine.lib.math.Vec3D
import ru.cherryngine.lib.math.YawPitch
import ru.cherryngine.platform.minecraft.java.player.MinecraftClientState
import ru.cherryngine.platform.minecraft.java.player.MinecraftPlayer
import ru.cherryngine.platform.minecraft.java.utils.minestomVec

@InstanceSingleton(platform = "minecraft")
class MinecraftPlayerMover(
    private val clientState: MinecraftClientState,
) : PlayerMover<MinecraftPlayer> {
    override fun canHandle(target: Player): Boolean = target is MinecraftPlayer

    override fun teleport(player: MinecraftPlayer, position: Vec3D, yawPitch: YawPitch) {
        // Оптимистичный апдейт кеша — иначе PostSync на следующем тике увидит старую позицию
        // и пошлёт teleport ещё раз, до подтверждения клиента.
        clientState.setPosition(player.uuid, position)
        clientState.setYawPitch(player.uuid, yawPitch)
        player.connection.sendPacket(
            PlayerPositionAndLookPacket(
                0,
                position.minestomVec(),
                Vec.ZERO,
                yawPitch.yaw,
                yawPitch.pitch,
                0
            )
        )
    }

    override fun correctClientPosition(player: MinecraftPlayer, position: Vec3D) {
        // Java Edition поддерживает relative-флаги — передаём position как delta от текущей
        // клиентской позиции, yaw/pitch/delta-velocity = 0 с RelativeFlags.ALL: клиент
        // не меняет направление камеры и не получает дополнительный импульс.
        val current = clientState.position(player.uuid) ?: position
        val delta = position - current
        clientState.setPosition(player.uuid, position)
        player.connection.sendPacket(
            PlayerPositionAndLookPacket(
                0,
                delta.minestomVec(),
                Vec.ZERO,
                0f,
                0f,
                RelativeFlags.ALL
            )
        )
    }

    override fun setVelocity(player: MinecraftPlayer, velocity: Vec3D) {
        player.connection.sendPacket(EntityVelocityPacket(0, velocity.div(20.0).minestomVec()))
    }
}
