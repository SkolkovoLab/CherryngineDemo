package ru.cherryngine.impl.demo.minecraft

import net.minestom.server.network.packet.server.play.TabCompletePacket
import ru.cherryngine.engine.core.commandmanager.CherryngineCommandManager
import ru.cherryngine.engine.core.commandmanager.CommandSender
import ru.cherryngine.engine.core.instance.InstanceSingleton
import ru.cherryngine.engine.core.instance.TickStage
import ru.cherryngine.engine.core.instance.Tickable
import ru.cherryngine.engine.core.player.CommandDispatcher
import ru.cherryngine.engine.core.player.PlayerManager
import ru.cherryngine.engine.core.player.SuggestionDispatcher
import ru.cherryngine.platform.minecraft.java.player.MinecraftPlayer
import kotlin.time.Duration

@InstanceSingleton(platform = "minecraft", stage = TickStage.PRE)
class MinecraftCommandTickable(
    private val playerManager: PlayerManager,
    private val commandManager: CherryngineCommandManager,
    private val commandDispatcher: CommandDispatcher,
    private val suggestionDispatcher: SuggestionDispatcher,
) : Tickable {

    override fun tick(delta: Duration) {
        for (player in playerManager.onlinePlayers()) {
            commandDispatcher.pollCommands(player).forEach { command ->
                commandManager.commandExecutor().executeCommand(player as CommandSender, command)
            }

            suggestionDispatcher.pollSuggestions(player).forEach { (transactionId, input) ->
                commandManager.suggestionFactory().suggest(player as CommandSender, input)
                    .whenComplete { suggestions, throwable ->
                        if (throwable != null) throw throwable
                        val lastSpace = input.lastIndexOf(' ')
                        (player as? MinecraftPlayer)?.connection?.sendPacket(
                            TabCompletePacket(
                                transactionId,
                                lastSpace + 2,
                                input.length - lastSpace - 1,
                                suggestions.list().map {
                                    TabCompletePacket.Match(it.suggestion(), null)
                                }
                            )
                        )
                    }
            }
        }
    }
}
