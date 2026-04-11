package ru.cherryngine.impl.demo.systems

import com.github.quillraven.fleks.IteratingSystem
import com.github.quillraven.fleks.World.Companion.family
import ru.cherryngine.engine.core.PlayerManager
import ru.cherryngine.engine.ecs.EcsEntity
import ru.cherryngine.engine.ecs.components.PlayerComponent
import ru.cherryngine.engine.ecs.components.PositionComponent
import ru.cherryngine.engine.ecs.components.ViewableComponent
import ru.cherryngine.impl.demo.components.AxolotlModelComponent
import ru.cherryngine.impl.demo.view.AxolotlView
import ru.cherryngine.impl.demo.view.AxolotlViewFactory
import java.util.UUID

class AxolotlModelSystem(
    private val viewFactory: AxolotlViewFactory,
    private val playerManager: PlayerManager,
) : IteratingSystem(
    family { all(AxolotlModelComponent) }
) {
    private val views = HashMap<UUID, AxolotlView>()

    override fun onTick() {
        val activeIds = mutableSetOf<UUID>()
        family.forEach { activeIds.add(it[AxolotlModelComponent].modelId) }
        views.keys.removeIf { uuid ->
            if (uuid !in activeIds) { views[uuid]?.destroy(); true } else false
        }
        super.onTick()
    }

    override fun onTickEntity(entity: EcsEntity) {
        val component = entity[AxolotlModelComponent]
        val view = views.getOrPut(component.modelId) { viewFactory.create() }

        entity.getOrNull(PositionComponent)?.also {
            view.updatePosition(it.position, it.yawPitch)
        }

        val playerUuid = entity.getOrNull(PlayerComponent)?.uuid
        val name = playerUuid?.let { playerManager.getPlayerNullable(it)?.username }
        view.setName(name)
        view.setHiddenFromPlayer(playerUuid)

        entity.getOrNull(ViewableComponent)?.also {
            view.setViewContextIDs(it.viewContextIDs)
        }
    }
}
