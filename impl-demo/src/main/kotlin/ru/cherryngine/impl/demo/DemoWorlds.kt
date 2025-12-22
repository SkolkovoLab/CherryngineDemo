package ru.cherryngine.impl.demo

import jakarta.inject.Singleton
import ru.cherryngine.engine.core.world.world.LayerWorldViewableProviderImpl
import ru.cherryngine.engine.core.world.world.WorldViewableProviderImpl
import ru.cherryngine.lib.minecraft.registry.Registries
import ru.cherryngine.lib.minecraft.registry.keys.DimensionTypes
import ru.cherryngine.lib.minecraft.registry.types.DimensionType
import ru.cherryngine.lib.polar.PolarWorldGenerator
import ru.cherryngine.lib.world.Chunk
import ru.cherryngine.lib.world.World

@Singleton
class DemoWorlds {
    private fun loadChunks(dimensionType: DimensionType, name: String): World {
        val chunks = PolarWorldGenerator.loadChunks(
            javaClass.getResource("/${name}.polar")!!.readBytes(),
        ).mapValues { (_, it) ->
            Chunk(it.sections, it.blockEntities, it.lightData, dimensionType)
        }
        return World(dimensionType, chunks)
    }

    val dtOverworld = Registries.dimensionType[DimensionTypes.OVERWORLD].value
    val normalWorld = WorldViewableProviderImpl(loadChunks(dtOverworld, "de_cache_normal"))
    val winterWorld = WorldViewableProviderImpl(loadChunks(dtOverworld, "de_cache_winter"))
    val dustWorld = WorldViewableProviderImpl(loadChunks(dtOverworld, "de_dust2"))
    val lobbyWorld = WorldViewableProviderImpl(loadChunks(dtOverworld, "lobby"))

    val streetWorld = WorldViewableProviderImpl(loadChunks(dtOverworld, "street"))
    val apart1World = LayerWorldViewableProviderImpl(loadChunks(dtOverworld, "apart1"))
    val apart2World = LayerWorldViewableProviderImpl(loadChunks(dtOverworld, "apart2"))

    val worlds = mapOf(
        "normal" to normalWorld,
        "winter" to winterWorld,
        "dust" to dustWorld,
        "lobby" to lobbyWorld,
        "street" to streetWorld,
        "apart1" to apart1World,
        "apart2" to apart2World,
    )
}