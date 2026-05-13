package ru.cherryngine.impl.demo

import jakarta.inject.Singleton
import ru.cherryngine.engine.core.instance.Instance
import ru.cherryngine.impl.demo.systems.*
import ru.cherryngine.lib.math.Vec3D

@Singleton
class DemoInit(instanceFactory: InstanceFactory) {
    val instance: Instance

    init {
        val lobbyPrefab = InstancePrefab(
            id = "lobby",
            platformIds = listOf("minecraft", "bedrock"),
            worlds = listOf(
                WorldLayerConfig("gm_construct", priority = 0),
                WorldLayerConfig("apart1", priority = 10),
                WorldLayerConfig("apart2", priority = 10, mutable = true),
            ),
            systems = listOf(
                PlayerInitSystem.Config("gm_construct", Vec3D(275.0, 56.0, 195.0)),
                CommandActionsConfig,
                AxolotlModelSystem.Config,
                CubeModelSystem.Config,
                ApartSystem.Config,
                CubePhysicsLifecycleSystem.Config,
                CarPhysicsLifecycleSystem.Config,
                GrabSystem.Config,
                CarDriveSystem.Config,
                PhysicsSimulationSystem.Config,
                CubePhysicsSyncSystem.Config,
                CarPhysicsSyncSystem.Config,
                CarModelSystem.Config,
                HitboxVisualizationSystem.Config,
                ToolSelectionDisplaySystem.Config,
                ViewContextSyncConfig,
                ClearEventsConfig,
            ),
        )

        instance = instanceFactory.create(lobbyPrefab)
    }
}
