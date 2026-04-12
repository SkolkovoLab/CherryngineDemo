package ru.cherryngine.impl.demo.minecraft

import jakarta.inject.Singleton
import ru.cherryngine.impl.demo.DemoInit
import ru.cherryngine.lib.world.LayerEntry

@Singleton
class WorldSystem(
    demoWorlds: DemoWorlds,
    demoInit: DemoInit,
) {
    init {
        val serverWorld = demoInit.serverWorld
        demoWorlds.layers.forEach { (worldName, layer) ->
            val priority = if (worldName in demoWorlds.apartNames) 10 else 0
            val entry = LayerEntry(layer, priority)
            serverWorld.registerLayer(worldName, entry)
        }
        serverWorld.dimensionType = demoWorlds.overworld
    }
}
