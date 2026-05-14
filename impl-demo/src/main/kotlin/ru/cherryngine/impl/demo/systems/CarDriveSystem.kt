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
import kotlin.math.abs

private const val FORWARD_SPEED_THRESHOLD = 0.5

/** rad/tick добавка к yaw angular velocity при W+S+руль на стоящей машине.
 *  Нужен — без него car абсолютно static (front locked → slip angle = 0 →
 *  lateral friction force = 0 → нет yaw torque, вращение не запускается). */
private const val BURNOUT_STEER_YAW_PER_TICK = 0.2

/** m/s — выше этой скорости burnout-kick полностью отключается (hard-gate).
 *  Цель — не давать неестественной закрутки на полном ходу: W+S на скорости
 *  должен просто тормозить front-brake'ом, и только когда car замедлился ниже
 *  порога — kick включается и начинает раскручивать пончик. Hard-gate (вместо
 *  ramp) потому что при ramp car часто слегка катится 1-2 m/s пока physics
 *  балансирует engine push vs front-brake, и ослабленный kick не запускает
 *  вращение. */
private const val BURNOUT_KICK_MAX_SPEED = 5.0

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
        // W+S одновременно — burnout: forward=1 (газ на rear) + brake=1. Per-axle
        // brake-torque настроен так, что front locked (frontBrakeTorque=4000),
        // rear free (rearBrakeTorque=0), и engine крутит задние через свободный
        // foot-brake. Это даёт настоящий physics-эффект: стоим, задние буксуют;
        // с рулём — задняя ось скользит, машина крутится вокруг передней оси.
        val burnout = input.forward() && input.backward()
        var forward = 0f
        var brake = 0f
        when {
            burnout -> {
                forward = 1f
                brake = 1f
            }
            input.forward() && signedSpeed < -FORWARD_SPEED_THRESHOLD -> brake = 1f
            input.backward() && signedSpeed > FORWARD_SPEED_THRESHOLD -> brake = 1f
            input.forward() -> forward = 1f
            input.backward() -> forward = -1f
        }
        if (!burnout && forward == 0f && brake == 0f) brake = 0.25f

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

        // Burnout + руль — kick yaw. Без него car абсолютно статична (front locked
        // → slip angle = 0 → lateral friction = 0 → нет yaw force), вращение не
        // запускается. Accumulative add (не override) — friction physics дальше
        // сама решает pivot point: front locked = max longitudinal resistance →
        // pivot тяготеет к передней оси.
        if (burnout && right != 0f && abs(signedSpeed) < BURNOUT_KICK_MAX_SPEED) {
            val ang = vehicle.getAngularVelocity()
            vehicle.setAngularVelocity(Vec3D(ang.x, ang.y - right * BURNOUT_STEER_YAW_PER_TICK, ang.z))
        }
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
