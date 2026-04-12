package ru.cherryngine.impl.demo

import ru.cherryngine.engine.core.instance.InstanceSetup
import ru.cherryngine.engine.core.instance.InstanceSetupFactory
import ru.cherryngine.impl.demo.view.AxolotlViewFactory
import ru.cherryngine.impl.demo.view.CubeViewFactory

interface DemoInstanceSetup : InstanceSetup {
    val axolotlViewFactory: AxolotlViewFactory
    val cubeViewFactory: CubeViewFactory
}

interface DemoInstanceSetupFactory : InstanceSetupFactory<DemoInstanceSetup>
