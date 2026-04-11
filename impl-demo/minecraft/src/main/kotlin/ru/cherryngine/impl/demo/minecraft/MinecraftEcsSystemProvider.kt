package ru.cherryngine.impl.demo.minecraft

import jakarta.inject.Singleton
import ru.cherryngine.engine.core.PlayerManager
import ru.cherryngine.engine.minecraft.ChunkPool
import ru.cherryngine.engine.minecraft.MinecraftWorldServiceHandler
import ru.cherryngine.engine.minecraft.entity.McEntityRegistry
import ru.cherryngine.engine.minecraft.systems.McEntityBeginTickSystem
import ru.cherryngine.engine.minecraft.systems.McEntityEndTickSystem
import ru.cherryngine.engine.minecraft.systems.MinecraftViewSystem
import ru.cherryngine.impl.demo.DemoEcsSystemProvider
import ru.cherryngine.impl.demo.SystemConfiguration

@Singleton
class MinecraftEcsSystemProvider(
    private val playerManager: PlayerManager,
    private val chunkPool: ChunkPool,
    private val worldServiceHandler: MinecraftWorldServiceHandler,
    private val mcEntityRegistry: McEntityRegistry,
) : DemoEcsSystemProvider {
    override fun addEarlySystems(cfg: SystemConfiguration) {
        cfg.add(McEntityBeginTickSystem(mcEntityRegistry))
    }

    override fun addLateSystems(cfg: SystemConfiguration) {
        cfg.add(MinecraftViewSystem(playerManager, chunkPool, worldServiceHandler, mcEntityRegistry))
        cfg.add(McEntityEndTickSystem(mcEntityRegistry))
    }
}
