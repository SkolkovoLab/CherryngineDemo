package ru.cherryngine.impl.demo

import com.github.quillraven.fleks.World.Companion.family
import com.github.quillraven.fleks.configureWorld
import jakarta.inject.Singleton
import ru.cherryngine.engine.core.Instance
import ru.cherryngine.engine.core.PlayerInputProvider
import ru.cherryngine.engine.core.PlayerManager
import ru.cherryngine.engine.core.PlayerOutputProvider
import ru.cherryngine.engine.core.WorldService
import ru.cherryngine.engine.ecs.EcsWorld
import ru.cherryngine.engine.ecs.EcsWorldTickable
import ru.cherryngine.engine.ecs.PlayerIndex
import ru.cherryngine.engine.ecs.components.PlayerComponent
import ru.cherryngine.engine.ecs.systems.*
import ru.cherryngine.engine.physics.PhysicsSpace
import ru.cherryngine.engine.physics.terrain.TerrainGenerator
import ru.cherryngine.engine.physics.terrain.TerrainLayerProvider
import ru.cherryngine.impl.demo.systems.*
import ru.cherryngine.impl.demo.view.CompositeAxolotlViewFactory
import ru.cherryngine.impl.demo.view.CompositeCubeViewFactory
import kotlin.time.Duration.Companion.milliseconds

@Singleton
class DemoInit(
    worldProvider: GameWorldProvider,
    playerManager: PlayerManager,
    worldService: WorldService,
    inputProviders: List<PlayerInputProvider>,
    outputProviders: List<PlayerOutputProvider>,
    terrainLayerProvider: TerrainLayerProvider,
    setupFactories: List<DemoInstanceSetupFactory>,
) {
    val ecsWorld: EcsWorld
    val playerIndex: PlayerIndex
    val instance: Instance

    init {
        val inputProvider = CompositePlayerInputProvider(inputProviders)
        val outputProvider = CompositePlayerOutputProvider(outputProviders)

        val setups = setupFactories.map { it.create() }

        val axolotlViewFactory = CompositeAxolotlViewFactory(setups.map { it.axolotlViewFactory })
        val cubeViewFactory = CompositeCubeViewFactory(setups.map { it.cubeViewFactory })

        val physicsSpace = PhysicsSpace()
        val terrainGenerator = TerrainGenerator(physicsSpace)
        playerIndex = PlayerIndex()

        ecsWorld = configureWorld {
            families {
                val playerFamily = family { all(PlayerComponent) }
                onAdd(playerFamily) { entity -> playerIndex.onAdd(entity, entity[PlayerComponent].uuid) }
                onRemove(playerFamily) { entity -> playerIndex.onRemove(entity[PlayerComponent].uuid) }
            }
            systems {
                add(PlayerInitSystem("street", playerManager))
                add(ReadClientPositionSystem(inputProvider))
                add(CommandActionsSystem())
                add(AxolotlModelSystem(axolotlViewFactory, playerManager))
                add(CubeModelSystem(cubeViewFactory))
                add(ApartSystem())
                add(PhysicsSystem(physicsSpace, terrainGenerator, terrainLayerProvider))
                add(ViewContextSyncSystem(worldService, playerManager))
                add(WriteClientPositionSystem(outputProvider))
                add(ClearEventsSystem())
            }
        }

        instance = Instance(
            tickDuration = 50.milliseconds,
            tickables = listOf(EcsWorldTickable(ecsWorld)) + setups.flatMap { it.createTickables() },
        )
        instance.start()
    }
}
