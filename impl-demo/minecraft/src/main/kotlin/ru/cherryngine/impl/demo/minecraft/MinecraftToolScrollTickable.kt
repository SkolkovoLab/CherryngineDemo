package ru.cherryngine.impl.demo.minecraft

import net.minestom.server.network.packet.client.play.ClientHeldItemChangePacket
import ru.cherryngine.engine.core.instance.InstanceSingleton
import ru.cherryngine.engine.core.instance.TickStage
import ru.cherryngine.engine.core.instance.Tickable
import ru.cherryngine.engine.core.player.PlayerManager
import ru.cherryngine.engine.core.utils.scrollAmount
import ru.cherryngine.engine.ecs.EcsWorld
import ru.cherryngine.engine.ecs.getPlayerEntityOrNull
import ru.cherryngine.engine.ecs.systems.CommandActionsSystem.Companion.commandAction
import ru.cherryngine.impl.demo.components.GrabbingComponent
import ru.cherryngine.impl.demo.components.SelectedToolComponent
import ru.cherryngine.impl.demo.components.Tool
import ru.cherryngine.platform.minecraft.java.player.MinecraftPlayer
import java.util.UUID
import kotlin.time.Duration

private const val GRAB_DISTANCE_STEP = 0.5
private const val GRAB_MIN_DISTANCE = 1.5
private const val GRAB_MAX_DISTANCE = 20.0

@InstanceSingleton(platform = "minecraft", stage = TickStage.PRE)
class MinecraftToolScrollTickable(
    private val playerManager: PlayerManager,
    private val ecsWorld: EcsWorld,
) : Tickable {

    // Per-player состояние "слот на конец прошлого тика" — нужно для scrollAmount(prev, new).
    // Слот на платформенном Player не храним: tools — демо-понятие.
    private val prevSlot = HashMap<UUID, Int>()

    override fun tick(delta: Duration) {
        for (player in playerManager.onlinePlayers()) {
            val mc = player as? MinecraftPlayer ?: continue
            val packets = mc.packets<ClientHeldItemChangePacket>()
            if (packets.isEmpty()) continue

            var prev = prevSlot[mc.uuid] ?: 0
            var totalDelta = 0
            for (p in packets) {
                val newSlot = p.slot.toInt()
                totalDelta += scrollAmount(prev, newSlot)
                prev = newSlot
            }
            prevSlot[mc.uuid] = prev
            if (totalDelta == 0) continue

            val playerUuid = mc.uuid
            ecsWorld.commandAction {
                val entity = getPlayerEntityOrNull(playerUuid) ?: return@commandAction
                val grabbing = entity.getOrNull(GrabbingComponent)
                if (grabbing != null) {
                    // Пока держим куб — колесо двигает его ближе/дальше.
                    // Инверсия: scroll up (totalDelta < 0) = от себя, scroll down = к себе.
                    grabbing.distance = (grabbing.distance - totalDelta * GRAB_DISTANCE_STEP)
                        .coerceIn(GRAB_MIN_DISTANCE, GRAB_MAX_DISTANCE)
                } else {
                    val cmp = entity.getOrNull(SelectedToolComponent) ?: return@commandAction
                    val n = Tool.entries.size
                    val newIdx = ((cmp.tool.ordinal + totalDelta) % n + n) % n
                    cmp.tool = Tool.entries[newIdx]
                }
            }
        }

        // GC застрявшие записи отключённых игроков.
        val online = playerManager.onlinePlayers().mapTo(HashSet()) { it.uuid }
        prevSlot.keys.retainAll(online)
    }
}
