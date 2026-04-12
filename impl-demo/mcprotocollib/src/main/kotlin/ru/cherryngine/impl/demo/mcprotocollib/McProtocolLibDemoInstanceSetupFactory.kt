package ru.cherryngine.impl.demo.mcprotocollib

import jakarta.inject.Singleton
import ru.cherryngine.engine.core.player.PlayerManager
import ru.cherryngine.engine.mcprotocollib.McProtocolLibChunkPool
import ru.cherryngine.engine.mcprotocollib.McProtocolLibWorldServiceHandler
import ru.cherryngine.impl.demo.DemoInstanceSetup
import ru.cherryngine.impl.demo.DemoInstanceSetupFactory

@Singleton
class McProtocolLibDemoInstanceSetupFactory(
    private val playerManager: PlayerManager,
    private val chunkPool: McProtocolLibChunkPool,
    private val worldServiceHandler: McProtocolLibWorldServiceHandler,
) : DemoInstanceSetupFactory {
    override fun create(): DemoInstanceSetup =
        McProtocolLibDemoInstanceSetup(playerManager, chunkPool, worldServiceHandler)
}
