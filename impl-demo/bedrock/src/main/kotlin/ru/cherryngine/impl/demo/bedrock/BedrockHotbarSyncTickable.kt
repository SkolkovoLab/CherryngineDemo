package ru.cherryngine.impl.demo.bedrock

import ru.cherryngine.engine.core.instance.InstanceSingleton
import ru.cherryngine.engine.core.instance.TickStage
import ru.cherryngine.engine.core.instance.TickablePriority
import ru.cherryngine.engine.core.instance.Tickable
import ru.cherryngine.engine.core.player.PlayerManager
import ru.cherryngine.engine.ecs.EcsWorld
import ru.cherryngine.engine.ecs.getPlayerEntityOrNull
import ru.cherryngine.engine.ecs.systems.CommandActionsSystem.Companion.commandAction
import ru.cherryngine.impl.demo.components.GrabbingComponent
import ru.cherryngine.impl.demo.components.InventoryComponent
import ru.cherryngine.platform.minecraft.bedrock.BedrockPlayer
import kotlin.time.Duration

private const val GRAB_DISTANCE_STEP = 0.5
private const val GRAB_MIN_DISTANCE = 1.5
private const val GRAB_MAX_DISTANCE = 20.0

@InstanceSingleton(platform = "bedrock")
@TickablePriority(stage = TickStage.PRE)
class BedrockHotbarSyncTickable(
    private val playerManager: PlayerManager,
    private val ecsWorld: EcsWorld,
) : Tickable {

    override fun tick(delta: Duration) {
        for (player in playerManager.onlinePlayers()) {
            val bp = player as? BedrockPlayer ?: continue
            // Дельты в очереди — индикатор «слот сменился в этом тике». Сами дельты
            // используем только для режима grab; для смены activeSlot — берём абсолютный
            // bp.heldItemSlot, который BedrockSessionHandler уже обновил из MobEquipmentPacket.
            var totalDelta = 0
            while (true) totalDelta += bp.pendingSlotDeltas.poll() ?: break
            if (totalDelta == 0) continue
            val absoluteSlot = bp.heldItemSlot
            val playerUuid = bp.uuid
            ecsWorld.commandAction {
                val entity = getPlayerEntityOrNull(playerUuid) ?: return@commandAction
                val grabbing = entity.getOrNull(GrabbingComponent)
                if (grabbing != null) {
                    grabbing.distance = (grabbing.distance - totalDelta * GRAB_DISTANCE_STEP)
                        .coerceIn(GRAB_MIN_DISTANCE, GRAB_MAX_DISTANCE)
                } else {
                    val inventory = entity.getOrNull(InventoryComponent) ?: return@commandAction
                    inventory.activeSlot = absoluteSlot.coerceIn(0, inventory.size - 1)
                }
            }
        }
    }
}
