package ru.cherryngine.impl.demo

import com.github.quillraven.fleks.configureWorld
import jakarta.inject.Singleton
import ru.cherryngine.engine.core.PlayerInputProvider
import ru.cherryngine.engine.core.PlayerManager
import ru.cherryngine.engine.core.PlayerOutputProvider
import ru.cherryngine.engine.core.WorldService
import ru.cherryngine.engine.core.utils.StableTicker
import ru.cherryngine.engine.ecs.EcsWorld
import ru.cherryngine.engine.ecs.systems.*
import ru.cherryngine.engine.physics.PhysicsSpace
import ru.cherryngine.engine.physics.terrain.TerrainGenerator
import ru.cherryngine.engine.physics.terrain.TerrainLayerProvider
import ru.cherryngine.impl.demo.systems.*
import ru.cherryngine.impl.demo.view.AxolotlViewFactory
import ru.cherryngine.impl.demo.view.CubeViewFactory
import kotlin.time.Duration.Companion.milliseconds

@Singleton
class DemoInit(
    worldProvider: GameWorldProvider,
    playerManager: PlayerManager,
    worldService: WorldService,
    axolotlViewFactory: AxolotlViewFactory,
    cubeViewFactory: CubeViewFactory,
    inputProvider: PlayerInputProvider,
    outputProvider: PlayerOutputProvider,
    terrainLayerProvider: TerrainLayerProvider,
    ecsSystems: List<DemoEcsSystemProvider>,
) {
    val ecsWorld: EcsWorld

    init {
        val physicsSpace = PhysicsSpace()
        val terrainGenerator = TerrainGenerator(physicsSpace)

        ecsWorld = configureWorld {
            systems {
                // ранние платформенные системы (beginTick и т.д.)
                ecsSystems.forEach { it.addEarlySystems(this) }

                // чтение состояния клиента
                add(PlayerInitSystem("street", playerManager))
                add(ReadClientPositionSystem(inputProvider))

                // всякие действия
                add(CommandActionsSystem())
                add(AxolotlModelSystem(axolotlViewFactory, playerManager))
                add(CubeModelSystem(cubeViewFactory))
                add(ApartSystem())
                add(PhysicsSystem(physicsSpace, terrainGenerator, terrainLayerProvider))

                // синхронизация контекстов → world service
                add(ViewContextSyncSystem(worldService, playerManager))

                // поздние платформенные системы (view, endTick и т.д.)
                ecsSystems.forEach { it.addLateSystems(this) }

                add(WriteClientPositionSystem(outputProvider))
                add(ClearEventsSystem())
            }
        }

        val tickDuration = 50.milliseconds
        val ticker = StableTicker(tickDuration) { _, _ ->
            ecsWorld.update(tickDuration)
        }
        ticker.start()
    }
}
