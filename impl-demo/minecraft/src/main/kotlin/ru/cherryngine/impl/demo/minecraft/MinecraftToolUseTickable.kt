package ru.cherryngine.impl.demo.minecraft

import net.minestom.server.network.packet.client.play.ClientUseItemPacket
import ru.cherryngine.engine.core.instance.InstanceSingleton
import ru.cherryngine.engine.core.instance.TickStage
import ru.cherryngine.engine.core.instance.Tickable
import ru.cherryngine.engine.core.player.PlayerManager
import ru.cherryngine.engine.core.world.WorldRaycasterDispatcher
import ru.cherryngine.engine.ecs.EcsWorld
import ru.cherryngine.engine.ecs.systems.CommandActionsSystem.Companion.commandAction
import ru.cherryngine.impl.demo.input.SwingDispatcher
import ru.cherryngine.impl.demo.systems.useTool
import ru.cherryngine.platform.minecraft.java.player.MinecraftPlayer
import kotlin.time.Duration

@InstanceSingleton(platform = "minecraft", stage = TickStage.PRE)
class MinecraftToolUseTickable(
    private val playerManager: PlayerManager,
    private val ecsWorld: EcsWorld,
    private val raycasterDispatcher: WorldRaycasterDispatcher,
    private val swingDispatcher: SwingDispatcher,
) : Tickable {

    override fun tick(delta: Duration) {
        for (player in playerManager.onlinePlayers()) {
            val mc = player as? MinecraftPlayer ?: continue
            // Tool — демо-понятие; для использования инструмента считаем любой клик
            // (LMB swing через SwingDispatcher, RMB use-item читается из packets() напрямую).
            val uses = swingDispatcher.pollSwings(mc) + mc.packets<ClientUseItemPacket>().size
            if (uses <= 0) continue
            val playerUuid = mc.uuid
            ecsWorld.commandAction {
                repeat(uses) { useTool(playerUuid, raycasterDispatcher) }
            }
        }
    }
}
