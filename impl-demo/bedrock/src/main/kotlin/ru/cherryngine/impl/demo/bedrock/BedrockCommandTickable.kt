package ru.cherryngine.impl.demo.bedrock

import org.cloudburstmc.protocol.bedrock.data.command.CommandData
import org.cloudburstmc.protocol.bedrock.data.command.CommandOverloadData
import org.cloudburstmc.protocol.bedrock.data.command.CommandParam
import org.cloudburstmc.protocol.bedrock.data.command.CommandParamData
import org.cloudburstmc.protocol.bedrock.data.command.CommandPermission
import org.cloudburstmc.protocol.bedrock.packet.AvailableCommandsPacket
import org.incendo.cloud.parser.standard.LiteralParser
import ru.cherryngine.engine.bedrock.BedrockPlayer
import ru.cherryngine.engine.core.commandmanager.CherryngineCommandManager
import ru.cherryngine.engine.core.commandmanager.CommandSender
import ru.cherryngine.engine.core.instance.Tickable
import ru.cherryngine.engine.core.player.PlayerManager
import java.util.*
import kotlin.time.Duration

class BedrockCommandTickable(
    private val playerManager: PlayerManager,
    private val commandManager: CherryngineCommandManager,
) : Tickable {
    private val playersWithCommands = mutableSetOf<UUID>()

    override fun tick(delta: Duration) {
        for (player in playerManager.onlinePlayers()) {
            val bp = player as? BedrockPlayer ?: continue

            if (bp.uuid !in playersWithCommands) {
                sendAvailableCommands(bp)
                playersWithCommands.add(bp.uuid)
            }

            while (true) {
                val command = bp.pendingCommands.poll() ?: break
                commandManager.commandExecutor().executeCommand(bp as CommandSender, command)
            }
        }
    }

    private fun sendAvailableCommands(player: BedrockPlayer) {
        val packet = AvailableCommandsPacket()
        val rootNode = commandManager.commandTree().rootNode()

        // Как Java-версия: каждая команда — литерал + один greedy string аргумент
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

    fun onPlayerLeave(uuid: UUID) {
        playersWithCommands.remove(uuid)
    }
}
