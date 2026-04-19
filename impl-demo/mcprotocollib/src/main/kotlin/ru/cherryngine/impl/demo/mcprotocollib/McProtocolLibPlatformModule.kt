package ru.cherryngine.impl.demo.mcprotocollib

import jakarta.inject.Singleton
import ru.cherryngine.engine.core.instance.Instance
import ru.cherryngine.engine.core.instance.ServerWorld
import ru.cherryngine.engine.core.player.PlayerManager
import ru.cherryngine.engine.mcprotocollib.McProtocolLibPlayerInputProvider
import ru.cherryngine.engine.mcprotocollib.McProtocolLibPlayerOutputProvider
import ru.cherryngine.impl.demo.PlatformModule
import ru.cherryngine.impl.demo.PlatformProviders

@Singleton
class McProtocolLibPlatformModule(
    private val playerManager: PlayerManager,
) : PlatformModule {
    override val id = "mcprotocollib"

    override fun createProviders(instance: Instance, serverWorld: ServerWorld) = PlatformProviders(
        inputProvider = McProtocolLibPlayerInputProvider(playerManager),
        outputProvider = McProtocolLibPlayerOutputProvider(playerManager),
    )
}
