package ru.cherryngine.impl.demo.minecraft

import io.micronaut.runtime.event.annotation.EventListener
import jakarta.inject.Singleton
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import ru.cherryngine.engine.minecraft.events.PlayerConfigurationAsyncEvent

@Singleton
class DemoConfigurationListener {
    @EventListener
    fun onPlayerConfiguration(event: PlayerConfigurationAsyncEvent) = runBlocking {
        delay(3000)
    }
}
