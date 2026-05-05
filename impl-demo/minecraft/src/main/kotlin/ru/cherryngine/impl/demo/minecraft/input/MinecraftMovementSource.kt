package ru.cherryngine.impl.demo.minecraft.input

import ru.cherryngine.engine.core.instance.InstanceSingleton
import ru.cherryngine.engine.core.player.Player
import ru.cherryngine.impl.demo.input.MovementSnapshot
import ru.cherryngine.impl.demo.input.MovementSource
import ru.cherryngine.platform.minecraft.java.player.MinecraftClientState
import ru.cherryngine.platform.minecraft.java.player.MinecraftPlayer

@InstanceSingleton(platform = "minecraft")
class MinecraftMovementSource(
    private val clientState: MinecraftClientState,
) : MovementSource<MinecraftPlayer> {
    override fun canHandle(target: Player): Boolean = target is MinecraftPlayer

    override fun pollMovement(player: MinecraftPlayer): MovementSnapshot? {
        val pos = clientState.position(player.uuid) ?: return null
        val yp = clientState.yawPitch(player.uuid) ?: return null
        return MovementSnapshot(pos, yp, clientState.isOnGround(player.uuid))
    }
}
