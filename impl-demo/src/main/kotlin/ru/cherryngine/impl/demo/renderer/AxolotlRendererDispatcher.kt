package ru.cherryngine.impl.demo.renderer

import ru.cherryngine.engine.core.instance.InstanceSingleton
import ru.cherryngine.engine.core.player.PlayerManager
import ru.cherryngine.engine.core.platform.SubscriptionDispatcher
import ru.cherryngine.lib.math.Vec3D
import ru.cherryngine.lib.math.YawPitch
import java.util.UUID

@InstanceSingleton
class AxolotlRendererDispatcher(
    private val renderers: List<AxolotlRenderer<*>>,
    playerManager: PlayerManager,
) {
    private val base = SubscriptionDispatcher(renderers, playerManager)

    fun onAdd(id: UUID) = base.onAdd(id)
    fun onRemove(id: UUID) = base.onRemove(id)

    fun update(
        id: UUID,
        position: Vec3D,
        yawPitch: YawPitch,
        name: String?,
        hiddenFromPlayer: UUID?,
        viewContextIDs: Set<String>,
    ) {
        base.syncSubscribers(id, viewContextIDs)
        renderers.forEach { it.update(id, position, yawPitch, name, hiddenFromPlayer) }
    }
}
