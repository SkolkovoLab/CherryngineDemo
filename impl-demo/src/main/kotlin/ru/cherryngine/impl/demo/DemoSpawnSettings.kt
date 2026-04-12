package ru.cherryngine.impl.demo

import io.micronaut.context.annotation.Primary
import jakarta.inject.Singleton
import ru.cherryngine.engine.core.services.SpawnSettings
import ru.cherryngine.lib.math.Vec3D

@Singleton
@Primary
class DemoSpawnSettings : SpawnSettings {
    override val position = Vec3D(164.0, 58.0, 170.0)
}
