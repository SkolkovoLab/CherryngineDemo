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
import kotlin.time.Duration.Companion.milliseconds

@Singleton
class DemoInit(
    worldProvider: GameWorldProvider,
    playerManager: PlayerManager,
    worldService: WorldService,
    inputProvider: PlayerInputProvider,
    outputProvider: PlayerOutputProvider,
    terrainLayerProvider: TerrainLayerProvider,
    setupFactory: DemoInstanceSetupFactory,
) {
    val ecsWorld: EcsWorld
    val playerIndex: PlayerIndex
    val instance: Instance

    init {
        val setup = setupFactory.create()

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
                // чтение состояния клиента
                add(PlayerInitSystem("street", playerManager))
                add(ReadClientPositionSystem(inputProvider))

                // всякие действия
                add(CommandActionsSystem())
                add(AxolotlModelSystem(setup.axolotlViewFactory, playerManager))
                add(CubeModelSystem(setup.cubeViewFactory))
                add(ApartSystem())
                add(PhysicsSystem(physicsSpace, terrainGenerator, terrainLayerProvider))

                // синхронизация контекстов → world service
                add(ViewContextSyncSystem(worldService, playerManager))

                add(WriteClientPositionSystem(outputProvider))
                add(ClearEventsSystem())
            }
        }

        instance = Instance(
            tickDuration = 50.milliseconds,
            tickables = listOf(EcsWorldTickable(ecsWorld)) + setup.createTickables(),
        )
        instance.start()
    }
}
