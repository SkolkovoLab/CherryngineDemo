package ru.cherryngine.impl.demo.minecraft

import net.minestom.server.network.packet.server.play.TabCompletePacket
import ru.cherryngine.engine.core.commandmanager.CherryngineCommandManager
import ru.cherryngine.engine.core.commandmanager.CommandSender
import ru.cherryngine.engine.core.instance.InstanceSingleton
import ru.cherryngine.engine.core.instance.TickStage
import ru.cherryngine.engine.core.instance.Tickable
import ru.cherryngine.engine.core.player.PlayerManager
import ru.cherryngine.engine.minecraft.player.MinecraftPlayer
import kotlin.time.Duration

@InstanceSingleton(platform = "minecraft", stage = TickStage.PRE)
class MinecraftCommandTickable(
    private val playerManager: PlayerManager,
    private val commandManager: CherryngineCommandManager,
) : Tickable {

    override fun tick(delta: Duration) {
        for (player in playerManager.onlinePlayers()) {
            val mcPlayer = player as? MinecraftPlayer ?: continue

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
