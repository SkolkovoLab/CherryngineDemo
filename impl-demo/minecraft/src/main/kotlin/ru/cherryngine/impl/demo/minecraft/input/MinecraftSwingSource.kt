package ru.cherryngine.impl.demo.minecraft.input

import net.minestom.server.network.packet.client.play.ClientAnimationPacket
import ru.cherryngine.engine.core.instance.InstanceSingleton
import ru.cherryngine.engine.core.player.Player
import ru.cherryngine.impl.demo.input.SwingSource
import ru.cherryngine.platform.minecraft.java.player.MinecraftPlayer

@InstanceSingleton(platform = "minecraft")
class MinecraftSwingSource : SwingSource<MinecraftPlayer> {
    override fun canHandle(target: Player): Boolean = target is MinecraftPlayer

    override fun pollSwings(player: MinecraftPlayer): Int =
        player.packets<ClientAnimationPacket>().size
}
