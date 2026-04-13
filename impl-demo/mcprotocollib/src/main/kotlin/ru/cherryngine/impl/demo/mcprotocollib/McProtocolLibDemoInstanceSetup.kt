package ru.cherryngine.impl.demo.mcprotocollib

import ru.cherryngine.engine.core.instance.ServerWorld
import ru.cherryngine.engine.core.instance.Tickable
import ru.cherryngine.engine.core.player.PlayerInputProvider
import ru.cherryngine.engine.core.player.PlayerManager
import ru.cherryngine.engine.core.player.PlayerOutputProvider
import ru.cherryngine.engine.mcprotocollib.McProtocolLibChunkPool
import ru.cherryngine.engine.mcprotocollib.McProtocolLibEntityRegistry
import ru.cherryngine.engine.mcprotocollib.McProtocolLibPlayerInputProvider
import ru.cherryngine.engine.mcprotocollib.McProtocolLibPlayerOutputProvider
import ru.cherryngine.engine.mcprotocollib.McProtocolLibViewTickable
import ru.cherryngine.engine.mcprotocollib.McProtocolLibWorldServiceHandler
import ru.cherryngine.impl.demo.DemoInstanceSetup
import ru.cherryngine.impl.demo.view.AxolotlViewFactory
import ru.cherryngine.impl.demo.view.CubeViewFactory

class McProtocolLibDemoInstanceSetup(
    private val playerManager: PlayerManager,
    private val chunkPool: McProtocolLibChunkPool,
    private val worldServiceHandler: McProtocolLibWorldServiceHandler,
    private val serverWorld: ServerWorld,
) : DemoInstanceSetup {
    private val entityRegistry = McProtocolLibEntityRegistry()

    override val axolotlViewFactory: AxolotlViewFactory =
        McProtocolLibAxolotlViewFactory(entityRegistry, playerManager)

    override val cubeViewFactory: CubeViewFactory =
        McProtocolLibCubeViewFactory(entityRegistry)

    override val inputProvider: PlayerInputProvider =
        McProtocolLibPlayerInputProvider(playerManager)

    override val outputProvider: PlayerOutputProvider =
        McProtocolLibPlayerOutputProvider(playerManager)

    override fun createTickables(): List<Tickable> = listOf(
        McProtocolLibViewTickable(playerManager, chunkPool, worldServiceHandler, entityRegistry, serverWorld)
    )
}
