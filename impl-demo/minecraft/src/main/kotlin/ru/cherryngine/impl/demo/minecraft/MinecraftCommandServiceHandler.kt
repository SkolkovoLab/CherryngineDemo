package ru.cherryngine.impl.demo.minecraft

import jakarta.inject.Singleton
import ru.cherryngine.engine.core.commandmanager.CommandSender
import ru.cherryngine.engine.core.commandmanager.CommandServiceHandler
import ru.cherryngine.engine.core.player.Player
import ru.cherryngine.engine.minecraft.commandmanager.CommandNodeUtils
import ru.cherryngine.engine.minecraft.player.MinecraftPlayer
import ru.cherryngine.impl.demo.DemoInit
import java.util.concurrent.CompletableFuture

@Singleton
class MinecraftCommandServiceHandler(
    private val demoInit: DemoInit,
) : CommandServiceHandler {
    override fun canHandle(player: Player) = player is MinecraftPlayer

    override fun onPlayerLeave(player: Player) {}

    override fun onPlayerJoin(player: Player) {
        player as MinecraftPlayer
        val commandsPacket = CommandNodeUtils.commandsPacket(
            demoInit.commandManager.commandTree().rootNode()
        )
        player.connection.sendPacket(commandsPacket)
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
