package ru.cherryngine.impl.demo.systems

import com.github.quillraven.fleks.IteratingSystem
import com.github.quillraven.fleks.World.Companion.family
import org.slf4j.LoggerFactory
import ru.cherryngine.engine.core.player.PlayerManager
import ru.cherryngine.engine.ecs.EcsEntity
import ru.cherryngine.engine.ecs.components.PlayerComponent
import ru.cherryngine.engine.ecs.components.PositionComponent
import ru.cherryngine.engine.ecs.components.ViewableComponent
import ru.cherryngine.engine.ecs.events.PacketsEvent
import ru.cherryngine.impl.demo.components.AxolotlModelComponent
import ru.cherryngine.lib.minecraft.network.protocol.packets.ServerboundPacket
import ru.cherryngine.lib.minecraft.network.protocol.packets.configurations.ServerboundFinishConfigurationPacket
import ru.cherryngine.lib.minecraft.network.protocol.packets.play.clientbound.ClientboundGameEventPacket
import ru.cherryngine.lib.minecraft.network.protocol.packets.play.clientbound.ClientboundLoginPacket
import ru.cherryngine.lib.minecraft.network.protocol.types.GameMode
import ru.cherryngine.lib.minecraft.registry.Registries
import ru.cherryngine.lib.minecraft.registry.keys.DimensionTypes
import java.util.*

class PlayerInitSystem(
    val defaultViewContextID: String,
    val playerManager: PlayerManager,
) : IteratingSystem(
    family { all(PlayerComponent) }
) {
    private val logger = LoggerFactory.getLogger(PlayerInitSystem::class.java)
    private var tickPackets: Map<UUID, MutableList<ServerboundPacket>> = emptyMap()

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
//                    it.remove()
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
        toCreate.forEach { player ->
            if (player in existingUUIDs) return@forEach
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

        // Drain packet channel → build local map
        val packets = mutableMapOf<UUID, MutableList<ServerboundPacket>>()
        while (true) {
            val result = playerManager.packetChannel.tryReceive()
            if (result.isSuccess) {
                val (uuid, packet) = result.getOrThrow()
                packets.getOrPut(uuid) { mutableListOf() }.add(packet)
            } else break
        }
        tickPackets = packets

        super.onTick()
    }

    override fun onTickEntity(entity: EcsEntity) {
        val playerComponent = entity[PlayerComponent]
        val uuid = playerComponent.uuid
        val packets = tickPackets[uuid] ?: return

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
                        Registries.dimensionType[DimensionTypes.OVERWORLD].value,
                        "world",
                        0L,
                        GameMode.CREATIVE,
                        GameMode.CREATIVE,
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