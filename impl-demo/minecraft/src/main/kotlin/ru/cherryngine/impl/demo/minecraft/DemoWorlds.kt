package ru.cherryngine.impl.demo.minecraft

import jakarta.inject.Singleton
import ru.cherryngine.lib.minecraft.registry.Registries
import ru.cherryngine.lib.minecraft.registry.keys.DimensionTypes
import ru.cherryngine.lib.minecraft.registry.types.DimensionType
import ru.cherryngine.lib.polar.PolarWorldGenerator
import ru.cherryngine.lib.world.ImmutableLayer
import ru.cherryngine.lib.world.Layer
import ru.cherryngine.lib.world.MutableLayer

@Singleton
class DemoWorlds {
    val apartNames = setOf("apart1", "apart2")

    val overworld: DimensionType = Registries.dimensionType[DimensionTypes.OVERWORLD].value

    private fun loadImmutableLayer(name: String): ImmutableLayer =
        PolarWorldGenerator.loadAsLayer(
            javaClass.getResource("/${name}.polar")!!.readBytes(),
            overworld,
            name,
        )

    private fun loadMutableLayer(name: String): MutableLayer =
        PolarWorldGenerator.loadAsMutableLayer(
            javaClass.getResource("/${name}.polar")!!.readBytes(),
            overworld,
            name,
        )

    val gmConstructLayer = loadImmutableLayer("gm_construct")
    val apart1Layer = loadImmutableLayer("apart1")
    val apart2Layer = loadMutableLayer("apart2")

    val layers: Map<String, Layer> = mapOf(
        "gm_construct" to gmConstructLayer,
        "apart1" to apart1Layer,
        "apart2" to apart2Layer,
    )
}
