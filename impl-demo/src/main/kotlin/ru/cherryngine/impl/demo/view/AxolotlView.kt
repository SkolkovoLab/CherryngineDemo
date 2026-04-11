package ru.cherryngine.impl.demo.view

import ru.cherryngine.lib.math.Vec3D
import ru.cherryngine.lib.math.YawPitch
import java.util.UUID

interface AxolotlView {
    fun updatePosition(position: Vec3D, yawPitch: YawPitch)
    fun setName(name: String?)
    fun setHiddenFromPlayer(uuid: UUID?)
    fun setViewContextIDs(contextIDs: Set<String>)
    fun destroy()
}
