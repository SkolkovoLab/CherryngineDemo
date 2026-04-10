package ru.cherryngine.impl.demo.systems

import com.github.quillraven.fleks.IteratingSystem
import com.github.quillraven.fleks.World.Companion.family
import ru.cherryngine.engine.ecs.EcsEntity
import ru.cherryngine.engine.ecs.components.PositionComponent
import ru.cherryngine.engine.ecs.components.ViewableComponent
import ru.cherryngine.engine.ecs.events.ViewableProvidersEvent
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
        val activeBodies = family.mapNotNull { entity ->
            val comp = entity[PhysicsComponent]
            if (comp.physContextIDs.isEmpty()) return@mapNotNull null
            val body = physicsSpace.getOrCreateBody(comp.physicsId, comp.physContextIDs) {
                when (comp.bodyInfo) {
                    is PhysicsComponent.BodyInfo.Cube -> physicsSpace.addCube(Vec3D.ZERO, Vec3D.ONE)
                }
            }
            ActiveBodyInfo(body.getWorldBounds(), comp.physContextIDs)
        }

        val layers = collectLayers()
        terrainGenerator.step(activeBodies, layers)

        physicsSpace.update(50f / 1000f)

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

    private fun collectLayers(): List<LayerWithContext> {
        val result = mutableListOf<LayerWithContext>()
        world.family { all(ViewableComponent, ViewableProvidersEvent) }.forEach { viewableEntity ->
            val viewableComponent = viewableEntity[ViewableComponent]
            val event = viewableEntity[ViewableProvidersEvent]
            val dimensionType = event.dimensionType ?: return@forEach
            for (layerEntry in event.layers) {
                result.add(LayerWithContext(layerEntry, viewableComponent.viewContextIDs, dimensionType))
            }
        }
        return result
    }
}
