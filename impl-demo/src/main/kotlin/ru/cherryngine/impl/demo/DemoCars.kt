package ru.cherryngine.impl.demo

import ru.cherryngine.engine.physics.CarSettings
import ru.cherryngine.lib.math.Vec3D

/**
 * Готовые спеки машин для демо. Сюда складываются «персонажи» — конкретные
 * параметрические наборы, выверенные под определённое поведение.
 */
object DemoCars {

    /**
     * RWD drift-spec купе: 1755×1400×4760 мм, wheelbase 2730 мм.
     * Сильно ослабленная rear lateral friction (0.5 vs front 1.6) → задняя ось
     * легко срывается на скорости + при газе в повороте.
     */
    val DRIFT_COUPE: CarSettings = CarSettings(
        chassisSize = Vec3D(1.8, 1.2, 4.8),
        chassisMass = 1500f,
        wheelbase = 2.73f,
        wheelRadius = 0.35f,
        wheelWidth = 0.25f,
        suspensionMinLength = 0.35f,
        suspensionMaxLength = 0.58f,
        suspensionFrequency = 2.5f,
        suspensionDamping = 0.7f,
        maxSteerAngleDegrees = 40f,
        maxPitchRollAngleDegrees = 60f,
        engineMaxTorque = 900f,
        engineMaxRpm = 7000f,
        rearHandBrakeTorque = 4000f,
        limitedSlipRatio = 1.4f,
        antiRollBarFrontStiffness = 500f,
        antiRollBarRearStiffness = 500f,
        frontLateralFrictionPeak = 1.6f,
        rearLateralFrictionPeak = 0.5f,
        longitudinalFrictionPeak = 2.0f,
    )
}
