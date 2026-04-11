package ru.cherryngine.impl.demo.mcprotocollib

import ru.cherryngine.engine.core.PlayerManager
import ru.cherryngine.engine.core.Tickable
import ru.cherryngine.engine.mcprotocollib.McProtocolLibChunkPool
import ru.cherryngine.engine.mcprotocollib.McProtocolLibEntityRegistry
import ru.cherryngine.engine.mcprotocollib.McProtocolLibViewTickable
import ru.cherryngine.engine.mcprotocollib.McProtocolLibWorldServiceHandler
import ru.cherryngine.impl.demo.DemoInstanceSetup
import ru.cherryngine.impl.demo.view.AxolotlViewFactory
import ru.cherryngine.impl.demo.view.CubeViewFactory

class McProtocolLibDemoInstanceSetup(
    private val playerManager: PlayerManager,
    private val chunkPool: McProtocolLibChunkPool,
    private val worldServiceHandler: McProtocolLibWorldServiceHandler,
) : DemoInstanceSetup {
    private val entityRegistry = McProtocolLibEntityRegistry()

    override val axolotlViewFactory: AxolotlViewFactory =
        McProtocolLibAxolotlViewFactory(entityRegistry, playerManager)

    override val cubeViewFactory: CubeViewFactory =
        McProtocolLibCubeViewFactory(entityRegistry)

    override fun createTickables(): List<Tickable> = listOf(
        McProtocolLibViewTickable(playerManager, chunkPool, worldServiceHandler, entityRegistry)
    )
}
