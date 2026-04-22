package ru.cherryngine.impl.demo.systems

import com.github.quillraven.fleks.IntervalSystem
import ru.cherryngine.engine.core.instance.Instance
import ru.cherryngine.engine.physics.PhysicsSpace
import ru.cherryngine.engine.physics.terrain.TerrainGenerator
import ru.cherryngine.impl.demo.EcsSystemConfig
import kotlin.time.Duration
import kotlin.time.DurationUnit

/**
 * Тонкая ECS-обёртка над симуляцией физики.
 * Стоит в списке систем между lifecycle (keepAlive) и sync-back (чтение transform'ов),
 * чтобы Fleks-порядок гарантировал правильный pipeline.
 */
class PhysicsSimulationSystem(
    private val physicsSpace: PhysicsSpace,
    private val terrainGenerator: TerrainGenerator,
    private val tickDuration: Duration,
) : IntervalSystem() {
    override fun onTick() {
        val deltaSec = tickDuration.toDouble(DurationUnit.SECONDS).toFloat()
        terrainGenerator.step(deltaSec, physicsSpace.collectActiveBodies())
        physicsSpace.update(deltaSec)
    }

    object Config : EcsSystemConfig {
        override fun create(instance: Instance) = PhysicsSimulationSystem(
            physicsSpace = instance.get(),
            terrainGenerator = instance.get(),
            tickDuration = instance.tickDuration,
        )
    }
}
