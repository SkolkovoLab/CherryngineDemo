package ru.cherryngine.impl.demo.minecraft.input

import net.minestom.server.network.packet.client.play.ClientTabCompletePacket
import ru.cherryngine.engine.core.instance.InstanceSingleton
import ru.cherryngine.engine.core.player.Player
import ru.cherryngine.impl.demo.input.SuggestionRequest
import ru.cherryngine.impl.demo.input.SuggestionSource
import ru.cherryngine.platform.minecraft.java.player.MinecraftPlayer

@InstanceSingleton(platform = "minecraft")
class MinecraftSuggestionSource : SuggestionSource<MinecraftPlayer> {
    override fun canHandle(target: Player): Boolean = target is MinecraftPlayer

    override fun pollSuggestions(player: MinecraftPlayer): List<SuggestionRequest> =
        player.packets<ClientTabCompletePacket>()
            .map { SuggestionRequest(it.transactionId, it.text.removePrefix("/")) }
}
