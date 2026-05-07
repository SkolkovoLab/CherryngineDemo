package ru.cherryngine.impl.demo.components

import com.github.quillraven.fleks.ComponentType
import ru.cherryngine.engine.ecs.EcsComponent

/**
 * Маркер-флаг на player-entity: игрок сидит в машине. Используется
 * [DemoPlayerHitboxDriver] для короткого замыкания (хитбокс игрока не
 * симулируется пока он в машине). Какой именно машины — не хранится:
 * единственный читатель не интересуется конкретной машиной, а сам водитель
 * однозначно определяется через `InputTargetComponent` на car-entity.
 */
class RidingCarComponent : EcsComponent<RidingCarComponent> {
    override fun type() = RidingCarComponent

    companion object : ComponentType<RidingCarComponent>()
}
