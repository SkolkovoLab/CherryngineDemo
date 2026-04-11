package ru.cherryngine.impl.demo.systems

import ru.cherryngine.engine.minecraft.MinecraftWorldServiceHandler
import ru.cherryngine.impl.demo.DemoWorlds
import ru.cherryngine.lib.world.LayerEntry

class WorldSystem(
    demoWorlds: DemoWorlds,
    worldServiceHandler: MinecraftWorldServiceHandler,
) {
    init {
        demoWorlds.layers.forEach { (worldName, layer) ->
            val priority = if (worldName in setOf("apart1", "apart2")) 10 else 0
            worldServiceHandler.registerLayer(worldName, LayerEntry(layer, priority))
        }
        worldServiceHandler.dimensionType = demoWorlds.overworld
    }
}
