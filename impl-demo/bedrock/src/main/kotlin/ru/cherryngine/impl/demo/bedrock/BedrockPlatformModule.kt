package ru.cherryngine.impl.demo.bedrock

import jakarta.inject.Singleton
import ru.cherryngine.engine.bedrock.BedrockPlayerInputProvider
import ru.cherryngine.engine.bedrock.BedrockPlayerOutputProvider
import ru.cherryngine.engine.core.instance.Instance
import ru.cherryngine.engine.core.instance.ServerWorld
import ru.cherryngine.engine.core.player.PlayerManager
import ru.cherryngine.impl.demo.PlatformModule
import ru.cherryngine.impl.demo.PlatformProviders

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
