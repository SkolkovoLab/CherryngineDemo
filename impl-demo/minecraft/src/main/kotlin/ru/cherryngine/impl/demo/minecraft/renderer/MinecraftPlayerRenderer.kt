package ru.cherryngine.impl.demo.minecraft.renderer

import net.kyori.adventure.key.Key
import net.minestom.server.entity.GameMode
import net.minestom.server.network.packet.server.play.ChangeGameStatePacket
import net.minestom.server.network.packet.server.play.JoinGamePacket
import net.minestom.server.registry.Registries
import ru.cherryngine.engine.core.commandmanager.CherryngineCommandManager
import ru.cherryngine.engine.core.instance.InstanceSingleton
import ru.cherryngine.engine.core.player.Player
import ru.cherryngine.impl.demo.renderer.PlayerRenderer
import ru.cherryngine.platform.minecraft.java.commandmanager.CommandNodeUtils
import ru.cherryngine.platform.minecraft.java.player.MinecraftPlayer

@InstanceSingleton(platform = "minecraft")
class MinecraftPlayerRenderer(
    private val commandManager: CherryngineCommandManager,
    private val registries: Registries,
) : PlayerRenderer<MinecraftPlayer> {
    private val overworldDimensionTypeId: Int by lazy {
        val dim = registries.dimensionType()
        val key = dim.getKey(Key.key("minecraft:overworld"))
            ?: error("overworld DimensionType отсутствует в registries")
        dim.getId(key)
    }

    override fun canHandle(target: Player): Boolean = target is MinecraftPlayer

    override fun onJoin(player: MinecraftPlayer) {
        player.connection.sendPacket(
            JoinGamePacket(
                0, false, listOf("minecraft:overworld"), 20,
                8, 8, false, true, false,
                overworldDimensionTypeId,
                "minecraft:overworld", 0L,
                GameMode.CREATIVE, GameMode.CREATIVE,
                false, false, null, 0, 64, false,
            )
        )
        player.connection.sendPacket(
            ChangeGameStatePacket(ChangeGameStatePacket.Reason.LEVEL_CHUNKS_LOAD_START, 0f)
        )
        player.connection.sendPacket(
            CommandNodeUtils.commandsPacket(commandManager.commandTree().rootNode())
        )
    }

    override fun onViewContextChanged(player: MinecraftPlayer, contextIDs: Set<String>) {
        if (player.viewContextIDs == contextIDs) return
        player.viewContextIDs = contextIDs
        player.sentChunksBase = null
        player.sentChunks.clear()
    }
}
