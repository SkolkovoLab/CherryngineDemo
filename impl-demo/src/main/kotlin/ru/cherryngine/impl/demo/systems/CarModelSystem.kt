package ru.cherryngine.impl.demo.systems

import com.github.quillraven.fleks.IteratingSystem
import com.github.quillraven.fleks.World.Companion.family
import ru.cherryngine.engine.core.instance.Instance
import ru.cherryngine.engine.ecs.EcsEntity
import ru.cherryngine.engine.ecs.components.PositionComponent
import ru.cherryngine.engine.ecs.components.ViewableComponent
import ru.cherryngine.engine.physics.PhysicsSpace
import ru.cherryngine.impl.demo.EcsSystemConfig
import ru.cherryngine.impl.demo.components.CarComponent
import ru.cherryngine.impl.demo.renderer.CarRendererDispatcher
import ru.cherryngine.lib.math.Transform
import ru.cherryngine.lib.math.Vec3D
import java.util.UUID

/**
 * Драйвер [CarRendererDispatcher] — каждый тик читает chassis+wheel трансформы
 * из Jolt [PhysicsSpace.VehicleBody] и передаёт их в платформенный рендерер.
 * Шаблон 1:1 с [CubeModelSystem]/[AxolotlModelSystem]: ловит добавления/удаления
 * семейства → onAdd/onRemove, в `onTickEntity` шлёт update.
 *
 * Машина не имеет собственного `CubeModelComponent` — рендер целиком на
 * стороне [ru.cherryngine.impl.demo.renderer.CarRenderer]'а.
 */
class CarModelSystem(
    private val dispatcher: CarRendererDispatcher,
    private val physicsSpace: PhysicsSpace,
) : IteratingSystem(
    family { all(CarComponent) }
) {
    private val activeIds = HashSet<UUID>()

    override fun onTick() {
        val currentIds = mutableSetOf<UUID>()
        family.forEach { currentIds.add(it[CarComponent].carPhysicsId) }

        activeIds.removeIf { id ->
            if (id !in currentIds) {
                dispatcher.onRemove(id)
                true
            } else false
        }

        currentIds.forEach { id ->
            if (activeIds.add(id)) {
                dispatcher.onAdd(id)
            }
        }

        super.onTick()
    }

    override fun onTickEntity(entity: EcsEntity) {
        val car = entity[CarComponent]
        val vehicle = physicsSpace.getVehicleBody(car.carPhysicsId) ?: return
        val viewContextIDs = entity.getOrNull(ViewableComponent)?.viewContextIDs ?: emptySet()

        val chassisRaw = vehicle.getTransform()
        val chassisTransform = Transform(
            translation = chassisRaw.translation,
            rotation = chassisRaw.rotation,
            scale = car.settings.chassisSize,
        )

        val wheelDiameter = car.settings.wheelRadius.toDouble() * 2.0
        val wheelSize = Vec3D(wheelDiameter, wheelDiameter, car.settings.wheelWidth.toDouble())
        val wheelTransforms = List(4) { i ->
            val raw = vehicle.getWheelTransform(i)
            Transform(
                translation = raw.translation,
                rotation = raw.rotation,
                scale = wheelSize,
            )
        }

        dispatcher.update(car.carPhysicsId, chassisTransform, wheelTransforms, viewContextIDs)
    }

    object Config : EcsSystemConfig {
        override fun create(instance: Instance) = CarModelSystem(
            dispatcher = instance.get(),
            physicsSpace = instance.get(),
        )
    }
}
