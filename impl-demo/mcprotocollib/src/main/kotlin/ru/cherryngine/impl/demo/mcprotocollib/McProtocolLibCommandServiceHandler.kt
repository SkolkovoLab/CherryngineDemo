package ru.cherryngine.impl.demo.mcprotocollib

import jakarta.inject.Singleton
import ru.cherryngine.engine.core.commandmanager.CommandSender
import ru.cherryngine.engine.core.commandmanager.CommandServiceHandler
import ru.cherryngine.engine.core.player.Player
import ru.cherryngine.engine.mcprotocollib.McProtocolLibCommandNodeUtils
import ru.cherryngine.engine.mcprotocollib.McProtocolLibPlayer
import ru.cherryngine.impl.demo.DemoInit
import java.util.concurrent.CompletableFuture

@Singleton
class McProtocolLibCommandServiceHandler(
    private val demoInit: DemoInit,
) : CommandServiceHandler {
    override fun canHandle(player: Player) = player is McProtocolLibPlayer

    override fun onPlayerJoin(player: Player) {
        player as McProtocolLibPlayer
        val commandsPacket = McProtocolLibCommandNodeUtils.commandsPacket(
            demoInit.commandManager.commandTree().rootNode()
        )
        player.session.send(commandsPacket)
    }

    override fun execute(player: Player, command: String) {
        demoInit.commandManager.commandExecutor().executeCommand(
            player as CommandSender, command
        )
    }

    override fun suggest(player: Player, input: String): CompletableFuture<List<String>> {
        return demoInit.commandManager.suggestionFactory()
            .suggest(player as CommandSender, input)
            .thenApply { suggestions -> suggestions.list().map { it.suggestion() } }
    }
}
