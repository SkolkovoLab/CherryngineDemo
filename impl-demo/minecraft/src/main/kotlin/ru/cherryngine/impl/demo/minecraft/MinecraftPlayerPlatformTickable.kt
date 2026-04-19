package ru.cherryngine.impl.demo.minecraft

import ru.cherryngine.engine.core.instance.InstanceSingleton
import ru.cherryngine.engine.core.instance.TickStage
import ru.cherryngine.engine.core.instance.Tickable
import ru.cherryngine.engine.core.player.PlayerManager
import ru.cherryngine.engine.minecraft.entity.McEntity
import ru.cherryngine.engine.minecraft.entity.McEntityRegistry
import ru.cherryngine.engine.minecraft.player.MinecraftPlayer
import ru.cherryngine.engine.physics.PhysicsSpace
import ru.cherryngine.impl.demo.PlayerPhysicsState
import ru.cherryngine.lib.math.Vec3D
import ru.cherryngine.lib.math.YawPitch
import ru.cherryngine.lib.minecraft.entity.EntityMeta
import ru.cherryngine.lib.minecraft.entity.ShulkerMeta
import ru.cherryngine.lib.minecraft.network.protocol.packets.play.clientbound.ClientboundSetPassengersPacket
import ru.cherryngine.lib.minecraft.network.protocol.types.Direction
import ru.cherryngine.lib.minecraft.registry.Registries
import ru.cherryngine.lib.minecraft.registry.keys.EntityTypes
import java.util.*
import kotlin.math.roundToInt
import kotlin.random.Random
import kotlin.time.Duration

@InstanceSingleton(platform = "minecraft", stage = TickStage.POST)
class MinecraftPlayerPlatformTickable(
    private val playerManager: PlayerManager,
    private val mcEntityRegistry: McEntityRegistry,
    private val physicsSpace: PhysicsSpace,
    private val playerPhysicsState: PlayerPhysicsState,
) : Tickable {

    private val platforms = HashMap<UUID, PlatformPair>()

    private data class PlatformPair(val vehicle: McEntity, val passenger: McEntity)

    companion object {
        private const val FLOOR_CAST_DISTANCE = 3.0
        private const val SNAP_STEP = 1.0 / 16.0
        private const val SHULKER_HEIGHT = 1.0
    }

    private fun snap(value: Double): Double = (value / SNAP_STEP).roundToInt() * SNAP_STEP

    override fun tick(delta: Duration) {
        val allPlayers = playerPhysicsState.allPlayers()

        for (playerUuid in allPlayers) {
            val mcPlayer = playerManager.getPlayerNullable(playerUuid) as? MinecraftPlayer ?: continue
            val physicsId = playerPhysicsState.getPhysicsId(playerUuid) ?: continue
            val hitboxBottom = physicsSpace.getBodyBottomPosition(physicsId) ?: continue
            val floorY = physicsSpace.castFloorBelow(physicsId, FLOOR_CAST_DISTANCE)

            if (floorY != null) {
                val platformTop = Vec3D(
                    snap(hitboxBottom.x),
                    snap(floorY),
                    snap(hitboxBottom.z)
                )
                val viewContextIDs = mcPlayer.viewContextIDs

                val pair = platforms.getOrPut(playerUuid) {
                    val vehicle = McEntity(
                        Random.nextInt(1_000_000, 9_000_000),
                        Registries.entityType[EntityTypes.ITEM_DISPLAY].value
                    ).apply {
                        metadata[EntityMeta.HAS_NO_GRAVITY] = true
                        viewerPredicate = { it.uuid == playerUuid }
                    }
                    val passenger = McEntity(
                        Random.nextInt(1_000_000, 9_000_000),
                        Registries.entityType[EntityTypes.SHULKER].value
                    ).apply {
                        metadata[EntityMeta.HAS_NO_GRAVITY] = true
                        metadata[ShulkerMeta.ATTACH_FACE] = Direction.DOWN
                        viewerPredicate = { it.uuid == playerUuid }
                    }
                    mcEntityRegistry.add(vehicle)
                    mcEntityRegistry.add(passenger)
                    PlatformPair(vehicle, passenger)
                }

                // Ставим vehicle так, чтобы top шалкера совпадал с полом.
                val anchorPos = platformTop - Vec3D(0.0, SHULKER_HEIGHT, 0.0)
                pair.vehicle.teleport(anchorPos, YawPitch.ZERO)
                pair.passenger.teleport(anchorPos, YawPitch.ZERO)
                pair.vehicle.viewContextIDs = viewContextIDs
                pair.passenger.viewContextIDs = viewContextIDs

                // Каждый тик связываем шалкера с item display — packet идемпотентен.
                mcPlayer.connection.sendPacket(
                    ClientboundSetPassengersPacket(
                        pair.vehicle.entityId,
                        listOf(pair.passenger.entityId)
                    )
                )
            } else {
                removePlatform(playerUuid)
            }
        }

        val toRemove = platforms.keys.filter { it !in allPlayers }
        toRemove.forEach { removePlatform(it) }
    }

    private fun removePlatform(playerUuid: UUID) {
        val pair = platforms.remove(playerUuid) ?: return
        mcEntityRegistry.remove(pair.vehicle)
        mcEntityRegistry.remove(pair.passenger)
    }
}
