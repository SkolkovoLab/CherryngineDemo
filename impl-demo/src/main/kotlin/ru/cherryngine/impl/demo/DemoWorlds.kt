package ru.cherryngine.impl.demo

import jakarta.inject.Singleton
import ru.cherryngine.lib.minecraft.registry.Registries
import ru.cherryngine.lib.minecraft.registry.keys.DimensionTypes
import ru.cherryngine.lib.minecraft.registry.types.DimensionType
import ru.cherryngine.lib.polar.PolarWorldGenerator
import ru.cherryngine.lib.world.ImmutableLayer

@Singleton
class DemoWorlds {
    val overworld: DimensionType = Registries.dimensionType[DimensionTypes.OVERWORLD].value

    private fun loadLayer(name: String): ImmutableLayer =
        PolarWorldGenerator.loadAsLayer(
            javaClass.getResource("/${name}.polar")!!.readBytes(),
            overworld,
            name,
        )

    val normalLayer = loadLayer("de_cache_normal")
    val winterLayer = loadLayer("de_cache_winter")
    val dustLayer = loadLayer("de_dust2")
    val lobbyLayer = loadLayer("lobby")

    val streetLayer = loadLayer("street")
    val apart1Layer = loadLayer("apart1")
    val apart2Layer = loadLayer("apart2")

    val layers = mapOf(
        "normal" to normalLayer,
        "winter" to winterLayer,
        "dust" to dustLayer,
        "lobby" to lobbyLayer,
        "street" to streetLayer,
        "apart1" to apart1Layer,
        "apart2" to apart2Layer,
    )
}
