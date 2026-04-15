package ru.cherryngine.impl.demo

import com.github.quillraven.fleks.configureWorld
import jakarta.inject.Singleton
import net.kyori.adventure.text.Component
import ru.cherryngine.engine.core.instance.Instance
import ru.cherryngine.engine.core.instance.ServerWorld
import ru.cherryngine.engine.core.player.InstanceRouter
import ru.cherryngine.engine.core.player.PlayerInputProvider
import ru.cherryngine.engine.core.player.PlayerManager
import ru.cherryngine.engine.core.player.PlayerOutputProvider
import ru.cherryngine.engine.core.services.WorldService
import ru.cherryngine.engine.ecs.EcsWorldTickable
import ru.cherryngine.lib.math.Vec3D
import ru.cherryngine.lib.math.YawPitch
import ru.cherryngine.lib.minecraft.registry.Registries
import ru.cherryngine.lib.minecraft.registry.keys.DimensionTypes
import ru.cherryngine.lib.polar.PolarWorldGenerator
import ru.cherryngine.lib.world.LayerEntry
import java.util.*
import kotlin.time.Duration.Companion.milliseconds

@Singleton
class InstanceFactory(
    private val playerManager: PlayerManager,
    private val worldService: WorldService,
    private val instanceRouter: InstanceRouter,
    private val platformModules: List<PlatformModule>,
) {
    fun create(prefab: InstancePrefab): Instance {
        val dimensionType = Registries.dimensionType[DimensionTypes.OVERWORLD].value

        val serverWorld = ServerWorld()
        prefab.worlds.forEach { worldConfig ->
            val bytes = InstanceFactory::class.java
                .getResource("/worlds/${worldConfig.name}.polar")!!
                .readBytes()
            val layer = if (worldConfig.mutable) {
                PolarWorldGenerator.loadAsMutableLayer(bytes, dimensionType, worldConfig.name)
            } else {
                PolarWorldGenerator.loadAsLayer(bytes, dimensionType, worldConfig.name)
            }
            serverWorld.registerLayer(worldConfig.name, LayerEntry(layer, worldConfig.priority))
        }
        serverWorld.dimensionType = dimensionType

        val activePlatforms = prefab.platformIds.map { id ->
            platformModules.firstOrNull { it.id == id }
                ?: error("Unknown platform: $id")
        }.map { it.createProviders(serverWorld) }

        val inputProvider = object : PlayerInputProvider {
            override fun getPosition(uuid: UUID) =
                activePlatforms.firstNotNullOfOrNull { it.inputProvider.getPosition(uuid) }
            override fun getYawPitch(uuid: UUID) =
                activePlatforms.firstNotNullOfOrNull { it.inputProvider.getYawPitch(uuid) }
        }
        val outputProvider = object : PlayerOutputProvider {
            override fun teleport(uuid: UUID, position: Vec3D, yawPitch: YawPitch) =
                activePlatforms.forEach { it.outputProvider.teleport(uuid, position, yawPitch) }
            override fun sendMessage(uuid: UUID, message: Component) =
                activePlatforms.forEach { it.outputProvider.sendMessage(uuid, message) }
            override fun setVelocity(uuid: UUID, velocity: Vec3D) =
                activePlatforms.forEach { it.outputProvider.setVelocity(uuid, velocity) }
        }

        val scope = InstanceScope(
            instanceId = prefab.id,
            serverWorld = serverWorld,
            inputProvider = inputProvider,
            outputProvider = outputProvider,
            axolotlRenderers = activePlatforms.map { it.axolotlRenderer },
            cubeRenderers = activePlatforms.map { it.cubeRenderer },
            playerManager = playerManager,
            worldService = worldService,
            instanceRouter = instanceRouter,
        )

        val ecsWorld = configureWorld {
            systems {
                prefab.systems.forEach { config ->
                    add(config.create(scope))
                }
            }
        }

        activePlatforms.forEach {
            it.commandManager.registerCommands(TestCommand(ecsWorld))
        }

        val instance = Instance(
            tickDuration = 50.milliseconds,
            tickables = listOf(EcsWorldTickable(ecsWorld)) + activePlatforms.flatMap { it.tickables },
        )
        instance.start()
        return instance
    }
}
