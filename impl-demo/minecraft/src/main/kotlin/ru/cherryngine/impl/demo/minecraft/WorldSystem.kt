package ru.cherryngine.impl.demo.minecraft

import jakarta.inject.Singleton
import ru.cherryngine.engine.minecraft.MinecraftWorldServiceHandler
import ru.cherryngine.lib.world.LayerEntry

@Singleton
class WorldSystem(
    demoWorlds: DemoWorlds,
    worldServiceHandler: MinecraftWorldServiceHandler,
) {
    init {
        demoWorlds.layers.forEach { (worldName, layer) ->
            val priority = if (worldName in demoWorlds.apartNames) 10 else 0
            worldServiceHandler.registerLayer(worldName, LayerEntry(layer, priority))
        }
        worldServiceHandler.dimensionType = demoWorlds.overworld
    }
}
