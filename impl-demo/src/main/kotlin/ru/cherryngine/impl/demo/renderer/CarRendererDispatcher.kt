package ru.cherryngine.impl.demo.renderer

import ru.cherryngine.engine.core.instance.InstanceSingleton
import ru.cherryngine.engine.core.player.PlayerManager
import ru.cherryngine.engine.core.platform.SubscriptionDispatcher
import ru.cherryngine.lib.math.Transform
import java.util.UUID

@InstanceSingleton
class CarRendererDispatcher(
    private val renderers: List<CarRenderer<*>>,
    playerManager: PlayerManager,
) {
    private val base = SubscriptionDispatcher(renderers, playerManager)

    fun onAdd(id: UUID) = base.onAdd(id)
    fun onRemove(id: UUID) = base.onRemove(id)

    fun update(
        id: UUID,
        chassisTransform: Transform,
        wheelTransforms: List<Transform>,
        viewContextIDs: Set<String>,
    ) {
        base.syncSubscribers(id, viewContextIDs)
        renderers.forEach { it.update(id, chassisTransform, wheelTransforms) }
    }
}
