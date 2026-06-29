package ru.cherryngine.impl.demo.bedrock

import ru.cherryngine.engine.core.instance.InstanceSingleton
import ru.cherryngine.engine.core.instance.TickStage
import ru.cherryngine.engine.core.instance.TickablePriority
import ru.cherryngine.engine.core.instance.Tickable
import ru.cherryngine.engine.core.player.PlayerManager
import ru.cherryngine.engine.core.shape.ShapeRaycaster
import ru.cherryngine.engine.core.world.WorldRaycasterDispatcher
import ru.cherryngine.engine.ecs.EcsWorld
import ru.cherryngine.engine.ecs.systems.CommandActionsSystem.Companion.commandAction
import ru.cherryngine.impl.demo.systems.useTool
import ru.cherryngine.platform.minecraft.bedrock.BedrockPlayer
import kotlin.time.Duration

@InstanceSingleton(platform = "bedrock")
@TickablePriority(stage = TickStage.PRE)
class BedrockToolUseTickable(
    private val playerManager: PlayerManager,
    private val ecsWorld: EcsWorld,
    private val worldRaycaster: WorldRaycasterDispatcher,
    private val shapeRaycaster: ShapeRaycaster,
) : Tickable {

    override fun tick(delta: Duration) {
        for (player in playerManager.onlinePlayers()) {
            val bp = player as? BedrockPlayer ?: continue
            // Tool — демо-понятие; для использования инструмента считаем любой клик
            // (LMB swing или RMB use-item).
            val uses = bp.pendingSwings.getAndSet(0) + bp.pendingUseItems.getAndSet(0)
            if (uses <= 0) continue
            val playerUuid = bp.uuid
            ecsWorld.commandAction {
                repeat(uses) { useTool(playerUuid, worldRaycaster, shapeRaycaster) }
            }
        }
    }
}
