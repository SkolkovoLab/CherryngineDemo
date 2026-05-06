package ru.cherryngine.impl.demo.components

import ru.cherryngine.engine.ecs.EcsEntity
import ru.cherryngine.engine.ecs.EcsWorld
import java.util.UUID

/**
 * Находит ECS-entity, у которой physicsId совпадает с заданным.
 * Проверяет PhysicsComponent (кубы/плиты) И CarComponent (машины) — это два
 * разных физических объекта в PhysicsSpace, но raycast по PhysicsCubeShape
 * возвращает один и тот же UUID, и lookup'у надо смотреть в обе стороны.
 */
fun EcsWorld.findPhysicsEntity(physicsId: UUID): EcsEntity? {
    family { all(PhysicsComponent) }
        .firstOrNull { it[PhysicsComponent].physicsId == physicsId }
        ?.let { return it }
    family { all(CarComponent) }
        .firstOrNull { it[CarComponent].carPhysicsId == physicsId }
        ?.let { return it }
    return null
}
