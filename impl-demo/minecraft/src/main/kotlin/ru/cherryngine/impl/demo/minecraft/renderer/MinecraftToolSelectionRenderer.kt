package ru.cherryngine.impl.demo.minecraft.renderer

import net.kyori.adventure.text.Component
import net.minestom.server.network.packet.server.play.SystemChatPacket
import ru.cherryngine.engine.core.instance.InstanceSingleton
import ru.cherryngine.engine.core.player.Player
import ru.cherryngine.impl.demo.renderer.ToolSelectionRenderer
import ru.cherryngine.platform.minecraft.java.player.MinecraftPlayer

@InstanceSingleton(platform = "minecraft")
class MinecraftToolSelectionRenderer : ToolSelectionRenderer<MinecraftPlayer> {
    override fun canHandle(target: Player): Boolean = target is MinecraftPlayer

    override fun showTool(player: MinecraftPlayer, displayName: String) {
        player.connection.sendPacket(SystemChatPacket(Component.text(displayName), true))
    }
}
