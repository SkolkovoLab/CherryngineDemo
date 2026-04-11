package ru.cherryngine.impl.demo.mcprotocollib

import ru.cherryngine.engine.mcprotocollib.McProtocolLibEntityRegistry
import ru.cherryngine.impl.demo.view.CubeView
import ru.cherryngine.impl.demo.view.CubeViewFactory

class McProtocolLibCubeViewFactory(
    private val entityRegistry: McProtocolLibEntityRegistry,
) : CubeViewFactory {
    override fun create(): CubeView =
        McProtocolLibCubeView(entityRegistry)
}
