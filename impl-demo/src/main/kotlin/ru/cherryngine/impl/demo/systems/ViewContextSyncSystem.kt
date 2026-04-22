package ru.cherryngine.impl.demo.systems

import com.github.quillraven.fleks.IteratingSystem
import com.github.quillraven.fleks.World.Companion.family
import ru.cherryngine.engine.core.player.PlayerManager
import ru.cherryngine.engine.ecs.EcsEntity
import ru.cherryngine.engine.ecs.components.PlayerComponent
import ru.cherryngine.impl.demo.renderer.PlayerRendererDispatcher

class ViewContextSyncSystem(
    private val playerRendererDispatcher: PlayerRendererDispatcher,
    private val playerManager: PlayerManager,
) : IteratingSystem(
    family { all(PlayerComponent) }
) {
    override fun onTickEntity(entity: EcsEntity) {
        val playerComponent = entity[PlayerComponent]
        val player = playerManager.getPlayerNullable(playerComponent.uuid) ?: return
        playerRendererDispatcher.onViewContextChanged(player, playerComponent.viewContextIDs)
    }
}
