package ru.cherryngine.impl.demo.systems

import ru.cherryngine.engine.ecs.systems.ClearEventsSystem
import ru.cherryngine.engine.ecs.systems.CommandActionsSystem
import ru.cherryngine.engine.ecs.systems.ReadClientPositionSystem
import ru.cherryngine.engine.ecs.systems.ViewContextSyncSystem
import ru.cherryngine.engine.ecs.systems.WriteClientPositionSystem
import ru.cherryngine.impl.demo.EcsSystemConfig
import ru.cherryngine.impl.demo.InstanceScope

object ReadClientPositionConfig : EcsSystemConfig {
    override fun create(scope: InstanceScope) = ReadClientPositionSystem(scope.inputProvider)
}

object WriteClientPositionConfig : EcsSystemConfig {
    override fun create(scope: InstanceScope) = WriteClientPositionSystem(scope.outputProvider)
}

object CommandActionsConfig : EcsSystemConfig {
    override fun create(scope: InstanceScope) = CommandActionsSystem()
}

object ViewContextSyncConfig : EcsSystemConfig {
    override fun create(scope: InstanceScope) = ViewContextSyncSystem(scope.worldService, scope.playerManager)
}

object ClearEventsConfig : EcsSystemConfig {
    override fun create(scope: InstanceScope) = ClearEventsSystem()
}
