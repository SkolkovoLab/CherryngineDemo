package ru.cherryngine.impl.demo.systems

import com.github.quillraven.fleks.IteratingSystem
import com.github.quillraven.fleks.World.Companion.family
import ru.cherryngine.engine.core.instance.ServerWorld
import ru.cherryngine.engine.ecs.EcsEntity
import ru.cherryngine.engine.ecs.components.PositionComponent
import ru.cherryngine.engine.physics.PhysicsSpace
import ru.cherryngine.engine.physics.terrain.ActiveBodyInfo
import ru.cherryngine.engine.physics.terrain.LayerWithContext
import ru.cherryngine.engine.physics.terrain.TerrainGenerator
import ru.cherryngine.impl.demo.components.CubeModelComponent
import ru.cherryngine.impl.demo.components.PhysicsComponent
import ru.cherryngine.lib.math.Vec3D

class PhysicsSystem(
    private val physicsSpace: PhysicsSpace,
    private val terrainGenerator: TerrainGenerator,
    private val serverWorld: ServerWorld,
) : IteratingSystem(
    family { all(PhysicsComponent) }
) {
    override fun onTick() {
        physicsSpace.beginTick()

        // keepAlive + создание тел
        family.forEach { entity ->
            val comp = entity[PhysicsComponent]
            val pos = entity.getOrNull(PositionComponent)?.position ?: Vec3D.ZERO
            physicsSpace.keepAlive(comp.physicsId)
            physicsSpace.getOrCreateBody(comp.physicsId, comp.physContextIDs) {
                when (comp.bodyInfo) {
                    is PhysicsComponent.BodyInfo.Cube -> physicsSpace.addCube(pos, Vec3D.ONE)
                }
            }
        }

        // Собираем активные тела для TerrainGenerator
        val delta = 50f / 1000f
        val activeBodies = family.mapNotNull { entity ->
            val comp = entity[PhysicsComponent]
            if (comp.physContextIDs.isEmpty()) return@mapNotNull null
            val body = physicsSpace.getOrCreateBody(comp.physicsId, comp.physContextIDs) {
                when (comp.bodyInfo) {
                    is PhysicsComponent.BodyInfo.Cube -> physicsSpace.addCube(Vec3D.ZERO, Vec3D.ONE)
                }
            }
            ActiveBodyInfo(body.getWorldBounds(), body.getLinearVelocity(), comp.physContextIDs)
        }

        val layers = serverWorld.getLayersByContext()
            .flatMap { (contextID, entries) ->
                val dt = serverWorld.dimensionType ?: return@flatMap emptyList()
                entries.map { LayerWithContext(it, setOf(contextID), dt) }
            }
        terrainGenerator.step(delta, activeBodies, layers)

        physicsSpace.update(delta)

        // Sync transform → ECS
        family.forEach { onTickEntity(it) }

        physicsSpace.endTick()
    }

    override fun onTickEntity(entity: EcsEntity) {
        val comp = entity[PhysicsComponent]
        val body = physicsSpace.getOrCreateBody(comp.physicsId, comp.physContextIDs) {
            when (comp.bodyInfo) {
                is PhysicsComponent.BodyInfo.Cube -> physicsSpace.addCube(Vec3D.ZERO, Vec3D.ONE)
            }
        }

        if (comp.bodyInfo == PhysicsComponent.BodyInfo.Cube) {
            entity.configure {
                val transform = body.getTransform()
                it.getOrNull(PositionComponent)?.position = transform.translation
                it.getOrNull(CubeModelComponent)?.transform = transform.copy(translation = Vec3D.ZERO)
            }
        }
    }
}
