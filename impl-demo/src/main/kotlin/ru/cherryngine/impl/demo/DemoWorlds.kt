package ru.cherryngine.impl.demo

import jakarta.inject.Singleton
import ru.cherryngine.lib.minecraft.network.protocol.types.SectionPos
import ru.cherryngine.lib.minecraft.registry.Registries
import ru.cherryngine.lib.minecraft.registry.keys.DimensionTypes
import ru.cherryngine.lib.minecraft.registry.types.DimensionType
import ru.cherryngine.lib.polar.PolarWorldGenerator
import ru.cherryngine.lib.world.BaseWorld

@Singleton
class DemoWorlds {
    private fun loadChunks(dimensionType: DimensionType, name: String): BaseWorld {
        val polarChunks = PolarWorldGenerator.loadChunks(
            javaClass.getResource("/${name}.polar")!!.readBytes(),
        )

        val baseWorld = BaseWorld(dimensionType)
        polarChunks.forEach { (pos, result) ->
            baseWorld.lightDataMap[pos.pack()] = result.lightData
            result.sections.forEachIndexed { i, section ->
                baseWorld.sectionsMap[SectionPos(pos.x, i + baseWorld.minSection, pos.z).pack()] = section
            }
        }

        return baseWorld
    }

    private val overworld = Registries.dimensionType[DimensionTypes.OVERWORLD].value

    val normalWorld = loadChunks(overworld, "de_cache_normal")
    val winterWorld = loadChunks(overworld, "de_cache_winter")
    val dustWorld = loadChunks(overworld, "de_dust2")
    val lobbyWorld = loadChunks(overworld, "lobby")

    val streetWorld = loadChunks(overworld, "street")
    val apart1World = loadChunks(overworld, "apart1")
    val apart2World = loadChunks(overworld, "apart2")

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