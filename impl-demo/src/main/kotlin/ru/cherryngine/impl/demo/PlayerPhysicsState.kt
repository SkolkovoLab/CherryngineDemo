package ru.cherryngine.impl.demo

import ru.cherryngine.engine.core.instance.InstanceSingleton
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

@InstanceSingleton
class PlayerPhysicsState {
    private val playerPhysicsIds = ConcurrentHashMap<UUID, UUID>()

    fun register(playerUuid: UUID, physicsId: UUID) {
        playerPhysicsIds[playerUuid] = physicsId
    }

    fun unregister(playerUuid: UUID) {
        playerPhysicsIds.remove(playerUuid)
    }

    fun getPhysicsId(playerUuid: UUID): UUID? = playerPhysicsIds[playerUuid]

    fun allPlayers(): Set<UUID> = playerPhysicsIds.keys
}
