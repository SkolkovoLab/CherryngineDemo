package ru.cherryngine.impl.demo.shape

import ru.cherryngine.engine.core.shape.Shape
import ru.cherryngine.engine.core.shape.ShapeGeometry
import ru.cherryngine.lib.math.Transform
import java.util.UUID

class PlayerShape(
    override val geometry: ShapeGeometry,
    override val getTransform: () -> Transform,
    val playerUuid: UUID,
) : Shape
