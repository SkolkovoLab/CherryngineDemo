package ru.cherryngine.impl.demo

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

    val normalLayer = loadImmutableLayer("de_cache_normal")
    val winterLayer = loadImmutableLayer("de_cache_winter")
    val dustLayer = loadImmutableLayer("de_dust2")
    val lobbyLayer = loadImmutableLayer("lobby")

    val streetLayer = loadImmutableLayer("street")
    val apart1Layer = loadImmutableLayer("apart1")
    val apart2Layer = loadMutableLayer("apart2")

    val layers: Map<String, Layer> = mapOf(
        "normal" to normalLayer,
        "winter" to winterLayer,
        "dust" to dustLayer,
        "lobby" to lobbyLayer,
        "street" to streetLayer,
        "apart1" to apart1Layer,
        "apart2" to apart2Layer,
    )
}
