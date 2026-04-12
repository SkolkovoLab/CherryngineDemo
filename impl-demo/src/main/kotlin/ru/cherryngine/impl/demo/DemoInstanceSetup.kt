package ru.cherryngine.impl.demo

import ru.cherryngine.engine.core.instance.InstanceSetup
import ru.cherryngine.engine.core.instance.InstanceSetupFactory
import ru.cherryngine.engine.core.instance.ServerWorld
import ru.cherryngine.impl.demo.view.AxolotlViewFactory
import ru.cherryngine.impl.demo.view.CubeViewFactory

interface DemoInstanceSetup : InstanceSetup {
    val axolotlViewFactory: AxolotlViewFactory
    val cubeViewFactory: CubeViewFactory
}

interface DemoInstanceSetupFactory : InstanceSetupFactory<DemoInstanceSetup> {
    fun create(serverWorld: ServerWorld): DemoInstanceSetup
    override fun create(): DemoInstanceSetup = create(ServerWorld())
}
