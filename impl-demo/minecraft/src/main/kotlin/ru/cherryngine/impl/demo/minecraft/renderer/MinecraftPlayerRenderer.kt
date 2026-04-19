package ru.cherryngine.impl.demo.minecraft.renderer

import ru.cherryngine.engine.core.commandmanager.CherryngineCommandManager
import ru.cherryngine.engine.core.instance.InstanceSingleton
import ru.cherryngine.engine.core.player.Player
import ru.cherryngine.engine.minecraft.commandmanager.CommandNodeUtils
import ru.cherryngine.engine.minecraft.player.MinecraftPlayer
import ru.cherryngine.impl.demo.renderer.PlayerRenderer
import ru.cherryngine.lib.minecraft.network.protocol.packets.play.clientbound.ClientboundGameEventPacket
import ru.cherryngine.lib.minecraft.network.protocol.packets.play.clientbound.ClientboundLoginPacket
import ru.cherryngine.lib.minecraft.network.protocol.types.GameMode
import ru.cherryngine.lib.minecraft.registry.Registries
import ru.cherryngine.lib.minecraft.registry.keys.DimensionTypes

@InstanceSingleton(platform = "minecraft")
class MinecraftPlayerRenderer(
    private val commandManager: CherryngineCommandManager,
) : PlayerRenderer {

    override fun onJoin(player: Player) {
        val mcPlayer = player as? MinecraftPlayer ?: return
        mcPlayer.connection.sendPacket(
            ClientboundLoginPacket(
                0, false, listOf(), 20, 8, 8, false, true, false,
                Registries.dimensionType[DimensionTypes.OVERWORLD].value,
                "world", 0L,
                GameMode.CREATIVE, GameMode.CREATIVE,
                false, false, null, 0, 32, false
            )
        )
        mcPlayer.connection.sendPacket(
            ClientboundGameEventPacket(ClientboundGameEventPacket.GameEvent.START_WAITING_FOR_CHUNKS, 0f)
        )
        mcPlayer.connection.sendPacket(
            CommandNodeUtils.commandsPacket(commandManager.commandTree().rootNode())
        )
    }

    override fun onViewContextChanged(player: Player, contextIDs: Set<String>) {
        val mcPlayer = player as? MinecraftPlayer ?: return
        if (mcPlayer.viewContextIDs == contextIDs) return
        mcPlayer.viewContextIDs = contextIDs
        mcPlayer.sentChunksBase = null
        mcPlayer.sentChunks.clear()
    }
}