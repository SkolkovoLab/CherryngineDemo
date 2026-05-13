package ru.cherryngine.impl.demo.systems

import net.kyori.adventure.key.Key
import ru.cherryngine.engine.core.shape.RayCastRequest
import ru.cherryngine.engine.core.shape.RayInfo
import ru.cherryngine.engine.core.shape.ResolvedShape
import ru.cherryngine.engine.core.shape.ShapeFilter
import ru.cherryngine.engine.core.shape.ShapeRaycaster
import ru.cherryngine.engine.core.world.WorldRaycasterDispatcher
import ru.cherryngine.engine.ecs.EcsWorld
import ru.cherryngine.engine.ecs.components.CameraMode
import ru.cherryngine.engine.ecs.components.CameraTargetComponent
import ru.cherryngine.engine.ecs.components.InputTargetComponent
import ru.cherryngine.engine.ecs.components.PositionComponent
import ru.cherryngine.engine.ecs.components.ViewableComponent
import ru.cherryngine.engine.ecs.getPlayerEntityOrNull
import ru.cherryngine.impl.demo.components.CarComponent
import ru.cherryngine.impl.demo.components.CubeModelComponent
import ru.cherryngine.impl.demo.components.GrabbingComponent
import ru.cherryngine.impl.demo.components.PhysicsComponent
import ru.cherryngine.impl.demo.components.RidingCarComponent
import ru.cherryngine.impl.demo.components.SelectedToolComponent
import ru.cherryngine.impl.demo.components.Tool
import ru.cherryngine.impl.demo.components.findPhysicsEntity
import ru.cherryngine.impl.demo.shape.PhysicsCubeShape
import ru.cherryngine.lib.math.Transform
import ru.cherryngine.lib.math.Vec3D
import ru.cherryngine.lib.math.YawPitch
import java.util.UUID

private const val EYE_HEIGHT = 1.62
private const val RAYCAST_MAX_DISTANCE = 50.0
private const val FALLBACK_DISTANCE = 5.0

