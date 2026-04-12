package ru.cherryngine.impl.demo.minecraft

import jakarta.inject.Singleton
import ru.cherryngine.engine.core.instance.ServerWorld
import ru.cherryngine.engine.core.player.PlayerManager
import ru.cherryngine.engine.minecraft.ChunkPool
import ru.cherryngine.engine.minecraft.MinecraftWorldServiceHandler
import ru.cherryngine.impl.demo.DemoInstanceSetup
import ru.cherryngine.impl.demo.DemoInstanceSetupFactory

@Singleton
class MinecraftDemoInstanceSetupFactory(
    private val playerManager: PlayerManager,
    private val chunkPool: ChunkPool,
    private val worldServiceHandler: MinecraftWorldServiceHandler,
) : DemoInstanceSetupFactory {
    override fun create(serverWorld: ServerWorld): DemoInstanceSetup =
        MinecraftDemoInstanceSetup(playerManager, chunkPool, worldServiceHandler, serverWorld)
}
