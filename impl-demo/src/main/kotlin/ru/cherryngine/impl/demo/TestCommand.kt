package ru.cherryngine.impl.demo

import net.kyori.adventure.key.Key
import org.incendo.cloud.annotation.specifier.Greedy
import org.incendo.cloud.annotation.specifier.Range
import org.incendo.cloud.annotations.Command
import org.incendo.cloud.annotations.Permission
import ru.cherryngine.engine.core.commandmanager.CloudCommand
import ru.cherryngine.engine.core.commandmanager.CommandSender
import ru.cherryngine.engine.core.player.Player
import ru.cherryngine.engine.ecs.components.PlayerComponent
import ru.cherryngine.engine.ecs.components.PositionComponent
import ru.cherryngine.engine.ecs.components.ViewableComponent
import ru.cherryngine.engine.ecs.getPlayerEntity
import ru.cherryngine.engine.ecs.systems.CommandActionsSystem.Companion.commandAction
import ru.cherryngine.impl.demo.components.ApartComponent
import ru.cherryngine.impl.demo.components.CubeModelComponent
import ru.cherryngine.impl.demo.components.PhysicsComponent
import ru.cherryngine.lib.math.Transform
import java.util.*

@CloudCommand
class TestCommand(
    private val demoInit: DemoInit,
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
        demoInit.ecsWorld.commandAction {
            val entity = getPlayerEntity(sender.uuid)
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
        demoInit.ecsWorld.commandAction {
            val entity = getPlayerEntity(sender.uuid)
            val otherPlayer = getPlayerEntity(other.uuid)
            val tmp = entity[PlayerComponent].uuid
            entity[PlayerComponent].uuid = otherPlayer[PlayerComponent].uuid
            otherPlayer[PlayerComponent].uuid = tmp
        }
    }

    @Command("phys")
    fun physCommand(
        sender: Player,
    ) {
        demoInit.ecsWorld.commandAction {
            val playerEntity = getPlayerEntity(sender.uuid)
            val spawnPosition = playerEntity[PositionComponent].position

            entity {
                it += PhysicsComponent("test", PhysicsComponent.BodyInfo.Cube)
                it += PositionComponent(spawnPosition)
                it += CubeModelComponent(Key.key("tnt"), Transform())
                it += ViewableComponent(setOf("street"))
            }
        }
    }
}