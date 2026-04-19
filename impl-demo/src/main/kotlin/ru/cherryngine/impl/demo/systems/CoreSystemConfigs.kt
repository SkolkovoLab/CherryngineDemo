package ru.cherryngine.impl.demo.systems

import ru.cherryngine.engine.core.instance.Instance
import ru.cherryngine.engine.ecs.systems.CommandActionsSystem
import ru.cherryngine.engine.ecs.systems.ReadClientPositionSystem
import ru.cherryngine.engine.ecs.systems.WriteClientPositionSystem
import ru.cherryngine.impl.demo.EcsSystemConfig

object ReadClientPositionConfig : EcsSystemConfig {
    override fun create(instance: Instance) = ReadClientPositionSystem(instance.get())
}

object WriteClientPositionConfig : EcsSystemConfig {
    override fun create(instance: Instance) = WriteClientPositionSystem(instance.get())
}

object CommandActionsConfig : EcsSystemConfig {
    override fun create(instance: Instance) = CommandActionsSystem()
}

object ViewContextSyncConfig : EcsSystemConfig {
    override fun create(instance: Instance) = ViewContextSyncSystem(
        playerRenderers = instance.getAll(),
        playerManager = instance.get(),
    )
}

