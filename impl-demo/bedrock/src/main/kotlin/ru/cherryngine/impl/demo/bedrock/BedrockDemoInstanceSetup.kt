package ru.cherryngine.impl.demo.bedrock

import net.kyori.adventure.key.Key
import ru.cherryngine.engine.core.commandmanager.CherryngineCommandManager
import ru.cherryngine.engine.core.commandmanager.SArgumentParser
import ru.cherryngine.engine.core.instance.Tickable
import ru.cherryngine.engine.core.player.PlayerInputProvider
import ru.cherryngine.engine.core.player.PlayerManager
import ru.cherryngine.engine.core.player.PlayerOutputProvider
import ru.cherryngine.engine.bedrock.BedrockPlayerInputProvider
import ru.cherryngine.engine.bedrock.BedrockPlayerOutputProvider
import ru.cherryngine.engine.bedrock.BedrockWorldServiceHandler
import ru.cherryngine.engine.bedrock.world.BedrockBlockMapping
import ru.cherryngine.engine.bedrock.world.BedrockViewTickable
import ru.cherryngine.impl.demo.DemoInstanceSetup
import ru.cherryngine.impl.demo.renderer.AxolotlRenderer
import ru.cherryngine.impl.demo.renderer.CubeRenderer
import ru.cherryngine.lib.math.Transform
import ru.cherryngine.lib.math.Vec3D
import ru.cherryngine.lib.math.YawPitch
import java.util.*

class BedrockDemoInstanceSetup(
    private val playerManager: PlayerManager,
    private val worldServiceHandler: BedrockWorldServiceHandler,
    private val blockMapping: BedrockBlockMapping,
    private val serverWorld: ru.cherryngine.engine.core.instance.ServerWorld,
    parsers: List<SArgumentParser<*>>,
) : DemoInstanceSetup {
    override val axolotlRenderer: AxolotlRenderer = NoOpAxolotlRenderer
    override val cubeRenderer: CubeRenderer = NoOpCubeRenderer
    override val inputProvider: PlayerInputProvider = BedrockPlayerInputProvider(playerManager)
    override val outputProvider: PlayerOutputProvider = BedrockPlayerOutputProvider(playerManager)
    override val commandManager = CherryngineCommandManager(parsers)

    override fun createTickables(): List<Tickable> = listOf(
        BedrockViewTickable(playerManager, worldServiceHandler, blockMapping, serverWorld),
        BedrockCommandTickable(playerManager, commandManager),
    )
}

private object NoOpAxolotlRenderer : AxolotlRenderer {
    override fun onAdd(id: UUID) {}
    override fun onRemove(id: UUID) {}
    override fun update(id: UUID, position: Vec3D, yawPitch: YawPitch, name: String?, hiddenFromPlayer: UUID?, viewContextIDs: Set<String>) {}
}

private object NoOpCubeRenderer : CubeRenderer {
    override fun onAdd(id: UUID) {}
    override fun onRemove(id: UUID) {}
    override fun update(id: UUID, position: Vec3D, yawPitch: YawPitch, material: Key, transform: Transform, viewContextIDs: Set<String>) {}
}
