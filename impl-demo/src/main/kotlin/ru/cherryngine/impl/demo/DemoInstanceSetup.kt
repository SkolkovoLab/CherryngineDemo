package ru.cherryngine.impl.demo

import ru.cherryngine.engine.core.instance.InstanceSetup
import ru.cherryngine.engine.core.instance.InstanceSetupFactory
import ru.cherryngine.engine.core.instance.ServerWorld
import ru.cherryngine.engine.core.player.PlayerInputProvider
import ru.cherryngine.engine.core.player.PlayerOutputProvider
import ru.cherryngine.impl.demo.view.AxolotlViewFactory
import ru.cherryngine.impl.demo.view.CubeViewFactory

interface DemoInstanceSetup : InstanceSetup {
    val axolotlViewFactory: AxolotlViewFactory
    val cubeViewFactory: CubeViewFactory
    val inputProvider: PlayerInputProvider
    val outputProvider: PlayerOutputProvider
}

interface DemoInstanceSetupFactory : InstanceSetupFactory<DemoInstanceSetup> {
    fun create(serverWorld: ServerWorld): DemoInstanceSetup
}
