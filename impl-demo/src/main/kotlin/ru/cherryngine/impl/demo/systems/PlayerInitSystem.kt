package ru.cherryngine.impl.demo.systems

import com.github.quillraven.fleks.IteratingSystem
import com.github.quillraven.fleks.World.Companion.family
import org.slf4j.LoggerFactory
import ru.cherryngine.engine.core.PlayerManager
import ru.cherryngine.engine.ecs.EcsEntity
import ru.cherryngine.engine.ecs.components.PlayerComponent
import ru.cherryngine.engine.ecs.components.PositionComponent
import ru.cherryngine.engine.ecs.components.ViewableComponent
import ru.cherryngine.engine.ecs.events.PacketsEvent
import ru.cherryngine.impl.demo.components.AxolotlModelComponent
import ru.cherryngine.lib.minecraft.protocol.packets.configurations.ServerboundFinishConfigurationPacket
import ru.cherryngine.lib.minecraft.protocol.packets.play.clientbound.ClientboundGameEventPacket
import ru.cherryngine.lib.minecraft.protocol.packets.play.clientbound.ClientboundLoginPacket
import ru.cherryngine.lib.minecraft.protocol.types.GameMode
import ru.cherryngine.lib.minecraft.registry.DimensionTypes
import java.util.*

class PlayerInitSystem(
    val defaultViewContextID: String,
    val playerManager: PlayerManager,
) : IteratingSystem(
    family { all(PlayerComponent) }
) {
    private val logger = LoggerFactory.getLogger(PlayerInitSystem::class.java)

    override fun onTick() {
        val skipCreate = mutableSetOf<UUID>()
        world.family { all(PlayerComponent) }.forEach {
            val playerComponent = it[PlayerComponent]
            if (playerComponent.uuid in playerManager.toCreatePlayers) {
                skipCreate.add(playerComponent.uuid)
            }
            if (playerComponent.uuid in playerManager.toRemovePlayers) {
//                it.remove()
            }
        }
        playerManager.toRemovePlayers.clear()

        playerManager.toCreatePlayers.forEach { player ->
            if (player in skipCreate) return@forEach
            logger.info("Creating ECS entity for player $player")
            world.entity {
                it += PlayerComponent(
                    player,
                    setOf(defaultViewContextID)
                )

                it += ViewableComponent(setOf(defaultViewContextID))

                it += PositionComponent()

                it += AxolotlModelComponent
            }
        }
        playerManager.toCreatePlayers.clear()

        super.onTick()
    }

    override fun onTickEntity(entity: EcsEntity) {
        val playerComponent = entity[PlayerComponent]
        val uuid = playerComponent.uuid
        val packets = playerManager.queues.remove(uuid) ?: return

        entity.configure {
            it += PacketsEvent(packets)
        }

        val player = playerManager.getPlayerNullable(uuid) ?: return

        packets.forEach { packet ->
            if (packet is ServerboundFinishConfigurationPacket) {
                player.connection.sendPacket(
                    ClientboundLoginPacket(
                        0,
                        false,
                        listOf(),
                        20,
                        8,
                        8,
                        false,
                        true,
                        false,
                        DimensionTypes.OVERWORLD,
                        "world",
                        0L,
                        GameMode.SURVIVAL,
                        GameMode.SURVIVAL,
                        false,
                        false,
                        null,
                        0,
                        32,
                        false
                    )
                )

                val positionComponent = entity[PositionComponent]
                player.teleport(positionComponent.position, positionComponent.yawPitch)

                player.connection.sendPacket(
                    ClientboundGameEventPacket(
                        ClientboundGameEventPacket.GameEvent.START_WAITING_FOR_CHUNKS,
                        0f
                    )
                )
            }
        }
    }
}