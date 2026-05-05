package ru.cherryngine.impl.demo.minecraft

import ru.cherryngine.engine.core.instance.InstanceSingleton
import ru.cherryngine.engine.core.instance.TickStage
import ru.cherryngine.engine.core.instance.Tickable
import ru.cherryngine.engine.core.player.PlayerManager
import ru.cherryngine.engine.ecs.EcsWorld
import ru.cherryngine.engine.ecs.getPlayerEntityOrNull
import ru.cherryngine.engine.ecs.systems.CommandActionsSystem.Companion.commandAction
import ru.cherryngine.impl.demo.components.SelectedToolComponent
import ru.cherryngine.impl.demo.components.Tool
import ru.cherryngine.platform.minecraft.java.player.MinecraftPlayer
import kotlin.time.Duration

@InstanceSingleton(platform = "minecraft", stage = TickStage.PRE)
class MinecraftToolScrollTickable(
    private val playerManager: PlayerManager,
    private val ecsWorld: EcsWorld,
) : Tickable {

    override fun tick(delta: Duration) {
        for (player in playerManager.onlinePlayers()) {
            val mc = player as? MinecraftPlayer ?: continue
            var totalDelta = 0
            while (true) totalDelta += mc.pendingSlotDeltas.poll() ?: break
            if (totalDelta == 0) continue
            val playerUuid = mc.uuid
            ecsWorld.commandAction {
                val entity = getPlayerEntityOrNull(playerUuid) ?: return@commandAction
                if (SelectedToolComponent !in entity) return@commandAction
                val cmp = entity[SelectedToolComponent]
                val n = Tool.entries.size
                val newIdx = ((cmp.tool.ordinal + totalDelta) % n + n) % n
                cmp.tool = Tool.entries[newIdx]
            }
        }
    }
}
