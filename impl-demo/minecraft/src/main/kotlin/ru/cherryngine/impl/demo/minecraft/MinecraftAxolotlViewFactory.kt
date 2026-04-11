package ru.cherryngine.impl.demo.minecraft

import ru.cherryngine.engine.core.PlayerManager
import ru.cherryngine.engine.minecraft.entity.McEntityRegistry
import ru.cherryngine.impl.demo.view.AxolotlView
import ru.cherryngine.impl.demo.view.AxolotlViewFactory

class MinecraftAxolotlViewFactory(
    private val mcEntityRegistry: McEntityRegistry,
    private val playerManager: PlayerManager,
) : AxolotlViewFactory {
    override fun create(): AxolotlView = MinecraftAxolotlView(mcEntityRegistry, playerManager)
}
