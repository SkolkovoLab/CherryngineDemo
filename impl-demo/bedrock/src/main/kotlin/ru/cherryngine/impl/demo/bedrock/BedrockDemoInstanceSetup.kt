package ru.cherryngine.impl.demo.bedrock

import ru.cherryngine.engine.bedrock.BedrockPlayerInputProvider
import ru.cherryngine.engine.bedrock.BedrockPlayerOutputProvider
import ru.cherryngine.engine.bedrock.BedrockWorldServiceHandler
import ru.cherryngine.engine.bedrock.entity.BedrockEntityRegistry
import ru.cherryngine.engine.bedrock.world.BedrockBlockMapping
import ru.cherryngine.engine.bedrock.world.BedrockViewTickable
import ru.cherryngine.engine.core.commandmanager.CherryngineCommandManager
import ru.cherryngine.engine.core.commandmanager.SArgumentParser
import ru.cherryngine.engine.core.instance.ServerWorld
import ru.cherryngine.engine.core.instance.Tickable
import ru.cherryngine.engine.core.player.PlayerInputProvider
import ru.cherryngine.engine.core.player.PlayerManager
import ru.cherryngine.engine.core.player.PlayerOutputProvider
import ru.cherryngine.impl.demo.DemoInstanceSetup
import ru.cherryngine.impl.demo.bedrock.renderer.BedrockAxolotlRenderer
import ru.cherryngine.impl.demo.bedrock.renderer.BedrockCubeRenderer
import ru.cherryngine.impl.demo.renderer.AxolotlRenderer
import ru.cherryngine.impl.demo.renderer.CubeRenderer

class BedrockDemoInstanceSetup(
    private val playerManager: PlayerManager,
    private val worldServiceHandler: BedrockWorldServiceHandler,
    private val blockMapping: BedrockBlockMapping,
    private val serverWorld: ServerWorld,
    parsers: List<SArgumentParser<*>>,
) : DemoInstanceSetup {
    private val entityRegistry = BedrockEntityRegistry()

    override val axolotlRenderer: AxolotlRenderer = BedrockAxolotlRenderer(entityRegistry, playerManager)
    override val cubeRenderer: CubeRenderer = BedrockCubeRenderer(entityRegistry, blockMapping)
    override val inputProvider: PlayerInputProvider = BedrockPlayerInputProvider(playerManager)
    override val outputProvider: PlayerOutputProvider = BedrockPlayerOutputProvider(playerManager)
    override val commandManager = CherryngineCommandManager(parsers)

    override fun createTickables(): List<Tickable> = listOf(
        BedrockViewTickable(playerManager, worldServiceHandler, blockMapping, serverWorld, entityRegistry),
        BedrockCommandTickable(playerManager, commandManager),
    )
}
