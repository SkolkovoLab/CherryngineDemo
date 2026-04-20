package ru.cherryngine.impl.demo.minecraft.renderer

import net.kyori.adventure.key.Key
import net.minestom.server.entity.GameMode
import net.minestom.server.network.packet.server.play.ChangeGameStatePacket
import net.minestom.server.network.packet.server.play.JoinGamePacket
import net.minestom.server.registry.Registries
import ru.cherryngine.engine.core.commandmanager.CherryngineCommandManager
import ru.cherryngine.engine.core.instance.InstanceSingleton
import ru.cherryngine.engine.core.player.Player
import ru.cherryngine.engine.minecraft.commandmanager.CommandNodeUtils
import ru.cherryngine.engine.minecraft.player.MinecraftPlayer
import ru.cherryngine.impl.demo.renderer.PlayerRenderer

@InstanceSingleton(platform = "minecraft")
class MinecraftPlayerRenderer(
    private val commandManager: CherryngineCommandManager,
    private val registries: Registries,
) : PlayerRenderer {
    private val overworldDimensionTypeId: Int by lazy {
        val dim = registries.dimensionType()
        val key = dim.getKey(Key.key("minecraft:overworld"))
            ?: error("overworld DimensionType отсутствует в registries")
        dim.getId(key)
    }

    override fun onJoin(player: Player) {
        val mcPlayer = player as? MinecraftPlayer ?: return
        mcPlayer.connection.sendPacket(
            JoinGamePacket(
                0, false, listOf("minecraft:overworld"), 20,
                8, 8, false, true, false,
                overworldDimensionTypeId,
                "minecraft:overworld", 0L,
                GameMode.CREATIVE, GameMode.CREATIVE,
                false, false, null, 0, 64, false,
            )
        )
        mcPlayer.connection.sendPacket(
            ChangeGameStatePacket(ChangeGameStatePacket.Reason.LEVEL_CHUNKS_LOAD_START, 0f)
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
