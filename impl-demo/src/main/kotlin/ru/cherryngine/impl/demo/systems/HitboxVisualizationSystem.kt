package ru.cherryngine.impl.demo.systems

import com.github.quillraven.fleks.IteratingSystem
import com.github.quillraven.fleks.World.Companion.family
import ru.cherryngine.engine.core.instance.Instance
import ru.cherryngine.engine.core.player.PlayerManager
import ru.cherryngine.engine.ecs.EcsEntity
import ru.cherryngine.engine.ecs.components.PositionComponent
import ru.cherryngine.engine.ecs.components.ViewableComponent
import ru.cherryngine.engine.physics.PhysicsSpace
import ru.cherryngine.impl.demo.EcsSystemConfig
import ru.cherryngine.impl.demo.PlayerPhysicsState
import ru.cherryngine.impl.demo.components.HitboxVisualizationComponent

/**
 * Синхронизирует entity-визуализацию хитбокса (красное стекло 0.6x1.8x0.6)
 * с реальной позицией jolt-тела хитбокса своего владельца.
 *
 * Владелец определяется [HitboxVisualizationComponent.ownerUuid].
 * Позиция хитбокса берётся через [PlayerPhysicsState] → [PhysicsSpace].
 * viewContextIDs визуализации синкаются с контекстами игрока-владельца.
 */
class HitboxVisualizationSystem(
    private val physicsSpace: PhysicsSpace,
    private val playerPhysicsState: PlayerPhysicsState,
    private val playerManager: PlayerManager,
) : IteratingSystem(
    family { all(HitboxVisualizationComponent, PositionComponent, ViewableComponent) }
) {
    override fun onTickEntity(entity: EcsEntity) {
        val ownerUuid = entity[HitboxVisualizationComponent].ownerUuid
        val physicsId = playerPhysicsState.getPhysicsId(ownerUuid) ?: return
        val player = playerManager.getPlayerNullable(ownerUuid) ?: return
        val transform = physicsSpace.getBodyTransform(physicsId) ?: return

        // item_display рендерит item вокруг entity position — нужен центр body,
        // не bottom, иначе scale=1.8 уезжает на 0.9 ниже хитбокса.
        entity[PositionComponent].position = transform.translation
        entity[ViewableComponent].viewContextIDs = player.viewContextIDs
    }

    object Config : EcsSystemConfig {
        override fun create(instance: Instance) = HitboxVisualizationSystem(
            physicsSpace = instance.get(),
            playerPhysicsState = instance.get(),
            playerManager = instance.get(),
        )
    }
}
