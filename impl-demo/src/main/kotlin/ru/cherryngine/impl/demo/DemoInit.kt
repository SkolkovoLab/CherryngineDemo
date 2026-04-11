package ru.cherryngine.impl.demo

import com.github.quillraven.fleks.configureWorld
import io.micronaut.runtime.event.annotation.EventListener
import jakarta.inject.Singleton
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import ru.cherryngine.engine.minecraft.ChunkPool
import ru.cherryngine.engine.minecraft.MinecraftWorldServiceHandler
import ru.cherryngine.engine.minecraft.entity.McEntityRegistry
import ru.cherryngine.engine.minecraft.events.PlayerConfigurationAsyncEvent
import ru.cherryngine.engine.minecraft.player.MinecraftPlayerInputProvider
import ru.cherryngine.engine.minecraft.player.MinecraftPlayerOutputProvider
import ru.cherryngine.engine.minecraft.systems.McEntityBeginTickSystem
import ru.cherryngine.engine.minecraft.systems.McEntityEndTickSystem
import ru.cherryngine.engine.minecraft.systems.MinecraftViewSystem
import ru.cherryngine.engine.core.PlayerManager
import ru.cherryngine.engine.core.WorldService
import ru.cherryngine.engine.core.utils.StableTicker
import ru.cherryngine.engine.ecs.EcsWorld
import ru.cherryngine.engine.ecs.systems.*
import ru.cherryngine.engine.physics.PhysicsSpace
import ru.cherryngine.engine.physics.terrain.TerrainGenerator
import ru.cherryngine.impl.demo.systems.*
import kotlin.time.Duration.Companion.milliseconds

@Singleton
class DemoInit(
    demoWorlds: DemoWorlds,
    playerManager: PlayerManager,
    worldService: WorldService,
    worldServiceHandler: MinecraftWorldServiceHandler,
    chunkPool: ChunkPool,
) {
    val ecsWorld: EcsWorld

    init {
        val physicsSpace = PhysicsSpace()
        val terrainGenerator = TerrainGenerator(physicsSpace)
        val mcEntityRegistry = McEntityRegistry()
        val inputProvider = MinecraftPlayerInputProvider(playerManager)
        val outputProvider = MinecraftPlayerOutputProvider(playerManager)

        // Регистрация слоёв (один раз, не каждый тик)
        WorldSystem(demoWorlds, worldServiceHandler)

        ecsWorld = configureWorld {
            systems {
                add(McEntityBeginTickSystem(mcEntityRegistry))

                // чтение состояния клиента
                add(PlayerInitSystem("street", playerManager))
                add(ReadClientPositionSystem(inputProvider))

                // всякие действия
                add(CommandActionsSystem())
                add(AxolotlModelSystem(playerManager, mcEntityRegistry))
                add(CubeModelSystem(mcEntityRegistry))
                add(ApartSystem())
                add(PhysicsSystem(physicsSpace, terrainGenerator, worldServiceHandler))

                // синхронизация контекстов → world service
                add(ViewContextSyncSystem(worldService, playerManager))

                // завершение
                add(MinecraftViewSystem(playerManager, chunkPool, worldServiceHandler, mcEntityRegistry))
                add(WriteClientPositionSystem(outputProvider))
                add(McEntityEndTickSystem(mcEntityRegistry))
                add(ClearEventsSystem())
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
}
