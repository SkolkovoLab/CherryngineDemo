package ru.cherryngine.impl.demo.systems

import com.github.quillraven.fleks.IteratingSystem
import com.github.quillraven.fleks.World.Companion.family
import net.minestom.server.network.packet.client.play.ClientInputPacket
import ru.cherryngine.engine.core.instance.Instance
import ru.cherryngine.engine.core.player.PlayerManager
import ru.cherryngine.engine.ecs.EcsEntity
import ru.cherryngine.engine.ecs.components.PlayerComponent
import ru.cherryngine.engine.ecs.components.PositionComponent
import ru.cherryngine.engine.physics.PhysicsSpace
import ru.cherryngine.impl.demo.EcsSystemConfig
import ru.cherryngine.impl.demo.components.CarComponent
import ru.cherryngine.impl.demo.components.RidingCarComponent
import ru.cherryngine.impl.demo.output.PlayerMoverDispatcher
import ru.cherryngine.lib.math.Vec3D
import ru.cherryngine.platform.minecraft.java.player.MinecraftPlayer
import java.util.UUID

private const val PLAYER_SEAT_OFFSET = 0.5

/**
 * Каждый тик для каждого игрока с RidingCarComponent: читает последний
 * ClientInputPacket из снепшота (с fallback'ом на закешированный — клиент шлёт
 * только rising/falling edge, не каждый тик), переводит WASD в input для Jolt
 * WheeledVehicleController (forward/right/brake/handBrake), пушит controller.
 *
 * Идёт ПЕРЕД PhysicsSimulationSystem — input применяется на текущем тике через
 * VehicleStepListener. Игрок телепортируется на корпус (после симуляции через
 * CarPhysicsSyncSystem ECS-position уже актуален).
 *
 * MVP: только Java-клиент (читает ClientInputPacket).
 */
class CarDriveSystem(
    private val physicsSpace: PhysicsSpace,
    private val playerManager: PlayerManager,
    private val moverDispatcher: PlayerMoverDispatcher,
) : IteratingSystem(
    family { all(RidingCarComponent, PositionComponent, PlayerComponent) }
) {
    private val lastInput = HashMap<UUID, ClientInputPacket>()

    override fun onTickEntity(entity: EcsEntity) {
        val playerUuid = entity[PlayerComponent].uuid
        val riding = entity[RidingCarComponent]
        val playerPos = entity[PositionComponent]

        val mc = playerManager.getPlayerNullable(playerUuid) as? MinecraftPlayer ?: return

        val input = mc.packets<ClientInputPacket>().lastOrNull()
            ?.also { lastInput[playerUuid] = it }
            ?: lastInput[playerUuid]
            ?: ClientInputPacket(0.toByte())

        if (input.shift()) {
            lastInput.remove(playerUuid)
            entity.configure { it -= RidingCarComponent }
            return
        }

        // Найти машину; если её больше нет — выход.
        val carEntity = world.family { all(CarComponent) }
            .firstOrNull { it[CarComponent].carPhysicsId == riding.carPhysicsId }
        if (carEntity == null) {
            lastInput.remove(playerUuid)
            entity.configure { it -= RidingCarComponent }
            return
        }
        val carComp = carEntity[CarComponent]
        val vehicle = physicsSpace.getVehicleBody(carComp.carPhysicsId) ?: return

        // WheeledVehicleController.setDriverInput(forward, right, brake, handBrake), все в [-1..1].
        // forward — газ/задний; right — поворот руля; brake/handBrake — тормоза.
        val forward = when {
            input.forward() -> 1f
            input.backward() -> -1f
            else -> 0f
        }
        val right = when {
            input.right() -> 1f
            input.left() -> -1f
            else -> 0f
        }
        val brake = if (input.jump()) 1f else 0f  // jump = тормоз
        val handBrake = 0f
        vehicle.setDriverInput(forward, right, brake, handBrake)

        // Игрок сидит на корпусе — телепорт каждый тик.
        val carTransform = vehicle.getTransform()
        val seatPos = carTransform.translation + Vec3D(0.0, carComp.chassisSize.y * 0.5 + PLAYER_SEAT_OFFSET, 0.0)
        playerPos.position = seatPos
        moverDispatcher.correctClientPosition(mc, seatPos)
    }

    object Config : EcsSystemConfig {
        override fun create(instance: Instance) = CarDriveSystem(
            physicsSpace = instance.get(),
            playerManager = instance.get(),
            moverDispatcher = instance.get(),
        )
    }
}
