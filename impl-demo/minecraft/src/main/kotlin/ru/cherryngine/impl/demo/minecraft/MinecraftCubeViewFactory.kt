package ru.cherryngine.impl.demo.minecraft

import jakarta.inject.Singleton
import ru.cherryngine.engine.minecraft.entity.McEntityRegistry
import ru.cherryngine.impl.demo.view.CubeView
import ru.cherryngine.impl.demo.view.CubeViewFactory

@Singleton
class MinecraftCubeViewFactory(
    private val mcEntityRegistry: McEntityRegistry,
) : CubeViewFactory {
    override fun create(): CubeView = MinecraftCubeView(mcEntityRegistry)
}
