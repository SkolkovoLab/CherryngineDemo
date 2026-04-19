package ru.cherryngine.impl.demo.mcprotocollib.renderer

import net.kyori.adventure.key.Key
import org.geysermc.mcprotocollib.protocol.data.game.entity.player.GameMode
import org.geysermc.mcprotocollib.protocol.data.game.entity.player.PlayerSpawnInfo
import org.geysermc.mcprotocollib.protocol.data.game.level.notify.GameEvent
import org.geysermc.mcprotocollib.protocol.packet.ingame.clientbound.ClientboundLoginPacket
import org.geysermc.mcprotocollib.protocol.packet.ingame.clientbound.level.ClientboundGameEventPacket
import ru.cherryngine.engine.core.instance.InstanceSingleton
import ru.cherryngine.engine.core.player.Player
import ru.cherryngine.engine.mcprotocollib.McProtocolLibPlayer
import ru.cherryngine.impl.demo.renderer.PlayerRenderer

@InstanceSingleton(platform = "mcprotocollib")
class McProtocolLibPlayerRenderer : PlayerRenderer {

    override fun onJoin(player: Player) {
        val mcplPlayer = player as? McProtocolLibPlayer ?: return
        val session = mcplPlayer.session

        session.send(
            ClientboundLoginPacket(
                0, false, arrayOf(Key.key("world")), 20, 8, 8, false, true, false,
                PlayerSpawnInfo(
                    0, Key.key("world"), 0L,
                    GameMode.CREATIVE, GameMode.CREATIVE,
                    false, false, null, 0, 32
                ),
                false
            )
        )
        session.send(ClientboundGameEventPacket(GameEvent.LEVEL_CHUNKS_LOAD_START, null))
    }

    override fun onViewContextChanged(player: Player, contextIDs: Set<String>) {
        val mcplPlayer = player as? McProtocolLibPlayer ?: return
        if (mcplPlayer.viewContextIDs == contextIDs) return
        mcplPlayer.viewContextIDs = contextIDs
        mcplPlayer.sentChunksBase = null
        mcplPlayer.sentChunks.clear()
    }
}