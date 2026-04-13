package ru.cherryngine.impl.demo

import com.github.quillraven.fleks.World.Companion.family
import com.github.quillraven.fleks.configureWorld
import jakarta.inject.Singleton
import net.kyori.adventure.text.Component
import org.slf4j.Logger
import ru.cherryngine.engine.core.commandmanager.CherryngineCommandManager
import ru.cherryngine.engine.core.commandmanager.CommandParserRegistrar
import ru.cherryngine.engine.core.instance.Instance
import ru.cherryngine.engine.core.instance.ServerWorld
import ru.cherryngine.engine.core.player.PlayerInputProvider
import ru.cherryngine.engine.core.player.PlayerManager
import ru.cherryngine.engine.core.player.PlayerOutputProvider
import ru.cherryngine.engine.core.services.WorldService
import ru.cherryngine.engine.ecs.EcsWorld
import ru.cherryngine.engine.ecs.EcsWorldTickable
import ru.cherryngine.engine.ecs.PlayerIndex
import ru.cherryngine.engine.ecs.components.PlayerComponent
import ru.cherryngine.engine.ecs.systems.*
import ru.cherryngine.engine.physics.PhysicsSpace
import ru.cherryngine.engine.physics.terrain.TerrainGenerator
import ru.cherryngine.impl.demo.systems.*
import ru.cherryngine.impl.demo.view.CompositeAxolotlViewFactory
import ru.cherryngine.impl.demo.view.CompositeCubeViewFactory
import ru.cherryngine.lib.math.Vec3D
import ru.cherryngine.lib.math.YawPitch
import java.util.*
import kotlin.time.Duration.Companion.milliseconds

@Singleton
class DemoInit(
    worldProvider: GameWorldProvider,
    playerManager: PlayerManager,
    worldService: WorldService,
    setupFactories: List<DemoInstanceSetupFactory>,
    parserRegistrars: List<CommandParserRegistrar>,
    private val logger: Logger,
) {
    val commandManager: CherryngineCommandManager
    val ecsWorld: EcsWorld
    val playerIndex: PlayerIndex
    val instance: Instance
    val serverWorld: ServerWorld

    init {
        serverWorld = ServerWorld()
        val setups = setupFactories.map { it.create(serverWorld) }

        val inputProvider = object : PlayerInputProvider {
            override fun getPosition(uuid: UUID) = setups.firstNotNullOfOrNull { it.inputProvider.getPosition(uuid) }
            override fun getYawPitch(uuid: UUID) = setups.firstNotNullOfOrNull { it.inputProvider.getYawPitch(uuid) }
        }
        val outputProvider = object : PlayerOutputProvider {
            override fun teleport(uuid: UUID, position: Vec3D, yawPitch: YawPitch) =
                setups.forEach { it.outputProvider.teleport(uuid, position, yawPitch) }
            override fun sendMessage(uuid: UUID, message: Component) =
                setups.forEach { it.outputProvider.sendMessage(uuid, message) }
        }

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
                add(ReadClientPositionSystem(inputProvider))
                add(PlayerInitSystem("gm_construct", Vec3D(275.0, 56.0, 195.0), playerManager))
                add(CommandActionsSystem())
                add(AxolotlModelSystem(axolotlViewFactory, playerManager))
                add(CubeModelSystem(cubeViewFactory))
                add(ApartSystem())
                add(PhysicsSystem(physicsSpace, terrainGenerator, serverWorld))
                add(ViewContextSyncSystem(worldService, playerManager))
                add(WriteClientPositionSystem(outputProvider))
                add(ClearEventsSystem())
            }
        }

        commandManager = CherryngineCommandManager(logger)
        parserRegistrars.forEach { it.registerParsers(commandManager) }
        commandManager.registerCommands(TestCommand(ecsWorld, playerIndex))

        instance = Instance(
            tickDuration = 50.milliseconds,
            tickables = listOf(EcsWorldTickable(ecsWorld)) + setups.flatMap { it.createTickables() },
        )
        instance.start()
    }
}
