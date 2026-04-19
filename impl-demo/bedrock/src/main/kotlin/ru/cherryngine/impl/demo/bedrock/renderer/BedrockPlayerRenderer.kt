package ru.cherryngine.impl.demo.bedrock.renderer

import org.cloudburstmc.protocol.bedrock.data.command.*
import org.cloudburstmc.protocol.bedrock.packet.AvailableCommandsPacket
import org.incendo.cloud.parser.standard.LiteralParser
import ru.cherryngine.engine.bedrock.BedrockPlayer
import ru.cherryngine.engine.core.commandmanager.CherryngineCommandManager
import ru.cherryngine.engine.core.instance.InstanceSingleton
import ru.cherryngine.engine.core.player.Player
import ru.cherryngine.impl.demo.renderer.PlayerRenderer

@InstanceSingleton(platform = "bedrock")
class BedrockPlayerRenderer(
    private val commandManager: CherryngineCommandManager,
) : PlayerRenderer {

    override fun onJoin(player: Player) {
        val bp = player as? BedrockPlayer ?: return
        sendAvailableCommands(bp)
    }

    override fun onViewContextChanged(player: Player, contextIDs: Set<String>) {
        val bedrockPlayer = player as? BedrockPlayer ?: return
        if (bedrockPlayer.viewContextIDs == contextIDs) return
        bedrockPlayer.viewContextIDs = contextIDs
        bedrockPlayer.sentChunks.clear()
    }

    private fun sendAvailableCommands(player: BedrockPlayer) {
        val packet = AvailableCommandsPacket()
        val rootNode = commandManager.commandTree().rootNode()

        for (child in rootNode.children()) {
            val parser = child.component().parser()
            val names = if (parser is LiteralParser) {
                listOf(child.component().name()) + parser.alternativeAliases()
            } else {
                listOf(child.component().name())
            }

            for (name in names) {
                val argsParam = CommandParamData()
                argsParam.name = "args"
                argsParam.isOptional = true
                argsParam.type = CommandParam.TEXT

                packet.commands.add(CommandData(
                    name,
                    "",
                    emptySet(),
                    CommandPermission.ANY,
                    null,
                    emptyList(),
                    arrayOf(CommandOverloadData(false, arrayOf(argsParam)))
                ))
            }
        }

        player.session.sendPacket(packet)
    }
}
