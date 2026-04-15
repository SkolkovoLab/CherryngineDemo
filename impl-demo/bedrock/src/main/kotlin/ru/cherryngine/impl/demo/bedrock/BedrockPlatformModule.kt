package ru.cherryngine.impl.demo.bedrock

import jakarta.inject.Singleton
import ru.cherryngine.engine.bedrock.BedrockPlayerInputProvider
import ru.cherryngine.engine.bedrock.BedrockPlayerOutputProvider
import ru.cherryngine.engine.bedrock.BedrockWorldServiceHandler
import ru.cherryngine.engine.bedrock.entity.BedrockEntityRegistry
import ru.cherryngine.engine.bedrock.world.BedrockBlockMapping
import ru.cherryngine.engine.bedrock.world.BedrockViewTickable
import ru.cherryngine.engine.core.commandmanager.CherryngineCommandManager
import ru.cherryngine.engine.core.commandmanager.SArgumentParser
import ru.cherryngine.engine.core.instance.ServerWorld
import ru.cherryngine.engine.core.player.PlayerManager
import ru.cherryngine.impl.demo.PlatformModule
import ru.cherryngine.impl.demo.PlatformProviders
import ru.cherryngine.impl.demo.bedrock.renderer.BedrockAxolotlRenderer
import ru.cherryngine.impl.demo.bedrock.renderer.BedrockCubeRenderer

@Singleton
class BedrockPlatformModule(
    private val playerManager: PlayerManager,
    private val worldServiceHandler: BedrockWorldServiceHandler,
    private val blockMapping: BedrockBlockMapping,
    private val parsers: List<SArgumentParser<*>>,
) : PlatformModule {
    override val id = "bedrock"

    override fun createProviders(serverWorld: ServerWorld): PlatformProviders {
        val entityRegistry = BedrockEntityRegistry()
        val commandManager = CherryngineCommandManager(parsers)
        val commandTickable = BedrockCommandTickable(playerManager, commandManager)

        return PlatformProviders(
            inputProvider = BedrockPlayerInputProvider(playerManager),
            outputProvider = BedrockPlayerOutputProvider(playerManager),
            axolotlRenderer = BedrockAxolotlRenderer(entityRegistry, playerManager),
            cubeRenderer = BedrockCubeRenderer(entityRegistry, blockMapping),
            commandManager = commandManager,
            tickables = listOf(
                BedrockViewTickable(playerManager, worldServiceHandler, blockMapping, serverWorld, entityRegistry),
                commandTickable,
            ),
        )
    }
}
