package ru.cherryngine.impl.demo.view

import net.kyori.adventure.key.Key
import ru.cherryngine.lib.math.Transform
import ru.cherryngine.lib.math.Vec3D
import ru.cherryngine.lib.math.YawPitch

class CompositeCubeView(
    private val views: List<CubeView>
) : CubeView {
    override fun updatePosition(position: Vec3D, yawPitch: YawPitch) =
        views.forEach { it.updatePosition(position, yawPitch) }
    override fun updateMaterial(material: Key) =
        views.forEach { it.updateMaterial(material) }
    override fun updateTransform(transform: Transform) =
        views.forEach { it.updateTransform(transform) }
    override fun setViewContextIDs(contextIDs: Set<String>) =
        views.forEach { it.setViewContextIDs(contextIDs) }
    override fun destroy() =
        views.forEach { it.destroy() }
}
