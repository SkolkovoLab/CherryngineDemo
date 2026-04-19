package ru.cherryngine.impl.demo.bedrock.renderer

import ru.cherryngine.engine.bedrock.BedrockPlayer
import ru.cherryngine.engine.core.instance.InstanceSingleton
import ru.cherryngine.engine.core.player.Player
import ru.cherryngine.impl.demo.renderer.PlayerRenderer

@InstanceSingleton(platform = "bedrock")
class BedrockPlayerRenderer : PlayerRenderer {
    override fun onViewContextChanged(player: Player, contextIDs: Set<String>) {
        val bedrockPlayer = player as? BedrockPlayer ?: return
        if (bedrockPlayer.viewContextIDs == contextIDs) return
        bedrockPlayer.viewContextIDs = contextIDs
        bedrockPlayer.sentChunks.clear()
    }
}