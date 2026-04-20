package ru.cherryngine.impl.demo

import com.github.quillraven.fleks.configureWorld
import io.micronaut.context.ApplicationContext
import jakarta.inject.Singleton
import kotlinx.coroutines.channels.Channel
import net.kyori.adventure.text.Component
import ru.cherryngine.engine.core.commandmanager.CherryngineCommandManager
import ru.cherryngine.engine.core.instance.Instance
import ru.cherryngine.engine.core.instance.ServerWorld
import ru.cherryngine.engine.core.player.InstanceRouter
import ru.cherryngine.engine.core.player.Player
import ru.cherryngine.engine.core.player.PlayerInputProvider
import ru.cherryngine.engine.core.player.PlayerOutputProvider
import net.kyori.adventure.key.Key
import net.minestom.server.registry.Registries
import net.minestom.server.world.DimensionType
import ru.cherryngine.engine.ecs.EcsWorld
import ru.cherryngine.lib.math.Vec3D
import ru.cherryngine.lib.math.YawPitch
import ru.cherryngine.lib.polar.PolarWorldGenerator
import ru.cherryngine.lib.world.LayerEntry
import java.util.*
import kotlin.time.Duration.Companion.milliseconds

@Singleton
class InstanceFactory(
    private val appContext: ApplicationContext,
    private val instanceRouter: InstanceRouter,
    private val platformModules: List<PlatformModule>,
    private val registries: Registries,
) {
    fun create(prefab: InstancePrefab): Instance {
        val overworldKey = Key.key("minecraft:overworld")
        val dimensionType: DimensionType = registries.dimensionType().get(overworldKey)
            ?: error("overworld DimensionType отсутствует в registries")

        val biomeRegistry = registries.biome()
        val biomeIdLookup: (String) -> Int = { id ->
            val key = if (id.contains(':')) Key.key(id) else Key.key("minecraft", id)
            val registryKey = biomeRegistry.getKey(key)
            if (registryKey != null) biomeRegistry.getId(registryKey).coerceAtLeast(0) else 0
        }

        val serverWorld = ServerWorld()
        prefab.worlds.forEach { worldConfig ->
            val bytes = InstanceFactory::class.java
                .getResource("/worlds/${worldConfig.name}.polar")!!
                .readBytes()
            val layer = if (worldConfig.mutable) {
                PolarWorldGenerator.loadAsMutableLayer(bytes, dimensionType, worldConfig.name, biomeIdLookup = biomeIdLookup)
            } else {
                PolarWorldGenerator.loadAsLayer(bytes, dimensionType, worldConfig.name, biomeIdLookup = biomeIdLookup)
            }
            serverWorld.registerLayer(worldConfig.name, LayerEntry(layer, worldConfig.priority))
        }
        serverWorld.dimensionType = dimensionType

        val joinChannel = Channel<UUID>(Channel.UNLIMITED)
        val leaveChannel = Channel<Player>(Channel.UNLIMITED)
        instanceRouter.register(prefab.id, joinChannel, leaveChannel)

        val instance = Instance(
            tickDuration = 50.milliseconds,
            platformIds = prefab.platformIds.toSet(),
            appContext = appContext,
        ).apply {
            register(InstancePrefab::class.java, prefab)
            register(ServerWorld::class.java, serverWorld)
            register(InstanceJoinChannel(joinChannel))
            register(InstanceLeaveChannel(leaveChannel))
        }

        val activePlatforms = prefab.platformIds.map { id ->
            platformModules.firstOrNull { it.id == id }
                ?: error("Unknown platform: $id")
        }.map { it.createProviders(instance, serverWorld) }

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

        instance.register(PlayerInputProvider::class.java, inputProvider)
        instance.register(PlayerOutputProvider::class.java, outputProvider)

        instance.initEager()

        val ecsWorld = configureWorld {
            systems {
                prefab.systems.forEach { config ->
                    add(config.create(instance))
                }
            }
        }
        instance.register(EcsWorld::class.java, ecsWorld)

        instance.get<CherryngineCommandManager>().registerCommands(TestCommand(ecsWorld))

        instance.startTicking()
        return instance
    }
}
