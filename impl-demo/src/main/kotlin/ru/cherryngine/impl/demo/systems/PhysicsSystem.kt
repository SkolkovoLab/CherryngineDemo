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

class PhysicsSystem : IteratingSystem(
    family { all(PhysicsComponent) }
) {
    private val physicsSpace = PhysicsSpace()
    private val terrainGenerator = TerrainGenerator(physicsSpace)

    private val bodies = HashMap<EcsEntity, BodyEntry>()

    private data class BodyEntry(
        val body: PhysicsSpace.PhysicsBody,
        val physContextIDs: Set<String>,
    )

    override fun onTick() {
        onSort()

        // Удаляем тела для удалённых entity
        bodies.entries.removeIf { (entity, entry) ->
            if (entity !in world || PhysicsComponent !in entity) {
                physicsSpace.unregisterBodyContexts(entry.body)
                entry.body.remove()
                true
            } else false
        }

        // Собираем слои из ECS (аналог ViewSystem)
        val layers = collectLayers()

        // Собираем активные динамические тела для TerrainGenerator
        val activeBodies = mutableListOf<ActiveBodyInfo>()
        for ((_, entry) in bodies) {
            if (entry.physContextIDs.isEmpty()) continue
            activeBodies.add(ActiveBodyInfo(entry.body.getWorldBounds(), entry.physContextIDs))
        }

        // Обновляем terrain
        terrainGenerator.step(activeBodies, layers)

        // Тикаем физику
        physicsSpace.update(50f / 1000f)

        // Sync transform → ECS
        family.forEach { onTickEntity(it) }
    }

    override fun onTickEntity(entity: EcsEntity) {
        val physicsComponent = entity[PhysicsComponent]

        val entry = bodies.computeIfAbsent(entity) {
            when (physicsComponent.bodyInfo) {
                is PhysicsComponent.BodyInfo.Cube -> {
                    val spawnPos = entity.getOrNull(PositionComponent)?.position ?: Vec3D.ZERO
                    val body = physicsSpace.addCube(spawnPos, Vec3D.ONE)
                    if (physicsComponent.physContextIDs.isNotEmpty()) {
                        physicsSpace.registerBodyContexts(body, physicsComponent.physContextIDs)
                    }
                    BodyEntry(body, physicsComponent.physContextIDs)
                }
            }
        }

        if (physicsComponent.bodyInfo == PhysicsComponent.BodyInfo.Cube) {
            entity.configure {
                val transform = entry.body.getTransform()
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
