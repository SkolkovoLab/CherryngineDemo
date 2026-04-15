package ru.cherryngine.impl.demo.mcprotocollib

import jakarta.inject.Singleton
import ru.cherryngine.engine.core.commandmanager.CherryngineCommandManager
import ru.cherryngine.engine.core.commandmanager.SArgumentParser
import ru.cherryngine.engine.core.instance.ServerWorld
import ru.cherryngine.engine.core.player.PlayerManager
import ru.cherryngine.engine.mcprotocollib.McProtocolLibChunkPool
import ru.cherryngine.engine.mcprotocollib.McProtocolLibEntityRegistry
import ru.cherryngine.engine.mcprotocollib.McProtocolLibPlayerInputProvider
import ru.cherryngine.engine.mcprotocollib.McProtocolLibPlayerOutputProvider
import ru.cherryngine.engine.mcprotocollib.McProtocolLibViewTickable
import ru.cherryngine.engine.mcprotocollib.McProtocolLibWorldServiceHandler
import ru.cherryngine.impl.demo.PlatformModule
import ru.cherryngine.impl.demo.PlatformProviders
import ru.cherryngine.impl.demo.mcprotocollib.renderer.McProtocolLibAxolotlRenderer
import ru.cherryngine.impl.demo.mcprotocollib.renderer.McProtocolLibCubeRenderer

@Singleton
class McProtocolLibPlatformModule(
    private val playerManager: PlayerManager,
    private val chunkPool: McProtocolLibChunkPool,
    private val worldServiceHandler: McProtocolLibWorldServiceHandler,
    private val parsers: List<SArgumentParser<*>>,
) : PlatformModule {
    override val id = "mcprotocollib"

    override fun createProviders(serverWorld: ServerWorld): PlatformProviders {
        val entityRegistry = McProtocolLibEntityRegistry()
        val commandManager = CherryngineCommandManager(parsers)
        val commandTickable = McProtocolLibCommandTickable(playerManager, commandManager)

        return PlatformProviders(
            inputProvider = McProtocolLibPlayerInputProvider(playerManager),
            outputProvider = McProtocolLibPlayerOutputProvider(playerManager),
            axolotlRenderer = McProtocolLibAxolotlRenderer(entityRegistry, playerManager),
            cubeRenderer = McProtocolLibCubeRenderer(entityRegistry),
            commandManager = commandManager,
            tickables = listOf(
                McProtocolLibViewTickable(playerManager, chunkPool, worldServiceHandler, entityRegistry, serverWorld),
                commandTickable,
            ),
        )
    }
}
