package ru.cherryngine.impl.demo.systems

import com.github.quillraven.fleks.IntervalSystem
import org.slf4j.LoggerFactory
import ru.cherryngine.engine.core.player.PlayerManager
import ru.cherryngine.engine.ecs.components.PlayerComponent
import ru.cherryngine.engine.ecs.components.PositionComponent
import ru.cherryngine.lib.math.Vec3D
import ru.cherryngine.engine.ecs.components.ViewableComponent
import ru.cherryngine.impl.demo.components.AxolotlModelComponent
import java.util.*

class PlayerInitSystem(
    val defaultViewContextID: String,
    val playerManager: PlayerManager,
) : IntervalSystem() {
    private val logger = LoggerFactory.getLogger(PlayerInitSystem::class.java)

    override fun onTick() {
        // Drain leave channel → remove entities
        val toRemove = mutableSetOf<UUID>()
        while (true) {
            val result = playerManager.playerLeaveChannel.tryReceive()
            if (result.isSuccess) toRemove.add(result.getOrThrow()) else break
        }
        if (toRemove.isNotEmpty()) {
            world.family { all(PlayerComponent) }.forEach {
                val playerComponent = it[PlayerComponent]
                if (playerComponent.uuid in toRemove) {
                    it.remove()
                }
            }
        }

        // Drain join channel → create entities
        val toCreate = mutableListOf<UUID>()
        while (true) {
            val result = playerManager.playerJoinChannel.tryReceive()
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

                it += PositionComponent(Vec3D(164.0, 58.0, 170.0))

                it += AxolotlModelComponent()
            }
        }
    }
}
