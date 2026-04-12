package ru.cherryngine.impl.demo

import net.kyori.adventure.key.Key
import org.incendo.cloud.annotation.specifier.Greedy
import org.incendo.cloud.annotation.specifier.Range
import org.incendo.cloud.annotations.Command
import org.incendo.cloud.annotations.Permission
import ru.cherryngine.engine.core.commandmanager.CommandSender
import ru.cherryngine.engine.core.player.Player
import ru.cherryngine.engine.ecs.EcsEntity
import ru.cherryngine.engine.ecs.EcsWorld
import ru.cherryngine.engine.ecs.PlayerIndex
import ru.cherryngine.engine.ecs.components.PlayerComponent
import ru.cherryngine.engine.ecs.components.PositionComponent
import ru.cherryngine.engine.ecs.components.ViewableComponent
import ru.cherryngine.engine.ecs.systems.CommandActionsSystem.Companion.commandAction
import ru.cherryngine.impl.demo.components.ApartComponent
import ru.cherryngine.impl.demo.components.CubeModelComponent
import ru.cherryngine.impl.demo.components.PhysicsComponent
import ru.cherryngine.lib.math.Transform
import java.util.*

class TestCommand(
    private val ecsWorld: EcsWorld,
    private val playerIndex: PlayerIndex,
) {
    @Command("testcommand <string> <int> <key> <uuid> <greedy>")
    @Permission("command.test")
    fun testCommand(
        sender: CommandSender,
        string: String,
        @Range(min = "5", max = "20") int: Int,
        key: Key,
        uuid: UUID,
        @Greedy greedy: String,
    ) {
        sender.sendMessage("$string + $int + $key + $uuid + $greedy")
    }

    @Command("apart <apartId>")
    fun apartCommand(
        sender: Player,
        apartId: String,
    ) {
        ecsWorld.commandAction {
            val entity = playerIndex.getOrThrow(sender.uuid)
            if (apartId == "null") {
                entity.configure {
                    it -= ApartComponent
                }
                sender.sendMessage("Apartment removed for ${sender.username}")
            } else {
                entity.configure {
                    it.getOrAdd(ApartComponent) { ApartComponent("") }.apartName = apartId
                }
                sender.sendMessage("Apartment set to $apartId for ${sender.username}")
            }
        }
    }

    @Command("swap <other>")
    fun swapCommand(
        sender: Player,
        other: Player,
    ) {
        ecsWorld.commandAction {
            val entity = playerIndex.getOrThrow(sender.uuid)
            val otherPlayer = playerIndex.getOrThrow(other.uuid)
            val tmp = entity[PlayerComponent].uuid
            entity[PlayerComponent].uuid = otherPlayer[PlayerComponent].uuid
            otherPlayer[PlayerComponent].uuid = tmp
        }
    }

    @Command("viewcontext <contexts>")
    fun viewContextCommand(
        sender: Player,
        contexts: String,
    ) {
        ecsWorld.commandAction {
            val entity = playerIndex.getOrThrow(sender.uuid)
            entity[PlayerComponent].viewContextIDs = contexts.split(",").toSet()
        }
    }

    @Command("phys cube")
    fun physCubeCommand(
        sender: Player,
    ) {
        ecsWorld.commandAction {
            val playerEntity = playerIndex.getOrThrow(sender.uuid)
            val spawnPosition = playerEntity[PositionComponent].position

            entity {
                it += PhysicsComponent(bodyInfo = PhysicsComponent.BodyInfo.Cube, physContextIDs = setOf("street"))
                it += PositionComponent(spawnPosition)
                it += CubeModelComponent(material = Key.key("tnt"), transform = Transform())
                it += ViewableComponent(setOf("street"))
            }
        }
    }

    @Command("phys clear")
    fun physClearCommand(
        sender: Player,
    ) {
        ecsWorld.commandAction {
            val toRemove = mutableListOf<EcsEntity>()
            family { all(PhysicsComponent) }.forEach { toRemove.add(it) }
            toRemove.forEach { it.remove() }
            sender.sendMessage("Removed ${toRemove.size} physics entities")
        }
    }

    @Command("phys remove")
    fun physRemoveCommand(
        sender: Player,
    ) {
        ecsWorld.commandAction {
            val playerPos = playerIndex.getOrThrow(sender.uuid)[PositionComponent].position
            var closest: EcsEntity? = null
            var closestDistSq = Double.MAX_VALUE
            family { all(PhysicsComponent, PositionComponent) }.forEach { entity ->
                if (PlayerComponent in entity) return@forEach
                val distSq = (entity[PositionComponent].position - playerPos).lengthSquared()
                if (distSq < closestDistSq) {
                    closestDistSq = distSq
                    closest = entity
                }
            }
            if (closest != null) {
                closest.remove()
                sender.sendMessage("Removed nearest physics entity")
            } else {
                sender.sendMessage("No physics entities found")
            }
        }
    }
}
