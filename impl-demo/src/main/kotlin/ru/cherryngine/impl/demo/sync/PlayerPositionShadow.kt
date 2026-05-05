package ru.cherryngine.impl.demo.sync

import ru.cherryngine.engine.core.instance.InstanceSingleton
import ru.cherryngine.lib.math.Vec3D
import ru.cherryngine.lib.math.YawPitch
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

/** Минимум для sync-логики: позиция + yawPitch, без onGround и прочих платформенных флагов. */
data class PositionSnapshot(val position: Vec3D, val yawPitch: YawPitch)

@InstanceSingleton
class PlayerPositionShadow {
    private val map = ConcurrentHashMap<UUID, PositionSnapshot>()

    operator fun get(uuid: UUID): PositionSnapshot? = map[uuid]
    operator fun set(uuid: UUID, snapshot: PositionSnapshot) {
        map[uuid] = snapshot
    }

    fun remove(uuid: UUID) {
        map.remove(uuid)
    }
}
