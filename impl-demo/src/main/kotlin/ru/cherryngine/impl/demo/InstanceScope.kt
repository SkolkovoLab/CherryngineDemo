package ru.cherryngine.impl.demo

import kotlinx.coroutines.channels.Channel
import ru.cherryngine.engine.core.instance.ServerWorld
import ru.cherryngine.engine.core.player.InstanceRouter
import ru.cherryngine.engine.core.player.PlayerInputProvider
import ru.cherryngine.engine.core.player.PlayerManager
import ru.cherryngine.engine.core.player.PlayerOutputProvider
import ru.cherryngine.engine.core.services.WorldService
import ru.cherryngine.engine.physics.PhysicsSpace
import ru.cherryngine.engine.physics.terrain.TerrainGenerator
import ru.cherryngine.impl.demo.renderer.AxolotlRenderer
import ru.cherryngine.impl.demo.renderer.CubeRenderer
import java.util.*
import kotlin.reflect.KClass

class InstanceScope(
    val instanceId: String,
    val serverWorld: ServerWorld,
    val inputProvider: PlayerInputProvider,
    val outputProvider: PlayerOutputProvider,
    val axolotlRenderers: List<AxolotlRenderer>,
    val cubeRenderers: List<CubeRenderer>,
    val playerManager: PlayerManager,
    val worldService: WorldService,
    val instanceRouter: InstanceRouter,
) {
    val joinChannel = Channel<UUID>(Channel.UNLIMITED)
    val leaveChannel = Channel<UUID>(Channel.UNLIMITED)

    init {
        instanceRouter.register(instanceId, joinChannel, leaveChannel)
    }

    private val beans = HashMap<KClass<*>, Any>()

    @Suppress("UNCHECKED_CAST")
    fun <T : Any> getOrCreate(type: KClass<T>, factory: () -> T): T =
        beans.getOrPut(type) { factory() } as T

    val physicsSpace: PhysicsSpace by lazy {
        getOrCreate(PhysicsSpace::class) { PhysicsSpace() }
    }
    val terrainGenerator: TerrainGenerator by lazy {
        getOrCreate(TerrainGenerator::class) { TerrainGenerator(physicsSpace) }
    }
}
