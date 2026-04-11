package ru.cherryngine.impl.demo.minecraft

import ru.cherryngine.engine.minecraft.entity.McEntityRegistry
import ru.cherryngine.impl.demo.view.CubeView
import ru.cherryngine.impl.demo.view.CubeViewFactory

class MinecraftCubeViewFactory(
    private val mcEntityRegistry: McEntityRegistry,
) : CubeViewFactory {
    override fun create(): CubeView = MinecraftCubeView(mcEntityRegistry)
}
