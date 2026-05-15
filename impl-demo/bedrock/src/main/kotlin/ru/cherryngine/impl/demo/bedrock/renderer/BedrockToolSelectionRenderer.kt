package ru.cherryngine.impl.demo.bedrock.renderer

import org.cloudburstmc.protocol.bedrock.packet.TextPacket
import ru.cherryngine.engine.core.instance.InstanceSingleton
import ru.cherryngine.engine.core.player.Player
import ru.cherryngine.impl.demo.renderer.ToolSelectionRenderer
import ru.cherryngine.platform.minecraft.bedrock.BedrockPlayer

@InstanceSingleton(platform = "bedrock")
class BedrockToolSelectionRenderer : ToolSelectionRenderer<BedrockPlayer> {
    override fun canHandle(target: Player): Boolean = target is BedrockPlayer

    override fun showTool(player: BedrockPlayer, displayName: String) {
        val packet = TextPacket().apply {
            type = TextPacket.Type.TIP
            setMessage(displayName)
            setNeedsTranslation(false)
            xuid = ""
            sourceName = ""
        }
        player.session.sendPacket(packet)
    }
}
