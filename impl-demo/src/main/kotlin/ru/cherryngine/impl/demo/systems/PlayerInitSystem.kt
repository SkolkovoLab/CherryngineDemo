package ru.cherryngine.impl.demo.systems

import com.github.quillraven.fleks.IntervalSystem
import kotlinx.coroutines.channels.Channel
import net.kyori.adventure.key.Key
import org.slf4j.LoggerFactory
import ru.cherryngine.engine.core.instance.Instance
import ru.cherryngine.engine.ecs.components.PlayerComponent
import ru.cherryngine.engine.ecs.components.PositionComponent
import ru.cherryngine.engine.ecs.components.ViewableComponent
import ru.cherryngine.impl.demo.EcsSystemConfig
import ru.cherryngine.impl.demo.InstanceJoinChannel
import ru.cherryngine.impl.demo.InstanceLeaveChannel
import ru.cherryngine.impl.demo.components.AxolotlModelComponent
import ru.cherryngine.impl.demo.components.CubeModelComponent
import ru.cherryngine.impl.demo.components.HitboxVisualizationComponent
import ru.cherryngine.impl.demo.components.PhysicsComponent
import ru.cherryngine.lib.math.Transform
import ru.cherryngine.lib.math.Vec3D
import java.util.*

class PlayerInitSystem(
    private val joinChannel: Channel<UUID>,
    private val leaveChannel: Channel<UUID>,
    private val defaultViewContextID: String,
    private val spawnPosition: Vec3D,
) : IntervalSystem() {
    private val logger = LoggerFactory.getLogger(PlayerInitSystem::class.java)

    override fun onTick() {
        // Drain leave channel → remove entities
        val toRemove = mutableSetOf<UUID>()
        while (true) {
            val result = leaveChannel.tryReceive()
            if (result.isSuccess) toRemove.add(result.getOrThrow()) else break
        }
        if (toRemove.isNotEmpty()) {
            world.family { all(PlayerComponent) }.forEach {
                if (it[PlayerComponent].uuid in toRemove) {
                    it.remove()
                }
            }
            world.family { all(HitboxVisualizationComponent) }.forEach {
                if (it[HitboxVisualizationComponent].ownerUuid in toRemove) {
                    it.remove()
                }
            }
        }

        // Drain join channel → create entities
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
            logger.info("Creating ECS entity for player $playerUuid")
            world.entity {
                it += PlayerComponent(
                    playerUuid,
                    setOf(defaultViewContextID)
                )

                it += ViewableComponent(setOf(defaultViewContextID))

                it += PositionComponent(spawnPosition)

                it += AxolotlModelComponent()

                it += PhysicsComponent(
                    bodyInfo = PhysicsComponent.BodyInfo.Player,
                    physContextIDs = setOf(defaultViewContextID)
                )
            }

            // Визуализация хитбокса — отдельная entity
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
        override fun create(instance: Instance) =
            PlayerInitSystem(
                joinChannel = instance.get<InstanceJoinChannel>().channel,
                leaveChannel = instance.get<InstanceLeaveChannel>().channel,
                defaultViewContextID = spawnViewContext,
                spawnPosition = spawnPosition,
            )
    }
}
