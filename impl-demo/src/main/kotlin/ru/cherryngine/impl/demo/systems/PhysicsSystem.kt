package ru.cherryngine.impl.demo.systems

import com.github.quillraven.fleks.IteratingSystem
import com.github.quillraven.fleks.World.Companion.family
import ru.cherryngine.engine.ecs.EcsEntity
import ru.cherryngine.engine.ecs.components.PositionComponent
import ru.cherryngine.engine.physics.PhysicsSpace
import ru.cherryngine.impl.demo.components.CubeModelComponent
import ru.cherryngine.impl.demo.components.PhysicsComponent
import ru.cherryngine.lib.math.Vec3D

class PhysicsSystem : IteratingSystem(
    family { all(PhysicsComponent) }
) {
    private val spaces = HashMap<String, PhysicsSpace>()

    private val bodies = HashMap<EcsEntity, PhysicsSpace.PhysicsBody>()

    override fun onTick() {
        onSort()

        // Удаляем старые PhysicsBody
        bodies.entries.removeIf { (entity, body) ->
            if (entity !in world || PhysicsComponent !in entity) {
                body.remove()
                true
            } else false
        }

        // Удаляем старые PhysicsSpace
        val activeSpaces = family.map { it[PhysicsComponent].spaceName }.toSet()
        spaces.entries.removeIf {
            if (it.key !in activeSpaces) {
                it.value.destroy()
                true
            } else false
        }

        // Тикаем PhysicsSpace
        spaces.values.forEach {
            it.update(50f / 1000f) // 50 mspt to seconds
        }

        // Тикаем Энтити
        family.forEach { onTickEntity(it) }
    }

    override fun onTickEntity(entity: EcsEntity) {
        val physicsComponent = entity[PhysicsComponent]
        val physicsSpace = spaces.computeIfAbsent(physicsComponent.spaceName) { PhysicsSpace() }

        val body = bodies.computeIfAbsent(entity) {
            when (val bodyInfo = physicsComponent.bodyInfo) {
                is PhysicsComponent.BodyInfo.Cube -> {
                    val spawnPos = entity.getOrNull(PositionComponent)?.position ?: Vec3D.ZERO
                    physicsSpace.addCube(spawnPos, Vec3D.ONE)
                }

                is PhysicsComponent.BodyInfo.Floor -> {
                    physicsSpace.addFloor(bodyInfo.y)
                }
            }
        }

        if (physicsComponent.bodyInfo == PhysicsComponent.BodyInfo.Cube) {
            entity.configure {
                val transform = body.getTransform()
                it.getOrNull(PositionComponent)?.position = transform.translation
                it.getOrNull(CubeModelComponent)?.transform = transform.copy(translation = Vec3D.ZERO)
            }
        }
    }
}