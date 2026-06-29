package ru.cherryngine.impl.demo.systems

import ru.cherryngine.engine.core.instance.Instance
import ru.cherryngine.engine.ecs.systems.CommandActionsSystem
import ru.cherryngine.impl.demo.EcsSystemConfig

object CommandActionsConfig : EcsSystemConfig {
    override fun create(instance: Instance) = CommandActionsSystem()
}

object ViewContextSyncConfig : EcsSystemConfig {
    override fun create(instance: Instance) = ViewContextSyncSystem(
        playerRendererDispatcher = instance.get(),
        playerManager = instance.get(),
    )
}
