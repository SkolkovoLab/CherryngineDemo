package ru.cherryngine.impl.demo.components

import com.github.quillraven.fleks.ComponentType
import ru.cherryngine.engine.core.shape.ShapeGroupRegistration
import ru.cherryngine.engine.ecs.EcsComponent
import ru.cherryngine.engine.ecs.EcsEntity
import ru.cherryngine.engine.ecs.EcsWorld

/**
 * Хранит токен регистрации шейпов entity в ShapeWorld.
 * Fleks автоматически вызывает onRemove при удалении компонента/entity —
 * там закрываем регистрацию, шейпы исчезают из ShapeWorld.
 */
class ShapeRegistrationComponent(
    val registration: ShapeGroupRegistration,
) : EcsComponent<ShapeRegistrationComponent> {
    override fun type() = ShapeRegistrationComponent

    override fun EcsWorld.onRemove(entity: EcsEntity) {
        registration.close()
    }

    companion object : ComponentType<ShapeRegistrationComponent>()
}
