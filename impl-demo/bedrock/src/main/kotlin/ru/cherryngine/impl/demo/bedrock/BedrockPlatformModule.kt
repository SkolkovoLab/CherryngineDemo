package ru.cherryngine.impl.demo.bedrock

import jakarta.inject.Singleton
import ru.cherryngine.engine.core.instance.Instance
import ru.cherryngine.engine.core.world.ServerWorld
import ru.cherryngine.engine.core.player.PlayerManager
import ru.cherryngine.impl.demo.PlatformModule
import ru.cherryngine.impl.demo.PlatformProviders
import ru.cherryngine.platform.minecraft.bedrock.BedrockPlayerInputProvider
import ru.cherryngine.platform.minecraft.bedrock.BedrockPlayerOutputProvider

@Singleton
class BedrockPlatformModule(
    private val playerManager: PlayerManager,
) : PlatformModule {
    override val id = "bedrock"

    override fun createProviders(instance: Instance, serverWorld: ServerWorld) = PlatformProviders(
        inputProvider = BedrockPlayerInputProvider(playerManager),
        outputProvider = BedrockPlayerOutputProvider(playerManager),
    )
}
