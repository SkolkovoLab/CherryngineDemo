package ru.cherryngine.impl.demo.systems

import ru.cherryngine.engine.core.instance.Instance
import ru.cherryngine.engine.ecs.systems.*
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
    override fun create(instance: Instance) = ViewContextSyncSystem(instance.get(), instance.get())
}

object ClearEventsConfig : EcsSystemConfig {
    override fun create(instance: Instance) = ClearEventsSystem()
}
