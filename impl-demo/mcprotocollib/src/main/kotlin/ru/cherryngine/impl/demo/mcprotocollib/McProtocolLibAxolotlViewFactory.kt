package ru.cherryngine.impl.demo.mcprotocollib

import ru.cherryngine.engine.core.PlayerManager
import ru.cherryngine.engine.mcprotocollib.McProtocolLibEntityRegistry
import ru.cherryngine.impl.demo.view.AxolotlView
import ru.cherryngine.impl.demo.view.AxolotlViewFactory

class McProtocolLibAxolotlViewFactory(
    private val entityRegistry: McProtocolLibEntityRegistry,
    private val playerManager: PlayerManager,
) : AxolotlViewFactory {
    override fun create(): AxolotlView =
        McProtocolLibAxolotlView(entityRegistry, playerManager)
}
