package ru.cherryngine.impl.demo

import com.github.quillraven.fleks.configureWorld
import io.micronaut.context.ApplicationContext
import jakarta.inject.Singleton
import kotlinx.coroutines.channels.Channel
import net.kyori.adventure.key.Key
import net.minestom.server.registry.Registries
import net.minestom.server.world.DimensionType
import ru.cherryngine.engine.core.commandmanager.CherryngineCommandManager
import ru.cherryngine.engine.core.instance.Instance
import ru.cherryngine.engine.core.world.ServerWorld
import ru.cherryngine.engine.core.instance.InstanceRouter
import ru.cherryngine.engine.core.player.Player
import ru.cherryngine.engine.ecs.EcsWorld
import ru.cherryngine.platform.minecraft.java.MinecraftServerWorld
import ru.cherryngine.platform.minecraft.java.world.LayerEntry
import ru.cherryngine.platform.minecraft.java.world.polar.PolarWorldGenerator
import java.util.*
import kotlin.time.Duration.Companion.milliseconds

@Singleton
class InstanceFactory(
    private val appContext: ApplicationContext,
    private val instanceRouter: InstanceRouter,
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

        val serverWorld = MinecraftServerWorld()
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
            register(MinecraftServerWorld::class.java, serverWorld)
            register(InstanceJoinChannel(joinChannel))
            register(InstanceLeaveChannel(leaveChannel))
        }

        instance.initEager()

        val ecsWorld = configureWorld {
            systems {
                prefab.systems.forEach { config ->
                    add(config.create(instance))
                }
            }
        }
        instance.register(EcsWorld::class.java, ecsWorld)

        instance.get<CherryngineCommandManager>().registerCommands(
            TestCommand(ecsWorld, instance.get(), instance.get())
        )

        instance.startTicking()
        return instance
    }
}
