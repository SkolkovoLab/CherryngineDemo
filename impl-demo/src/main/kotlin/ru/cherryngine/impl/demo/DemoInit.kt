package ru.cherryngine.impl.demo

import com.github.quillraven.fleks.configureWorld
import io.micronaut.runtime.event.annotation.EventListener
import jakarta.inject.Singleton
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import ru.cherryngine.engine.core.entity.McEntityRegistry
import ru.cherryngine.engine.core.events.PlayerConfigurationAsyncEvent
import ru.cherryngine.engine.core.events.SetGameProfileEvent
import ru.cherryngine.engine.core.player.PlayerManager
import ru.cherryngine.engine.core.utils.StableTicker
import ru.cherryngine.engine.physics.PhysicsSpace
import ru.cherryngine.engine.physics.terrain.TerrainGenerator
import ru.cherryngine.engine.ecs.EcsWorld
import ru.cherryngine.engine.ecs.components.ViewableComponent
import ru.cherryngine.engine.ecs.systems.*
import ru.cherryngine.engine.core.ChunkPool
import ru.cherryngine.impl.demo.components.WorldComponent
import ru.cherryngine.impl.demo.systems.*
import ru.cherryngine.lib.minecraft.network.protocol.types.GameProfile
import java.util.*
import kotlin.time.Duration.Companion.milliseconds

@Singleton
class DemoInit(
    demoWorlds: DemoWorlds,
    playerManager: PlayerManager,
    chunkPool: ChunkPool,
    physicsSpace: PhysicsSpace,
    terrainGenerator: TerrainGenerator,
    mcEntityRegistry: McEntityRegistry,
) {
    val ecsWorld: EcsWorld

    init {
        ecsWorld = configureWorld {
            systems {
                // чтение сосотяния клиента
                add(PlayerInitSystem("street", playerManager))
                add(ReadClientPositionSystem(playerManager))

                // всякие действия
                add(CommandActionsSystem())
                add(AxolotlModelSystem(playerManager))
                add(CubeModelSystem(mcEntityRegistry))
                add(WorldSystem(demoWorlds))
                add(ApartSystem())
                add(PhysicsSystem(physicsSpace, terrainGenerator))

                // завершение
                add(ViewSystem(playerManager, chunkPool))
                add(WriteClientPositionSystem(playerManager))
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