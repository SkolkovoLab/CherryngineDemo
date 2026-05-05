package ru.cherryngine.impl.demo.systems

import net.kyori.adventure.key.Key
import ru.cherryngine.engine.core.world.WorldRaycasterDispatcher
import ru.cherryngine.engine.ecs.EcsEntity
import ru.cherryngine.engine.ecs.EcsWorld
import ru.cherryngine.engine.ecs.components.PlayerComponent
import ru.cherryngine.engine.ecs.components.PositionComponent
import ru.cherryngine.engine.ecs.components.ViewableComponent
import ru.cherryngine.engine.ecs.getPlayerEntityOrNull
import ru.cherryngine.impl.demo.components.CubeModelComponent
import ru.cherryngine.impl.demo.components.PhysicsComponent
import ru.cherryngine.impl.demo.components.SelectedToolComponent
import ru.cherryngine.impl.demo.components.Tool
import ru.cherryngine.lib.math.Transform
import ru.cherryngine.lib.math.Vec3D
import ru.cherryngine.lib.math.YawPitch
import java.util.UUID

private const val EYE_HEIGHT = 1.62
private const val RAYCAST_MAX_DISTANCE = 50.0
private const val FALLBACK_DISTANCE = 5.0

fun EcsWorld.useTool(playerUuid: UUID, raycaster: WorldRaycasterDispatcher) {
    val playerEntity = getPlayerEntityOrNull(playerUuid) ?: return
    val tool = playerEntity.getOrNull(SelectedToolComponent)?.tool ?: return
    val playerPos = playerEntity[PositionComponent].position
    val yp = playerEntity[PositionComponent].yawPitch
    val viewableContexts = playerEntity[ViewableComponent].viewContextIDs

    when (tool) {
        Tool.CREATE_CUBE -> {
            val spawnPos = computeSpawnPos(playerPos, yp, raycaster, viewableContexts, backOff = 0.5)
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
            val spawnPos = computeSpawnPos(playerPos, yp, raycaster, viewableContexts, backOff = 0.25)
            entity {
                it += PhysicsComponent(physContextIDs = viewableContexts, size = slabSize)
                it += PositionComponent(spawnPos)
                it += CubeModelComponent(material = Key.key("tnt"), transform = Transform(scale = slabSize))
                it += ViewableComponent(viewableContexts)
            }
        }

        Tool.REMOVE_NEAREST -> {
            var closest: EcsEntity? = null
            var closestDistSq = Double.MAX_VALUE
            family { all(PhysicsComponent, PositionComponent) }.forEach { e ->
                if (PlayerComponent in e) return@forEach
                val distSq = (e[PositionComponent].position - playerPos).lengthSquared()
                if (distSq < closestDistSq) {
                    closestDistSq = distSq
                    closest = e
                }
            }
            closest?.remove()
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
