package ru.cherryngine.impl.demo.bedrock

import ru.cherryngine.engine.core.commandmanager.CherryngineCommandManager
import ru.cherryngine.engine.core.commandmanager.CommandSender
import ru.cherryngine.engine.core.instance.InstanceSingleton
import ru.cherryngine.engine.core.instance.TickStage
import ru.cherryngine.engine.core.instance.TickablePriority
import ru.cherryngine.engine.core.instance.Tickable
import ru.cherryngine.engine.core.player.PlayerManager
import ru.cherryngine.platform.minecraft.bedrock.BedrockPlayer
import kotlin.time.Duration

@InstanceSingleton(platform = "bedrock")
@TickablePriority(stage = TickStage.PRE)
class BedrockCommandTickable(
    private val playerManager: PlayerManager,
    private val commandManager: CherryngineCommandManager,
) : Tickable {

    override fun tick(delta: Duration) {
        for (player in playerManager.onlinePlayers()) {
            val bp = player as? BedrockPlayer ?: continue

            while (true) {
                val command = bp.pendingCommands.poll() ?: break
                commandManager.commandExecutor().executeCommand(bp as CommandSender, command)
            }
        }
    }
}
