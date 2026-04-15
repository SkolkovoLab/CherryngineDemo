package ru.cherryngine.impl.demo

import ru.cherryngine.engine.core.commandmanager.CherryngineCommandManager
import ru.cherryngine.engine.core.instance.ServerWorld
import ru.cherryngine.engine.core.instance.Tickable
import ru.cherryngine.engine.core.player.PlayerInputProvider
import ru.cherryngine.engine.core.player.PlayerOutputProvider
import ru.cherryngine.impl.demo.renderer.AxolotlRenderer
import ru.cherryngine.impl.demo.renderer.CubeRenderer

interface PlatformModule {
    val id: String
    fun createProviders(serverWorld: ServerWorld): PlatformProviders
}

data class PlatformProviders(
    val inputProvider: PlayerInputProvider,
    val outputProvider: PlayerOutputProvider,
    val axolotlRenderer: AxolotlRenderer,
    val cubeRenderer: CubeRenderer,
    val commandManager: CherryngineCommandManager,
    val tickables: List<Tickable>,
)
