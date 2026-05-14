package ru.cherryngine.impl.demo.minecraft.input

import net.minestom.server.network.packet.client.play.ClientInputPacket
import ru.cherryngine.engine.core.instance.InstanceSingleton
import ru.cherryngine.engine.core.player.Player
import ru.cherryngine.impl.demo.input.DriverInputSnapshot
import ru.cherryngine.impl.demo.input.DriverInputSource
import ru.cherryngine.platform.minecraft.java.player.MinecraftPlayer
import java.util.UUID

@InstanceSingleton(platform = "minecraft")
class MinecraftDriverInputSource : DriverInputSource<MinecraftPlayer> {
    /**
     * Java-клиент шлёт ClientInputPacket только на rising/falling edge клавиш,
     * не каждый тик. Кэшируем последний пакет, чтобы возвращать актуальное
     * состояние пока клавиша зажата.
     */
    private val lastPacket = HashMap<UUID, ClientInputPacket>()

    override fun canHandle(target: Player): Boolean = target is MinecraftPlayer

    override fun pollInput(player: MinecraftPlayer): DriverInputSnapshot {
        val packet = player.packets<ClientInputPacket>().lastOrNull()
            ?.also { lastPacket[player.uuid] = it }
            ?: lastPacket[player.uuid]
            ?: return DriverInputSnapshot.EMPTY
        return DriverInputSnapshot(
            forward = packet.forward(),
            backward = packet.backward(),
            left = packet.left(),
            right = packet.right(),
            jump = packet.jump(),
            shift = packet.shift(),
        )
    }

    override fun forget(player: MinecraftPlayer) {
        lastPacket.remove(player.uuid)
    }
}
