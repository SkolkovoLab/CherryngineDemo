package ru.cherryngine.impl.demo.minecraft

import jakarta.inject.Singleton
import ru.cherryngine.engine.minecraft.MinecraftWorldServiceHandler
import ru.cherryngine.engine.physics.terrain.LayerWithContext
import ru.cherryngine.engine.physics.terrain.TerrainLayerProvider

@Singleton
class MinecraftTerrainLayerProvider(
    private val worldServiceHandler: MinecraftWorldServiceHandler,
) : TerrainLayerProvider {
    override fun collectLayers(): List<LayerWithContext> {
        val dimensionType = worldServiceHandler.dimensionType ?: return emptyList()
        val result = mutableListOf<LayerWithContext>()
        for ((contextID, layers) in worldServiceHandler.getLayersByContext()) {
            for (layerEntry in layers) {
                result.add(LayerWithContext(layerEntry, setOf(contextID), dimensionType))
            }
        }
        return result
    }
}
