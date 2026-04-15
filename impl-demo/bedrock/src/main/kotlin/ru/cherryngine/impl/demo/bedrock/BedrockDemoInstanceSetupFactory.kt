package ru.cherryngine.impl.demo.bedrock

import jakarta.inject.Singleton
import ru.cherryngine.engine.core.commandmanager.SArgumentParser
import ru.cherryngine.engine.core.instance.ServerWorld
import ru.cherryngine.engine.core.player.PlayerManager
import ru.cherryngine.engine.bedrock.BedrockWorldServiceHandler
import ru.cherryngine.engine.bedrock.world.BedrockBlockMapping
import ru.cherryngine.impl.demo.DemoInstanceSetup
import ru.cherryngine.impl.demo.DemoInstanceSetupFactory

@Singleton
class BedrockDemoInstanceSetupFactory(
    private val playerManager: PlayerManager,
    private val worldServiceHandler: BedrockWorldServiceHandler,
    private val blockMapping: BedrockBlockMapping,
    private val parsers: List<SArgumentParser<*>>,
) : DemoInstanceSetupFactory {
    override fun create(serverWorld: ServerWorld): DemoInstanceSetup =
        BedrockDemoInstanceSetup(playerManager, worldServiceHandler, blockMapping, serverWorld, parsers)
}