fun EcsWorld.useTool(
    playerUuid: UUID,
    worldRaycaster: WorldRaycasterDispatcher,
    shapeRaycaster: ShapeRaycaster,
) {
    val playerEntity = getPlayerEntityOrNull(playerUuid) ?: return
    val tool = playerEntity.getOrNull(SelectedToolComponent)?.tool ?: return
    val playerPos = playerEntity[PositionComponent].position
    val yp = playerEntity[PositionComponent].yawPitch
    val viewableContexts = playerEntity[ViewableComponent].viewContextIDs

    when (tool) {
        Tool.CREATE_CUBE -> {
            val spawnPos = computeSpawnPos(playerPos, yp, worldRaycaster, viewableContexts, backOff = 0.5)
            entity {
                it += PhysicsComponent(physContextIDs = viewableContexts)
                it += PositionComponent(spawnPos)
                it += CubeModelComponent(material = Key.key("tnt"), transform = Transform.ZERO)
                it += ViewableComponent(viewableContexts)
            }
        }

        Tool.CREATE_SLAB -> {
            val slabSize = Vec3D(1.0, 0.5, 1.0)
            // back-off на половину высоты, чтобы плита легла на грань блока, а не вросла в него
            val spawnPos = computeSpawnPos(playerPos, yp, worldRaycaster, viewableContexts, backOff = 0.25)
            entity {
                it += PhysicsComponent(physContextIDs = viewableContexts, size = slabSize)
                it += PositionComponent(spawnPos)
                it += CubeModelComponent(material = Key.key("tnt"), transform = Transform(scale = slabSize))
                it += ViewableComponent(viewableContexts)
            }
        }

        Tool.GRAB -> {
            // Тоггл: если уже что-то держим — отпускаем; иначе raycast и берём первый PHYSICS-шейп.
            val existing = playerEntity.getOrNull(GrabbingComponent)
            if (existing != null) {
                playerEntity.configure { it -= GrabbingComponent }
            } else {
                val eye = playerPos + Vec3D(0.0, EYE_HEIGHT, 0.0)
                val dir = yp.direction()
                val request = FirstShapeHit()
                shapeRaycaster.raycast(
                    from = eye,
                    direction = dir,
                    maxDistance = RAYCAST_MAX_DISTANCE,
                    request = request,
                    filter = ShapeFilter { it is PhysicsCubeShape },
                )
                val cube = request.hit?.shape as? PhysicsCubeShape ?: return
                // distance до центра куба, а не до грани — куб будет висеть на расстоянии
                // на котором его центр был при хвате.
                val distance = (request.hit!!.transform.translation - eye).length()
                playerEntity.configure { it += GrabbingComponent(cube.physicsId, distance) }
            }
        }

        Tool.REMOVE -> {
            // Raycast по PHYSICS_OBJECTS — берём первый шейп под прицелом, ищем ECS-entity
            // по physicsId (он же owner у PhysicsCubeShape) и удаляем.
            val eye = playerPos + Vec3D(0.0, EYE_HEIGHT, 0.0)
            val dir = yp.direction()
            val request = FirstShapeHit()
            shapeRaycaster.raycast(
                from = eye,
                direction = dir,
                maxDistance = RAYCAST_MAX_DISTANCE,
                request = request,
                filter = ShapeFilter { it is PhysicsCubeShape },
            )
            val cube = request.hit?.shape as? PhysicsCubeShape ?: return
            findPhysicsEntity(cube.physicsId)?.remove()
        }

        Tool.SPAWN_CAR -> {
            // Машина — Jolt VehicleConstraint в PhysicsSpace, ECS держит только маркер
            // CarComponent. CarPhysicsLifecycleSystem подберёт это и создаст VehicleBody
            // при первом тике, CarModelSystem отдаст визуал в CarRenderer (chassis + 4 колеса).
            //
            // Spawn-lift = chassisHalfHeight + suspensionMax + tiny margin. Чтобы при
            // максимально-вытянутой подвеске wheels-bottom оказались чуть НАД terrain'ом
            // — иначе ray-cast подвески промахивается во время свободного падения, и
            // чассис ударяется коллизионным боксом о землю раньше чем подвеска успевает
            // сработать → лежит на пузе.
            val carSize = Vec3D(2.5, 1.5, 6.0)
            val wheelRadius = minOf(carSize.y, carSize.x) * 0.25
            val suspMax = wheelRadius * 2.0
            val lift = carSize.y * 0.5 + suspMax + 0.1
            val groundHit = computeSpawnPos(playerPos, yp, worldRaycaster, viewableContexts, backOff = 0.0)
            val spawnPos = groundHit + Vec3D(0.0, lift, 0.0)

            entity {
                it += CarComponent(physContextIDs = viewableContexts, chassisSize = carSize)
                it += PositionComponent(spawnPos)
                it += ViewableComponent(viewableContexts)
            }
        }

        Tool.INTERACT -> {
            // Универсальный «interact» — пока знает только про машины.
            val eye = playerPos + Vec3D(0.0, EYE_HEIGHT, 0.0)
            val dir = yp.direction()
            val request = FirstShapeHit()
            shapeRaycaster.raycast(
                from = eye,
                direction = dir,
                maxDistance = RAYCAST_MAX_DISTANCE,
                request = request,
                filter = ShapeFilter { it is PhysicsCubeShape },
            )
            val cube = request.hit?.shape as? PhysicsCubeShape ?: return
            val target = findPhysicsEntity(cube.physicsId) ?: return
            if (CarComponent in target) {
                // Игрок садится в машину: инпут и камера перенаправляются на car-entity.
                // RidingCarComponent на игроке — маркер для DemoPlayerHitboxDriver.
                // CameraTargetComponent живёт на ЦЕЛИ камеры (машина), а не на игроке —
                // фокус для камеры это точка машины, и сам игрок при этом не трогается.
                playerEntity.configure {
                    it += RidingCarComponent()
                    it -= InputTargetComponent
                }
                target.configure {
                    it += InputTargetComponent(playerUuid)
                    it += CameraTargetComponent(playerUuid, CameraMode.ThirdPerson(radius = 8.0))
                }
            }
        }
    }
}

private fun computeSpawnPos(
    playerPos: Vec3D,
    yp: YawPitch,
    raycaster: WorldRaycasterDispatcher,
    contexts: Set<String>,
    backOff: Double,
): Vec3D {
    val eye = playerPos + Vec3D(0.0, EYE_HEIGHT, 0.0)
    val dir = yp.direction()
    val hit = raycaster.raycast(eye, dir, RAYCAST_MAX_DISTANCE, contexts)
    return if (hit != null) hit.hitPos - dir * backOff
    else eye + dir * FALLBACK_DISTANCE
}

/** Захватывает первый шейп, в который попал луч, и останавливает обход. */
private class FirstShapeHit : RayCastRequest {
    var hit: ResolvedShape? = null

    override fun onShapeEnter(rayInfo: RayInfo, shape: ResolvedShape): Boolean {
        hit = shape
        return true
    }
}
