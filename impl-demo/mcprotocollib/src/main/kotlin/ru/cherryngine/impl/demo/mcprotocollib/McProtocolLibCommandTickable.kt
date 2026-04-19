package ru.cherryngine.impl.demo.mcprotocollib

import org.geysermc.mcprotocollib.protocol.packet.ingame.clientbound.ClientboundCommandSuggestionsPacket
import ru.cherryngine.engine.core.commandmanager.CherryngineCommandManager
import ru.cherryngine.engine.core.commandmanager.CommandSender
import ru.cherryngine.engine.core.instance.InstanceSingleton
import ru.cherryngine.engine.core.instance.TickStage
import ru.cherryngine.engine.core.instance.Tickable
import ru.cherryngine.engine.core.player.PlayerManager
import ru.cherryngine.engine.mcprotocollib.McProtocolLibCommandNodeUtils
import ru.cherryngine.engine.mcprotocollib.McProtocolLibPlayer
import java.util.*
import kotlin.time.Duration

@InstanceSingleton(platform = "mcprotocollib", stage = TickStage.PRE)
class McProtocolLibCommandTickable(
    private val playerManager: PlayerManager,
    private val commandManager: CherryngineCommandManager,
) : Tickable {
    private val playersWithCommandTree = mutableSetOf<UUID>()

    override fun tick(delta: Duration) {
        for (player in playerManager.onlinePlayers()) {
            val mcplPlayer = player as? McProtocolLibPlayer ?: continue

            if (mcplPlayer.uuid !in playersWithCommandTree) {
                mcplPlayer.session.send(
                    McProtocolLibCommandNodeUtils.commandsPacket(commandManager.commandTree().rootNode())
                )
                playersWithCommandTree.add(mcplPlayer.uuid)
            }

            while (true) {
                val command = mcplPlayer.pendingCommands.poll() ?: break
                commandManager.commandExecutor().executeCommand(mcplPlayer as CommandSender, command)
            }

            while (true) {
                val (transactionId, input) = mcplPlayer.pendingSuggestions.poll() ?: break
                commandManager.suggestionFactory().suggest(mcplPlayer as CommandSender, input)
                    .whenComplete { suggestions, throwable ->
                        if (throwable != null) throw throwable
                        val lastSpace = input.lastIndexOf(' ')
                        mcplPlayer.session.send(
                            ClientboundCommandSuggestionsPacket(
                                transactionId,
                                lastSpace + 2,
                                input.length - lastSpace - 1,
                                suggestions.list().map { it.suggestion() }.toTypedArray(),
                                arrayOfNulls(suggestions.list().size)
                            )
                        )
                    }
            }
        }
    }

    fun onPlayerLeave(uuid: UUID) {
        playersWithCommandTree.remove(uuid)
    }
}
