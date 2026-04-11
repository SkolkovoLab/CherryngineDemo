package ru.cherryngine.impl.demo.view

import ru.cherryngine.lib.math.Vec3D
import ru.cherryngine.lib.math.YawPitch
import java.util.UUID

class CompositeAxolotlView(
    private val views: List<AxolotlView>
) : AxolotlView {
    override fun updatePosition(position: Vec3D, yawPitch: YawPitch) =
        views.forEach { it.updatePosition(position, yawPitch) }
    override fun setName(name: String?) =
        views.forEach { it.setName(name) }
    override fun setHiddenFromPlayer(uuid: UUID?) =
        views.forEach { it.setHiddenFromPlayer(uuid) }
    override fun setViewContextIDs(contextIDs: Set<String>) =
        views.forEach { it.setViewContextIDs(contextIDs) }
    override fun destroy() =
        views.forEach { it.destroy() }
}
