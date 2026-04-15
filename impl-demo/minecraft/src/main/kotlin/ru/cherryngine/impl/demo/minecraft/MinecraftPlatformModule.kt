package ru.cherryngine.impl.demo.minecraft

import jakarta.inject.Singleton
import ru.cherryngine.engine.core.commandmanager.CherryngineCommandManager
import ru.cherryngine.engine.core.commandmanager.SArgumentParser
import ru.cherryngine.engine.core.instance.ServerWorld
import ru.cherryngine.engine.core.player.PlayerManager
import ru.cherryngine.engine.minecraft.ChunkPool
import ru.cherryngine.engine.minecraft.MinecraftViewTickable
import ru.cherryngine.engine.minecraft.MinecraftWorldServiceHandler
import ru.cherryngine.engine.minecraft.entity.McEntityRegistry
import ru.cherryngine.engine.minecraft.player.MinecraftPlayerInputProvider
import ru.cherryngine.engine.minecraft.player.MinecraftPlayerOutputProvider
import ru.cherryngine.impl.demo.PlatformModule
import ru.cherryngine.impl.demo.PlatformProviders
import ru.cherryngine.impl.demo.minecraft.renderer.MinecraftAxolotlRenderer
import ru.cherryngine.impl.demo.minecraft.renderer.MinecraftCubeRenderer

@Singleton
class MinecraftPlatformModule(
    private val playerManager: PlayerManager,
    private val chunkPool: ChunkPool,
    private val worldServiceHandler: MinecraftWorldServiceHandler,
    private val parsers: List<SArgumentParser<*>>,
) : PlatformModule {
    override val id = "minecraft"

    override fun createProviders(serverWorld: ServerWorld): PlatformProviders {
        val mcEntityRegistry = McEntityRegistry()
        val commandManager = CherryngineCommandManager(parsers)
        val commandTickable = MinecraftCommandTickable(playerManager, commandManager)

        return PlatformProviders(
            inputProvider = MinecraftPlayerInputProvider(playerManager),
            outputProvider = MinecraftPlayerOutputProvider(playerManager),
            axolotlRenderer = MinecraftAxolotlRenderer(mcEntityRegistry, playerManager),
            cubeRenderer = MinecraftCubeRenderer(mcEntityRegistry),
            commandManager = commandManager,
            tickables = listOf(
                MinecraftViewTickable(playerManager, chunkPool, worldServiceHandler, mcEntityRegistry, serverWorld),
                commandTickable,
            ),
        )
    }
}
