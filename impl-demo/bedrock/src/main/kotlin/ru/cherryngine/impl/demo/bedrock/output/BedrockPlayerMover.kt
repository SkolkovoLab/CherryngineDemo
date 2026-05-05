package ru.cherryngine.impl.demo.bedrock.output

import org.cloudburstmc.math.vector.Vector3f
import org.cloudburstmc.protocol.bedrock.packet.MovePlayerPacket
import org.cloudburstmc.protocol.bedrock.packet.SetEntityMotionPacket
import ru.cherryngine.engine.core.instance.InstanceSingleton
import ru.cherryngine.engine.core.player.Player
import ru.cherryngine.impl.demo.output.PlayerMover
import ru.cherryngine.lib.math.Vec3D
import ru.cherryngine.lib.math.YawPitch
import ru.cherryngine.platform.minecraft.bedrock.BedrockPlayer
import ru.cherryngine.platform.minecraft.bedrock.utils.cloudburstVector3f

@InstanceSingleton(platform = "bedrock")
class BedrockPlayerMover : PlayerMover<BedrockPlayer> {
    override fun canHandle(target: Player): Boolean = target is BedrockPlayer

    override fun teleport(player: BedrockPlayer, position: Vec3D, yawPitch: YawPitch) {
        player.clientPosition = position
        player.clientYawPitch = yawPitch
        val packet = MovePlayerPacket()
        packet.runtimeEntityId = player.runtimeEntityId
        packet.position = position.plus(0.0, 1.62, 0.0).cloudburstVector3f()
        packet.rotation = Vector3f.from(yawPitch.pitch, yawPitch.yaw, yawPitch.yaw)
        packet.mode = MovePlayerPacket.Mode.TELEPORT
        packet.teleportationCause = MovePlayerPacket.TeleportationCause.COMMAND
        player.session.sendPacket(packet)
    }

    override fun correctClientPosition(player: BedrockPlayer, position: Vec3D) {
        // Bedrock MovePlayerPacket не имеет relative-флагов — эмулируем Java-аналог
        // через absolute teleport с сохранённым clientYawPitch: камера визуально не поворачивается.
        player.clientPosition = position
        val packet = MovePlayerPacket()
        packet.runtimeEntityId = player.runtimeEntityId
        packet.position = position.plus(0.0, 1.62, 0.0).cloudburstVector3f()
        packet.rotation = Vector3f.from(player.clientYawPitch.pitch, player.clientYawPitch.yaw, player.clientYawPitch.yaw)
        packet.mode = MovePlayerPacket.Mode.TELEPORT
        packet.teleportationCause = MovePlayerPacket.TeleportationCause.COMMAND
        player.session.sendPacket(packet)
    }

    override fun setVelocity(player: BedrockPlayer, velocity: Vec3D) {
        val packet = SetEntityMotionPacket()
        packet.runtimeEntityId = player.runtimeEntityId
        packet.motion = (velocity / 20.0).cloudburstVector3f()
        player.session.sendPacket(packet)
    }
}
