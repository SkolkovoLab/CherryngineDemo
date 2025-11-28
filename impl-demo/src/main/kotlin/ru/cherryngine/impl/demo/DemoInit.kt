package ru.cherryngine.impl.demo

import com.github.quillraven.fleks.configureWorld
import io.micronaut.runtime.event.annotation.EventListener
import jakarta.inject.Singleton
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import ru.cherryngine.engine.core.events.PlayerConfigurationAsyncEvent
import ru.cherryngine.engine.core.events.SetGameProfileEvent
import ru.cherryngine.engine.core.player.PlayerManager
import ru.cherryngine.engine.core.utils.StableTicker
import ru.cherryngine.engine.ecs.EcsWorld
import ru.cherryngine.engine.ecs.components.ViewableComponent
import ru.cherryngine.engine.ecs.systems.*
import ru.cherryngine.impl.demo.components.WorldComponent
import ru.cherryngine.impl.demo.systems.ApartSystem
import ru.cherryngine.impl.demo.systems.AxolotlModelSystem
import ru.cherryngine.impl.demo.systems.PlayerInitSystem
import ru.cherryngine.impl.demo.systems.WorldSystem
import ru.cherryngine.lib.minecraft.protocol.types.GameProfile
import java.util.*
import kotlin.time.Duration.Companion.milliseconds

@Singleton
class DemoInit(
    demoWorlds: DemoWorlds,
    playerManager: PlayerManager,
) {
    val ecsWorld: EcsWorld

    init {
        ecsWorld = configureWorld {
            systems {
                // чтение сосотяния клиента
                add(PlayerInitSystem("street", playerManager))
                add(ReadClientPositionSystem(playerManager))

                // всякие действия
                add(CommandActionsSystem())
                add(AxolotlModelSystem(playerManager))
                add(WorldSystem(demoWorlds))
                add(ApartSystem())

                // завершение
                add(ViewSystem(playerManager))
                add(WriteClientPositionSystem(playerManager))
                add(ClearEventsSystem())
            }
        }

        ecsWorld.entity {
            it += ViewableComponent(setOf("street"))
            it += WorldComponent("street")
        }

        ecsWorld.entity {
            it += ViewableComponent(setOf("apart1"))
            it += WorldComponent("apart1")
        }

        ecsWorld.entity {
            it += ViewableComponent(setOf("apart2"))
            it += WorldComponent("apart2")
        }

        val tickDuration = 50.milliseconds
        val ticker = StableTicker(tickDuration) { _, _ ->
            ecsWorld.update(tickDuration)
        }
        ticker.start()
    }

    @EventListener
    fun onPlayerConfiguration(event: PlayerConfigurationAsyncEvent) = runBlocking {
        // для теста подержим игрока в конфигурации 3 секунды
        delay(3000)
    }

    @EventListener
    fun onSetGameProfile(event: SetGameProfileEvent) {
        event.gameProfile = GameProfile(UUID.randomUUID(), "ebanatina")
    }
}