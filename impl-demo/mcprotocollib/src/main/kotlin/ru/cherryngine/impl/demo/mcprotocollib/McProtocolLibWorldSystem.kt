package ru.cherryngine.impl.demo.mcprotocollib

import jakarta.inject.Singleton
import ru.cherryngine.impl.demo.DemoInit
import ru.cherryngine.impl.demo.GameWorldProvider
import ru.cherryngine.lib.minecraft.registry.Registries
import ru.cherryngine.lib.minecraft.registry.keys.DimensionTypes
import ru.cherryngine.lib.polar.PolarWorldGenerator
import ru.cherryngine.lib.world.LayerEntry

@Singleton
class McProtocolLibWorldSystem(
    demoWorlds: GameWorldProvider,
    demoInit: DemoInit,
) {
    init {
        val serverWorld = demoInit.serverWorld
        val overworld = Registries.dimensionType[DimensionTypes.OVERWORLD].value

        val polarFileNames = mapOf(
            "normal" to "de_cache_normal",
            "winter" to "de_cache_winter",
            "dust" to "de_dust2",
            "lobby" to "lobby",
            "street" to "street",
            "apart1" to "apart1",
            "apart2" to "apart2",
        )

        val mutableWorlds = setOf("apart2")

        for (worldName in demoWorlds.worldNames) {
            val fileName = polarFileNames[worldName] ?: continue
            val resource = javaClass.getResource("/$fileName.polar") ?: continue
            val bytes = resource.readBytes()

            val layer = if (worldName in mutableWorlds) {
                PolarWorldGenerator.loadAsMutableLayer(bytes, overworld, worldName)
            } else {
                PolarWorldGenerator.loadAsLayer(bytes, overworld, worldName)
            }

            val priority = if (worldName in demoWorlds.apartNames) 10 else 0
            serverWorld.registerLayer(worldName, LayerEntry(layer, priority))
        }

        serverWorld.dimensionType = overworld
    }
}
