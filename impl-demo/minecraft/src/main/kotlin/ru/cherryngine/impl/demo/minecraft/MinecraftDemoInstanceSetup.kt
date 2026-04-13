package ru.cherryngine.impl.demo.minecraft

import ru.cherryngine.engine.core.instance.ServerWorld
import ru.cherryngine.engine.core.instance.Tickable
import ru.cherryngine.engine.core.player.PlayerInputProvider
import ru.cherryngine.engine.core.player.PlayerManager
import ru.cherryngine.engine.core.player.PlayerOutputProvider
import ru.cherryngine.engine.minecraft.ChunkPool
import ru.cherryngine.engine.minecraft.MinecraftViewTickable
import ru.cherryngine.engine.minecraft.MinecraftWorldServiceHandler
import ru.cherryngine.engine.minecraft.entity.McEntityRegistry
import ru.cherryngine.engine.minecraft.player.MinecraftPlayerInputProvider
import ru.cherryngine.engine.minecraft.player.MinecraftPlayerOutputProvider
import ru.cherryngine.impl.demo.DemoInstanceSetup
import ru.cherryngine.impl.demo.view.AxolotlViewFactory
import ru.cherryngine.impl.demo.view.CubeViewFactory

class MinecraftDemoInstanceSetup(
    private val playerManager: PlayerManager,
    private val chunkPool: ChunkPool,
    private val worldServiceHandler: MinecraftWorldServiceHandler,
    private val serverWorld: ServerWorld,
) : DemoInstanceSetup {
    private val mcEntityRegistry = McEntityRegistry()

    override val axolotlViewFactory: AxolotlViewFactory =
        MinecraftAxolotlViewFactory(mcEntityRegistry, playerManager)

    override val cubeViewFactory: CubeViewFactory =
        MinecraftCubeViewFactory(mcEntityRegistry)

    override val inputProvider: PlayerInputProvider =
        MinecraftPlayerInputProvider(playerManager)

    override val outputProvider: PlayerOutputProvider =
        MinecraftPlayerOutputProvider(playerManager)

    override fun createTickables(): List<Tickable> = listOf(
        MinecraftViewTickable(playerManager, chunkPool, worldServiceHandler, mcEntityRegistry, serverWorld)
    )
}
