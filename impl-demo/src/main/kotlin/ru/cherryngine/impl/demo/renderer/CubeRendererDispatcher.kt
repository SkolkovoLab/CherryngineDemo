package ru.cherryngine.impl.demo.renderer

import net.kyori.adventure.key.Key
import ru.cherryngine.engine.core.instance.InstanceSingleton
import ru.cherryngine.engine.core.player.PlayerManager
import ru.cherryngine.engine.core.platform.SubscriptionDispatcher
import ru.cherryngine.lib.math.Transform
import ru.cherryngine.lib.math.Vec3D
import ru.cherryngine.lib.math.YawPitch
import java.util.UUID

@InstanceSingleton
class CubeRendererDispatcher(
    private val renderers: List<CubeRenderer<*>>,
    playerManager: PlayerManager,
) {
    private val base = SubscriptionDispatcher(renderers, playerManager)

    fun onAdd(id: UUID) = base.onAdd(id)
    fun onRemove(id: UUID) = base.onRemove(id)

    fun update(
        id: UUID,
        position: Vec3D,
        yawPitch: YawPitch,
        material: Key,
        transform: Transform,
        viewContextIDs: Set<String>,
    ) {
        base.syncSubscribers(id, viewContextIDs)
        renderers.forEach { it.update(id, position, yawPitch, material, transform) }
    }
}
