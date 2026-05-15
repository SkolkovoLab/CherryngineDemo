package ru.cherryngine.impl.demo.systems

import com.github.quillraven.fleks.IntervalSystem
import kotlinx.coroutines.channels.Channel
import net.kyori.adventure.key.Key
import org.slf4j.LoggerFactory
import ru.cherryngine.engine.core.instance.Instance
import ru.cherryngine.engine.core.player.Player
import ru.cherryngine.engine.core.player.PlayerManager
import ru.cherryngine.engine.core.shape.ShapeGeometry
import ru.cherryngine.engine.core.shape.ShapeWorld
import ru.cherryngine.engine.ecs.components.InputTargetComponent
import ru.cherryngine.engine.ecs.components.PlayerComponent
import ru.cherryngine.engine.ecs.components.PositionComponent
import ru.cherryngine.engine.ecs.components.ViewableComponent
import ru.cherryngine.impl.demo.EcsSystemConfig
import ru.cherryngine.impl.demo.InstanceJoinChannel
import ru.cherryngine.impl.demo.InstanceLeaveChannel
import ru.cherryngine.impl.demo.PlayerPhysicsState
import ru.cherryngine.impl.demo.components.AxolotlModelComponent
import ru.cherryngine.impl.demo.components.CreateCubeToolComponent
import ru.cherryngine.impl.demo.components.CreateSlabToolComponent
import ru.cherryngine.impl.demo.components.CubeModelComponent
import ru.cherryngine.impl.demo.components.DisplayNameComponent
import ru.cherryngine.impl.demo.components.GrabToolComponent
import ru.cherryngine.impl.demo.components.HitboxVisualizationComponent
import ru.cherryngine.impl.demo.components.InInventoryComponent
import ru.cherryngine.impl.demo.components.InteractToolComponent
import ru.cherryngine.impl.demo.components.InventoryComponent
import ru.cherryngine.impl.demo.components.ItemComponent
import ru.cherryngine.impl.demo.components.RemoveToolComponent
import ru.cherryngine.impl.demo.components.ShapeRegistrationComponent
import ru.cherryngine.impl.demo.components.SpawnCarToolComponent
import ru.cherryngine.impl.demo.renderer.PlayerRendererDispatcher
import ru.cherryngine.impl.demo.shape.PlayerShape
import ru.cherryngine.lib.math.Transform
import ru.cherryngine.lib.math.Vec3D
import java.util.*

class PlayerInitSystem(
    private val joinChannel: Channel<UUID>,
    private val leaveChannel: Channel<Player>,
    private val playerRendererDispatcher: PlayerRendererDispatcher,
    private val playerManager: PlayerManager,
    private val playerPhysicsState: PlayerPhysicsState,
    private val shapeWorld: ShapeWorld,
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
                playerRendererDispatcher.onLeave(player)
            }
            toRemoveUUIDs.forEach { uuid -> playerPhysicsState.unregister(uuid) }
            world.family { all(PlayerComponent) }.forEach {
                if (it[PlayerComponent].uuid in toRemoveUUIDs) {
                    // Уносим item-entity из инвентаря — иначе они осиротеют (item без location-компонента).
                    it.getOrNull(InventoryComponent)?.slots?.forEach { item -> item?.remove() }
                    it.remove()
                }
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
            playerRendererDispatcher.onJoin(player)

            val playerEntityRef = world.entity {
                it += PlayerComponent(playerUuid, setOf(defaultViewContextID))
                it += InputTargetComponent(playerUuid)
                it += ViewableComponent(setOf(defaultViewContextID))
                it += PositionComponent(spawnPosition)
                it += AxolotlModelComponent()
            }

            // Стартовый инвентарь: по одному предмету на каждый инструмент в слотах 0–5.
            // Каждая item-entity несёт ItemComponent + DisplayName + InInventory (location-инвариант)
            // + маркер конкретного инструмента, который читает useTool/ToolUseAction.
            val inventoryCmp = InventoryComponent()
            inventoryCmp.slots[0] = world.entity {
                it += ItemComponent()
                it += DisplayNameComponent("Create Cube")
                it += InInventoryComponent(playerEntityRef, 0)
                it += CreateCubeToolComponent()
            }
            inventoryCmp.slots[1] = world.entity {
                it += ItemComponent()
                it += DisplayNameComponent("Create Slab")
                it += InInventoryComponent(playerEntityRef, 1)
                it += CreateSlabToolComponent()
            }
            inventoryCmp.slots[2] = world.entity {
                it += ItemComponent()
                it += DisplayNameComponent("Remove")
                it += InInventoryComponent(playerEntityRef, 2)
                it += RemoveToolComponent()
            }
            inventoryCmp.slots[3] = world.entity {
                it += ItemComponent()
                it += DisplayNameComponent("Grab")
                it += InInventoryComponent(playerEntityRef, 3)
                it += GrabToolComponent()
            }
            inventoryCmp.slots[4] = world.entity {
                it += ItemComponent()
                it += DisplayNameComponent("Spawn Car")
                it += InInventoryComponent(playerEntityRef, 4)
                it += SpawnCarToolComponent()
            }
            inventoryCmp.slots[5] = world.entity {
                it += ItemComponent()
                it += DisplayNameComponent("Interact")
                it += InInventoryComponent(playerEntityRef, 5)
                it += InteractToolComponent()
            }
            playerEntityRef.configure { it += inventoryCmp }

            // Шейп игрока — капсула, читающая текущую позицию из PositionComponent.
            // Регистрация уйдёт через ShapeRegistrationComponent.onRemove когда entity удалится.
            val ecsWorld = world
            val playerShape = PlayerShape(
                geometry = ShapeGeometry.Capsule(radius = 0.3f, halfHeight = 0.9f),
                getTransform = {
                    val pos = with(ecsWorld) { playerEntityRef.getOrNull(PositionComponent) }
                    Transform(translation = pos?.position ?: Vec3D.ZERO)
                },
                playerUuid = playerUuid,
            )
            val registration = shapeWorld.registerGroup(listOf(playerShape))
            playerEntityRef.configure { it += ShapeRegistrationComponent(registration) }

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
            playerRendererDispatcher = instance.get(),
            playerManager = instance.get(),
            playerPhysicsState = instance.get(),
            shapeWorld = instance.get(),
            defaultViewContextID = spawnViewContext,
            spawnPosition = spawnPosition,
        )
    }
}
