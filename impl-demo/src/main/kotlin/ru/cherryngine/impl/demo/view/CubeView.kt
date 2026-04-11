package ru.cherryngine.impl.demo.view

import net.kyori.adventure.key.Key
import ru.cherryngine.lib.math.Transform
import ru.cherryngine.lib.math.Vec3D
import ru.cherryngine.lib.math.YawPitch

interface CubeView {
    fun updatePosition(position: Vec3D, yawPitch: YawPitch)
    fun updateMaterial(material: Key)
    fun updateTransform(transform: Transform)
    fun setViewContextIDs(contextIDs: Set<String>)
    fun destroy()
}
