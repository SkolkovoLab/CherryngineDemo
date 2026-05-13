package ru.cherryngine.impl.demo.renderer

import ru.cherryngine.engine.core.player.Player
import ru.cherryngine.engine.core.platform.EntityRenderer
import ru.cherryngine.lib.math.Transform
import java.util.UUID

/**
 * Машина целиком (chassis + 4 колеса) как платформенный рендерер.
 * Внутри реализации — сколько угодно платформенных entity'ей, снаружи
 * — одна логическая «машина» с единым id.
 *
 * Вызывается из [ru.cherryngine.impl.demo.systems.CarModelSystem] каждый тик
 * с актуальными трансформами от Jolt VehicleConstraint:
 * - [chassisTransform] — world-translation + rotation chassis, scale = chassisSize
 * - [wheelTransforms] — 4 элемента, world-translation + rotation колеса, scale = wheelSize
 */
interface CarRenderer<in P : Player> : EntityRenderer<P> {
    fun update(
        id: UUID,
        chassisTransform: Transform,
        wheelTransforms: List<Transform>,
    )
}
