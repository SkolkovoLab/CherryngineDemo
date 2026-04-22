package ru.cherryngine.impl.demo

import ru.cherryngine.engine.core.instance.Instance
import ru.cherryngine.engine.core.world.ServerWorld
import ru.cherryngine.engine.core.player.PlayerInputProvider
import ru.cherryngine.engine.core.player.PlayerOutputProvider

interface PlatformModule {
    val id: String
    fun createProviders(instance: Instance, serverWorld: ServerWorld): PlatformProviders
}

data class PlatformProviders(
    val inputProvider: PlayerInputProvider,
    val outputProvider: PlayerOutputProvider,
)
