package ru.cherryngine.impl.demo.bedrock.renderer

import org.cloudburstmc.protocol.bedrock.data.command.*
import org.cloudburstmc.protocol.bedrock.packet.AvailableCommandsPacket
import org.incendo.cloud.parser.standard.LiteralParser
import ru.cherryngine.engine.core.commandmanager.CherryngineCommandManager
import ru.cherryngine.engine.core.instance.InstanceSingleton
import ru.cherryngine.engine.core.player.Player
import ru.cherryngine.impl.demo.renderer.PlayerRenderer
import ru.cherryngine.platform.minecraft.bedrock.BedrockPlayer

@InstanceSingleton(platform = "bedrock")
class BedrockPlayerRenderer(
    private val commandManager: CherryngineCommandManager,
) : PlayerRenderer<BedrockPlayer> {

    override fun canHandle(target: Player): Boolean = target is BedrockPlayer

    override fun onJoin(player: BedrockPlayer) {
        sendAvailableCommands(player)
    }

    override fun onViewContextChanged(player: BedrockPlayer, contextIDs: Set<String>) {
        if (player.viewContextIDs == contextIDs) return
        player.viewContextIDs = contextIDs
        player.sentChunks.clear()
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
