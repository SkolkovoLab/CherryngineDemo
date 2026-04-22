package ru.cherryngine.impl.demo.systems

import com.github.quillraven.fleks.IteratingSystem
import com.github.quillraven.fleks.World.Companion.family
import ru.cherryngine.engine.core.instance.Instance
import ru.cherryngine.engine.core.player.PlayerManager
import ru.cherryngine.engine.ecs.EcsEntity
import ru.cherryngine.engine.ecs.components.PlayerComponent
import ru.cherryngine.engine.ecs.components.PositionComponent
import ru.cherryngine.engine.ecs.components.ViewableComponent
import ru.cherryngine.impl.demo.EcsSystemConfig
import ru.cherryngine.impl.demo.components.AxolotlModelComponent
import ru.cherryngine.impl.demo.renderer.AxolotlRendererDispatcher
import ru.cherryngine.lib.math.Vec3D
import ru.cherryngine.lib.math.YawPitch
import java.util.*

class AxolotlModelSystem(
    private val dispatcher: AxolotlRendererDispatcher,
    private val playerManager: PlayerManager,
) : IteratingSystem(
    family { all(AxolotlModelComponent) }
) {
    private val activeIds = HashSet<UUID>()

    override fun onTick() {
        val currentIds = mutableSetOf<UUID>()
        family.forEach { currentIds.add(it[AxolotlModelComponent].modelId) }

        activeIds.removeIf { id ->
            if (id !in currentIds) {
                dispatcher.onRemove(id)
                true
            } else false
        }

        currentIds.forEach { id ->
            if (activeIds.add(id)) {
                dispatcher.onAdd(id)
            }
        }

        super.onTick()
    }

    override fun onTickEntity(entity: EcsEntity) {
        val id = entity[AxolotlModelComponent].modelId
        val pos = entity.getOrNull(PositionComponent)
        val playerUuid = entity.getOrNull(PlayerComponent)?.uuid
        val name = playerUuid?.let { playerManager.getPlayerNullable(it)?.username }
        val viewContextIDs = entity.getOrNull(ViewableComponent)?.viewContextIDs ?: emptySet()

        dispatcher.update(
            id,
            pos?.position ?: Vec3D.ZERO,
            pos?.yawPitch ?: YawPitch.ZERO,
            name,
            playerUuid,
            viewContextIDs,
        )
    }

    object Config : EcsSystemConfig {
        override fun create(instance: Instance) =
            AxolotlModelSystem(instance.get(), instance.get())
    }
}
