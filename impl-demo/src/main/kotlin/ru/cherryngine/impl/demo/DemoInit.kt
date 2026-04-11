package ru.cherryngine.impl.demo

import com.github.quillraven.fleks.configureWorld
import io.micronaut.runtime.event.annotation.EventListener
import jakarta.inject.Singleton
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import ru.cherryngine.engine.minecraft.ChunkPool
import ru.cherryngine.engine.minecraft.entity.McEntityRegistry
import ru.cherryngine.engine.minecraft.events.PlayerConfigurationAsyncEvent
import ru.cherryngine.engine.minecraft.player.MinecraftConnectionService
import ru.cherryngine.engine.minecraft.player.MinecraftPlayerInputProvider
import ru.cherryngine.engine.minecraft.player.MinecraftPlayerOutputProvider
import ru.cherryngine.engine.core.PlayerManager
import ru.cherryngine.engine.core.utils.StableTicker
import ru.cherryngine.engine.ecs.EcsWorld
import ru.cherryngine.engine.ecs.components.ViewableComponent
import ru.cherryngine.engine.ecs.systems.*
import ru.cherryngine.engine.ecs.systems.mc_entity.McEntityBeginTickSystem
import ru.cherryngine.engine.ecs.systems.mc_entity.McEntityEndTickSystem
import ru.cherryngine.engine.physics.PhysicsSpace
import ru.cherryngine.engine.physics.terrain.TerrainGenerator
import ru.cherryngine.impl.demo.components.WorldComponent
import ru.cherryngine.impl.demo.systems.*
import kotlin.time.Duration.Companion.milliseconds

@Singleton
class DemoInit(
    demoWorlds: DemoWorlds,
    playerManager: PlayerManager,
    connectionService: MinecraftConnectionService,
    chunkPool: ChunkPool,
) {
    val ecsWorld: EcsWorld

    init {
        val physicsSpace = PhysicsSpace()
        val terrainGenerator = TerrainGenerator(physicsSpace)
        val mcEntityRegistry = McEntityRegistry()
        val inputProvider = MinecraftPlayerInputProvider(playerManager)
        val outputProvider = MinecraftPlayerOutputProvider(playerManager)

        ecsWorld = configureWorld {
            systems {
//                add(McEntityBeginTickSystem(mcEntityRegistry))

                // чтение состояния клиента
                add(PlayerInitSystem("street", playerManager, connectionService))
                add(ReadClientPositionSystem(inputProvider))

                // всякие действия
                add(CommandActionsSystem())
                add(AxolotlModelSystem(playerManager, mcEntityRegistry))
                add(CubeModelSystem(mcEntityRegistry))
                add(WorldSystem(demoWorlds))
                add(ApartSystem())
                add(PhysicsSystem(physicsSpace, terrainGenerator))

                // завершение
                add(ViewSystem(playerManager, chunkPool))
                add(WriteClientPositionSystem(outputProvider))
//                add(McEntityEndTickSystem(mcEntityRegistry))
                add(ClearEventsSystem())
            }
        }

        val apartNames = setOf("apart1", "apart2")
        demoWorlds.layers.keys.forEach { worldName ->
            ecsWorld.entity {
                it += ViewableComponent(setOf(worldName))
                it += WorldComponent(worldName, priority = if (worldName in apartNames) 10 else 0)
            }
        }

        val tickDuration = 50.milliseconds
        val ticker = StableTicker(tickDuration) { _, _ ->
            ecsWorld.update(tickDuration)
        }
        ticker.start()
    }

    @EventListener
    fun onPlayerConfiguration(event: PlayerConfigurationAsyncEvent) = runBlocking {
        // для теста подержим игрока в конфигурации 3 секунды
        delay(3000)
    }

//    @EventListener
//    fun onSetGameProfile(event: SetGameProfileEvent) {
//        event.gameProfile = GameProfile(UUID.randomUUID(), "ebanatina")
//    }
}