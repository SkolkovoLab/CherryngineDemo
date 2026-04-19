package ru.cherryngine.impl.demo.systems

import com.github.quillraven.fleks.IntervalSystem
import kotlinx.coroutines.channels.Channel
import net.kyori.adventure.key.Key
import org.slf4j.LoggerFactory
import ru.cherryngine.engine.core.instance.Instance
import ru.cherryngine.engine.core.player.Player
import ru.cherryngine.engine.core.player.PlayerManager
import ru.cherryngine.engine.ecs.components.PlayerComponent
import ru.cherryngine.engine.ecs.components.PositionComponent
import ru.cherryngine.engine.ecs.components.ViewableComponent
import ru.cherryngine.impl.demo.EcsSystemConfig
import ru.cherryngine.impl.demo.InstanceJoinChannel
import ru.cherryngine.impl.demo.InstanceLeaveChannel
import ru.cherryngine.impl.demo.PlayerPhysicsState
import ru.cherryngine.impl.demo.components.AxolotlModelComponent
import ru.cherryngine.impl.demo.components.CubeModelComponent
import ru.cherryngine.impl.demo.components.HitboxVisualizationComponent
import ru.cherryngine.impl.demo.components.PhysicsComponent
import ru.cherryngine.impl.demo.renderer.PlayerRenderer
import ru.cherryngine.lib.math.Transform
import ru.cherryngine.lib.math.Vec3D
import java.util.*

class PlayerInitSystem(
    private val joinChannel: Channel<UUID>,
    private val leaveChannel: Channel<Player>,
    private val playerRenderers: List<PlayerRenderer>,
    private val playerManager: PlayerManager,
    private val playerPhysicsState: PlayerPhysicsState,
    private val defaultViewContextID: String,
    private val spawnPosition: Vec3D,
) : IntervalSystem() {
    private val logger = LoggerFactory.getLogger(PlayerInitSystem::class.java)

    override fun onTick() {
        val toRemove = mutableListOf<Player>()
        while (true) {
            val result = leaveChannel.tryReceive()
            if (result.isSuccess) toRemove.add(result.getOrThrow()) else break
        }
        if (toRemove.isNotEmpty()) {
            val toRemoveUUIDs = toRemove.mapTo(HashSet()) { it.uuid }
            toRemove.forEach { player ->
                playerRenderers.forEach { it.onLeave(player) }
            }
            toRemoveUUIDs.forEach { uuid -> playerPhysicsState.unregister(uuid) }
            world.family { all(PlayerComponent) }.forEach {
                if (it[PlayerComponent].uuid in toRemoveUUIDs) it.remove()
            }
            world.family { all(HitboxVisualizationComponent) }.forEach {
                if (it[HitboxVisualizationComponent].ownerUuid in toRemoveUUIDs) it.remove()
            }
        }

        val toCreate = mutableListOf<UUID>()
        while (true) {
            val result = joinChannel.tryReceive()
            if (result.isSuccess) toCreate.add(result.getOrThrow()) else break
        }
        val existingUUIDs = mutableSetOf<UUID>()
        world.family { all(PlayerComponent) }.forEach {
            existingUUIDs.add(it[PlayerComponent].uuid)
        }
        toCreate.forEach { playerUuid ->
            if (playerUuid in existingUUIDs) return@forEach
            val player = playerManager.getPlayerNullable(playerUuid) ?: return@forEach

            logger.info("Creating ECS entity for player $playerUuid")
            playerRenderers.forEach { it.onJoin(player) }

            world.entity {
                it += PlayerComponent(playerUuid, setOf(defaultViewContextID))
                it += ViewableComponent(setOf(defaultViewContextID))
                it += PositionComponent(spawnPosition)
                it += AxolotlModelComponent()
                it += PhysicsComponent(
                    bodyInfo = PhysicsComponent.BodyInfo.Player,
                    physContextIDs = setOf(defaultViewContextID)
                )
            }

            world.entity {
                it += PositionComponent(spawnPosition)
                it += ViewableComponent(setOf(defaultViewContextID))
                it += CubeModelComponent(
                    material = Key.key("red_stained_glass"),
                    transform = Transform(scale = Vec3D(0.6, 1.8, 0.6))
                )
                it += HitboxVisualizationComponent(playerUuid)
            }
        }
    }

    data class Config(
        val spawnViewContext: String,
        val spawnPosition: Vec3D,
    ) : EcsSystemConfig {
        override fun create(instance: Instance) = PlayerInitSystem(
            joinChannel = instance.get<InstanceJoinChannel>().channel,
            leaveChannel = instance.get<InstanceLeaveChannel>().channel,
            playerRenderers = instance.getAll(),
            playerManager = instance.get(),
            playerPhysicsState = instance.get(),
            defaultViewContextID = spawnViewContext,
            spawnPosition = spawnPosition,
        )
    }
}
