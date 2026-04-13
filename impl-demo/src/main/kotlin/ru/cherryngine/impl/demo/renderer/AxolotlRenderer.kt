package ru.cherryngine.impl.demo.renderer

import ru.cherryngine.lib.math.Vec3D
import ru.cherryngine.lib.math.YawPitch
import java.util.UUID

interface AxolotlRenderer {
    fun onAdd(id: UUID)
    fun onRemove(id: UUID)
    fun update(
        id: UUID,
        position: Vec3D,
        yawPitch: YawPitch,
        name: String?,
        hiddenFromPlayer: UUID?,
        viewContextIDs: Set<String>,
    )
}
