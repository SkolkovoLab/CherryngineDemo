package ru.cherryngine.impl.demo.systems

import com.github.quillraven.fleks.IteratingSystem
import com.github.quillraven.fleks.World.Companion.family
import net.minestom.server.network.packet.client.play.ClientInputPacket
import ru.cherryngine.engine.core.instance.Instance
import ru.cherryngine.engine.core.player.PlayerManager
import ru.cherryngine.engine.ecs.EcsEntity
import ru.cherryngine.engine.ecs.components.CameraTargetComponent
import ru.cherryngine.engine.ecs.components.InputTargetComponent
import ru.cherryngine.engine.ecs.getPlayerEntityOrNull
import ru.cherryngine.engine.physics.PhysicsSpace
import ru.cherryngine.impl.demo.EcsSystemConfig
import ru.cherryngine.impl.demo.components.CarComponent
import ru.cherryngine.impl.demo.components.RidingCarComponent
import ru.cherryngine.lib.math.Vec3D
import ru.cherryngine.platform.minecraft.java.player.MinecraftPlayer
import java.util.UUID

private const val FORWARD_SPEED_THRESHOLD = 0.5

/**
 * Каждый тик для каждой машины с InputTargetComponent: читает последний
 * ClientInputPacket из снепшота водителя (с fallback'ом на закешированный —
 * клиент шлёт только rising/falling edge, не каждый тик), переводит WASD в
 * input для Jolt WheeledVehicleController (forward/right/brake/handBrake),
 * пушит controller. При shift — водитель спешивается.
 *
 * **Игрока не трогаем вообще**. Третье лицо реализуется на стороне клиента
 * через horse-anchor (см. MinecraftThirdPersonCameraTickable): сервер лишь
 * сажает игрока пассажиром на лошадь, всё остальное (вращение мышью, render
 * камеры) — клиентское. Любые серверные teleport/correctClientPosition в
 * этом режиме сбивают камеру обратно на устаревший yaw/pitch.
 *
 * MVP: только Java-клиент (читает ClientInputPacket).
 */
class CarDriveSystem(
    private val physicsSpace: PhysicsSpace,
    private val playerManager: PlayerManager,
) : IteratingSystem(
    family { all(CarComponent, InputTargetComponent) }
) {
    private val lastInput = HashMap<UUID, ClientInputPacket>()

    override fun onTickEntity(entity: EcsEntity) {
        val playerUuid = entity[InputTargetComponent].playerUuid
        val carComp = entity[CarComponent]

        val mc = playerManager.getPlayerNullable(playerUuid) as? MinecraftPlayer ?: return

        val input = mc.packets<ClientInputPacket>().lastOrNull()
            ?.also { lastInput[playerUuid] = it }
            ?: lastInput[playerUuid]
            ?: ClientInputPacket(0.toByte())

        if (input.shift()) {
            exit(entity, playerUuid)
            return
        }

        val vehicle = physicsSpace.getVehicleBody(carComp.carPhysicsId) ?: return

        // Signed-скорость машины вдоль её forward-оси: > 0 — едет вперёд,
        // < 0 — назад, около 0 — стоит. Нужно чтобы W при движении назад (и S при
        // движении вперёд) работали как тормоз, а не как «газ в противоположную»:
        // иначе игрок при попытке остановиться сразу включает реверс.
        val chassisRot = vehicle.getTransform().rotation
        val forwardDir = chassisRot.apply(Vec3D.PLUS_Z)
        val signedSpeed = vehicle.getLinearVelocity().dot(forwardDir)

        // WheeledVehicleController.setDriverInput(forward, right, brake, handBrake), все в [-1..1].
        var forward = 0f
        var brake = 0f
        when {
            input.forward() && signedSpeed < -FORWARD_SPEED_THRESHOLD -> brake = 1f
            input.backward() && signedSpeed > FORWARD_SPEED_THRESHOLD -> brake = 1f
            input.forward() -> forward = 1f
            input.backward() -> forward = -1f
        }
        if (forward == 0f && brake == 0f) brake = 0.25f

        val right = when {
            input.right() -> 1f
            input.left() -> -1f
            else -> 0f
        }
        val handBrake = if (input.jump()) 1f else 0f

        // Jolt усыпляет неактивные body после простоя — driver-input при этом
        // молча игнорится. Активируем chassis перед input'ом если игрок что-то жмёт.
        if (forward != 0f) {
            vehicle.activate()
        }
        vehicle.setDriverInput(forward, right, brake, handBrake)
    }

    private fun exit(carEntity: EcsEntity, playerUuid: UUID) {
        lastInput.remove(playerUuid)
        carEntity.configure {
            it -= InputTargetComponent
            it -= CameraTargetComponent
        }
        val playerEntity = world.getPlayerEntityOrNull(playerUuid) ?: return
        playerEntity.configure {
            it -= RidingCarComponent
            it += InputTargetComponent(playerUuid)
        }
    }

    object Config : EcsSystemConfig {
        override fun create(instance: Instance) = CarDriveSystem(
            physicsSpace = instance.get(),
            playerManager = instance.get(),
        )
    }
}
