package ru.cherryngine.impl.demo

import ru.cherryngine.engine.core.instance.InstanceSetup
import ru.cherryngine.engine.core.instance.InstanceSetupFactory
import ru.cherryngine.engine.core.instance.ServerWorld
import ru.cherryngine.engine.core.player.PlayerInputProvider
import ru.cherryngine.engine.core.player.PlayerOutputProvider
import ru.cherryngine.impl.demo.renderer.AxolotlRenderer
import ru.cherryngine.impl.demo.renderer.CubeRenderer

interface DemoInstanceSetup : InstanceSetup {
    val axolotlRenderer: AxolotlRenderer
    val cubeRenderer: CubeRenderer
    val inputProvider: PlayerInputProvider
    val outputProvider: PlayerOutputProvider
}

interface DemoInstanceSetupFactory : InstanceSetupFactory<DemoInstanceSetup> {
    fun create(serverWorld: ServerWorld): DemoInstanceSetup
}
