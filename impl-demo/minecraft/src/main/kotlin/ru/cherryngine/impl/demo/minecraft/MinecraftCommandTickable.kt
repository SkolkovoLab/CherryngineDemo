package ru.cherryngine.impl.demo.minecraft

import ru.cherryngine.engine.core.commandmanager.CherryngineCommandManager
import ru.cherryngine.engine.core.commandmanager.CommandSender
import ru.cherryngine.engine.core.instance.Tickable
import ru.cherryngine.engine.core.player.PlayerManager
import ru.cherryngine.engine.minecraft.commandmanager.CommandNodeUtils
import ru.cherryngine.engine.minecraft.player.MinecraftPlayer
import ru.cherryngine.lib.minecraft.network.protocol.packets.ProtocolState
import ru.cherryngine.lib.minecraft.network.protocol.packets.play.clientbound.ClientboundCommandSuggestionsPacket
import java.util.*
import kotlin.time.Duration

class MinecraftCommandTickable(
    private val playerManager: PlayerManager,
    private val commandManager: CherryngineCommandManager,
) : Tickable {
    private val playersWithCommandTree = mutableSetOf<UUID>()

    override fun tick(delta: Duration) {
        for (player in playerManager.onlinePlayers()) {
            val mcPlayer = player as? MinecraftPlayer ?: continue

            if (mcPlayer.uuid !in playersWithCommandTree && mcPlayer.connection.state == ProtocolState.PLAY) {
                mcPlayer.connection.sendPacket(
                    CommandNodeUtils.commandsPacket(commandManager.commandTree().rootNode())
                )
                playersWithCommandTree.add(mcPlayer.uuid)
            }

            while (true) {
                val command = mcPlayer.pendingCommands.poll() ?: break
                commandManager.commandExecutor().executeCommand(mcPlayer as CommandSender, command)
            }

            while (true) {
                val (transactionId, input) = mcPlayer.pendingSuggestions.poll() ?: break
                commandManager.suggestionFactory().suggest(mcPlayer as CommandSender, input)
                    .whenComplete { suggestions, throwable ->
                        if (throwable != null) throw throwable
                        val lastSpace = input.lastIndexOf(' ')
                        mcPlayer.connection.sendPacket(
                            ClientboundCommandSuggestionsPacket(
                                transactionId,
                                lastSpace + 2,
                                input.length - lastSpace - 1,
                                suggestions.list().map {
                                    ClientboundCommandSuggestionsPacket.Suggestion(it.suggestion(), null)
                                }
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
