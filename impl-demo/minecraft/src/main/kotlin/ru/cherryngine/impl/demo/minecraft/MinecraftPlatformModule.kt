package ru.cherryngine.impl.demo.minecraft

import jakarta.inject.Singleton
import ru.cherryngine.engine.core.instance.Instance
import ru.cherryngine.engine.core.instance.ServerWorld
import ru.cherryngine.engine.core.player.PlayerManager
import ru.cherryngine.engine.minecraft.player.MinecraftPlayerInputProvider
import ru.cherryngine.engine.minecraft.player.MinecraftPlayerOutputProvider
import ru.cherryngine.impl.demo.PlatformModule
import ru.cherryngine.impl.demo.PlatformProviders

@Singleton
class MinecraftPlatformModule(
    private val playerManager: PlayerManager,
) : PlatformModule {
    override val id = "minecraft"

    override fun createProviders(instance: Instance, serverWorld: ServerWorld) = PlatformProviders(
        inputProvider = MinecraftPlayerInputProvider(playerManager),
        outputProvider = MinecraftPlayerOutputProvider(playerManager),
    )
}
