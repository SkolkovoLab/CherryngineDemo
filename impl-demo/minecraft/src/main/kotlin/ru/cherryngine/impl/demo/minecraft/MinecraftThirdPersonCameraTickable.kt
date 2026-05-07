package ru.cherryngine.impl.demo.minecraft

import net.minestom.server.entity.EntityType
import net.minestom.server.entity.Metadata
import net.minestom.server.entity.MetadataDef
import net.minestom.server.network.packet.server.play.SetPassengersPacket
import ru.cherryngine.engine.core.instance.InstanceSingleton
import ru.cherryngine.engine.core.instance.TickStage
import ru.cherryngine.engine.core.instance.Tickable
import ru.cherryngine.engine.core.player.PlayerManager
import ru.cherryngine.engine.core.world.WorldRaycasterDispatcher
import ru.cherryngine.engine.ecs.EcsWorld
import ru.cherryngine.engine.ecs.components.CameraMode
import ru.cherryngine.engine.ecs.components.CameraTargetComponent
import ru.cherryngine.engine.ecs.components.PositionComponent
import ru.cherryngine.lib.math.Vec3D
import ru.cherryngine.lib.math.YawPitch
import ru.cherryngine.platform.minecraft.java.entity.McEntity
import ru.cherryngine.platform.minecraft.java.entity.McEntityRegistry
import ru.cherryngine.platform.minecraft.java.player.MinecraftClientState
import ru.cherryngine.platform.minecraft.java.player.MinecraftPlayer
import java.util.UUID
import kotlin.random.Random
import kotlin.time.Duration

/**
 * Камера третьего лица для игроков с [CameraTargetComponent] (mode = ThirdPerson).
 *
 * Реализация — «horse trick»: невидимый baby-horse каждый тик телепортируется
 * за focus-entity на расстояние [CameraMode.ThirdPerson.radius] вдоль
 * НАПРАВЛЕНИЯ ВЗГЛЯДА игрока, и игрок сажается на коня через SetPassengersPacket.
 * Клиент трактует это как «я passenger horse'а» и рендерит камеру с пассажирской
 * привязки.
 *
 * **Почему именно лошадь, а не ARMOR_STAND/INTERACTION/etc.** — потому что
 * лошадь это рейдабельный mount, и её водитель шлёт `ClientInputPacket`,
 * через который мы получаем нажатие E (interact). Будущая логика
 * «E = взаимодействовать сидя в машине» завязана на этом. На пассивных
 * сущностях ClientInputPacket не приходит.
 *
 * Yaw/pitch читаем напрямую из [MinecraftClientState] — там самый свежий снимок
 * (network thread обновляет его на каждый rotation-пакет, без тиковой задержки).
 *
 * Yaw лошади всегда 0: если слать `yaw=playerYaw`, клиент трактует игрока
 * как driver mount'а и тянет yaw игрока к yaw лошади каждый тик. У baby-horse
 * passenger-anchor зависит от yaw слабо — горизонтальное смещение в пределах
 * нескольких см, незаметно.
 *
 * SetPassengers шлётся ровно один раз — при создании лошади. Повторная отправка
 * клиентом интерпретируется как re-mount и сбрасывает текущий yaw игрока.
 *
 * `clientState.position` обновляется server-authoritative (мы знаем, куда
 * поставили лошадь). Клиент в третьем лице на лошади никаких пакетов о позиции
 * не шлёт, а render чанков и видимость entity завязаны на `clientState.position`.
 *
 * Workaround на проникновение камеры в стены: дополнительный raycast по блокам
 * и ограничение radius'а до hit-distance.
 */
@InstanceSingleton(platform = "minecraft", stage = TickStage.POST)
class MinecraftThirdPersonCameraTickable(
    private val playerManager: PlayerManager,
    private val mcEntityRegistry: McEntityRegistry,
    private val ecsWorld: EcsWorld,
    private val worldRaycaster: WorldRaycasterDispatcher,
    private val clientState: MinecraftClientState,
) : Tickable {

    private val cameraHorses = HashMap<UUID, McEntity>()

    override fun tick(delta: Duration) {
        val activePlayers = mutableSetOf<UUID>()

        ecsWorld.family { all(CameraTargetComponent, PositionComponent) }.forEach { entity ->
            val camTarget = entity[CameraTargetComponent]
            val mode = camTarget.mode as? CameraMode.ThirdPerson ?: return@forEach
            val playerUuid = camTarget.playerUuid
            val focusPos = entity[PositionComponent].position

            val mcPlayer = playerManager.getPlayerNullable(playerUuid) as? MinecraftPlayer ?: return@forEach
            activePlayers.add(playerUuid)

            // Yaw/pitch из MinecraftClientState — обновляется на сетевом потоке немедленно
            // на каждый прилетающий ClientPlayer*RotationPacket / ClientVehicleMovePacket,
            // т.е. это самые свежие данные про взгляд игрока.
            val yawPitch = clientState.yawPitch(playerUuid) ?: YawPitch.ZERO

            // freshlyCreated — флаг для одноразовой отправки SetPassengersPacket. Каждый
            // повторный SetPassengers клиент трактует как новое посаживание и снимает
            // у игрока текущий yaw, выравнивая по yaw entity'и.
            var freshlyCreated = false
            val horse = cameraHorses.getOrPut(playerUuid) {
                freshlyCreated = true
                McEntity(
                    Random.nextInt(1_000_000, 9_000_000),
                    EntityType.HORSE,
                ).apply {
                    metadata[MetadataDef.HAS_NO_GRAVITY.index()] = Metadata.Boolean(true)
                    // IS_INVISIBLE — бит 0x20 в byte-поле ENTITY_FLAGS (index 0).
                    // Записывать туда Metadata.Boolean нельзя: клиент завалит десериализацию
                    // ("old=Byte, new=Boolean").
                    metadata[MetadataDef.ENTITY_FLAGS.index()] = Metadata.Byte(0x20.toByte())
                    // Baby horse — passenger-anchor ниже, игрок ближе к focus'у.
                    metadata[MetadataDef.AgeableMob.IS_BABY.index()] = Metadata.Boolean(true)
                    viewerPredicate = { it.uuid == playerUuid }
                    subscribers.add(mcPlayer)
                }.also { mcEntityRegistry.add(it) }
            }

            val cameraPos = computeCameraPos(focusPos, yawPitch, mode.radius, mcPlayer.viewContextIDs)
            // ВАЖНО: yaw лошади должен оставаться нулевым. Если слать yaw=playerYaw,
            // клиент трактует игрока как driver рейдабельного mount'а и тянет yaw игрока
            // к yaw лошади каждый тик, отбрасывая мышь. Yaw=0 → клиент не пушит свою
            // ориентацию назад. Passenger-anchor у baby horse зависит от yaw слабо.
            horse.teleport(cameraPos, YawPitch.ZERO)

            // Server-authoritative обновление clientState.position. Клиент в третьем лице
            // сидит на лошади, своих ClientPlayerPositionPacket не шлёт, а ClientVehicleMove —
            // не всегда (зависит от того, считает ли клиент себя driver'ом). Но СЕРВЕР знает,
            // где он поставил лошадь, и куда смотрит игрок — этого достаточно. Без этого
            // апдейта зона прорисовки чанков (MinecraftViewTickable читает clientState.position)
            // застревает там, где игрок был до посадки. Y используем ≈eye-level: для chunkPos
            // важен только XZ, а eye-level точнее всего соответствует камере.
            clientState.setPosition(playerUuid, cameraPos + Vec3D(0.0, 1.87, 0.0))

            // На первом тике руками шлём Spawn (через show) + SetPassengers одним пакетом-залпом.
            // Иначе MinecraftViewTickable пришлёт Spawn потом, а наш SetPassengers улетит раньше
            // и клиент его проигнорирует (entity ещё не существует у клиента).
            // ViewTickable не продублирует Spawn: он проверяет `mcPlayer in horse.viewers`,
            // а horse.show() именно туда и пишет.
            if (freshlyCreated) {
                horse.show(mcPlayer)
                mcPlayer.connection.sendPacket(
                    SetPassengersPacket(horse.entityId, listOf(mcPlayer.entityId))
                )
            }
        }

        val toRemove = cameraHorses.keys.filter { it !in activePlayers }
        toRemove.forEach { removeCamera(it) }
    }

    private fun computeCameraPos(
        focus: Vec3D,
        yawPitch: YawPitch,
        maxRadius: Double,
        contextIDs: Set<String>,
    ): Vec3D {
        // Назад от направления взгляда — это позиция «сзади» entity.
        val direction = -yawPitch.direction()
        var radius = maxRadius

        worldRaycaster.raycast(focus, direction, maxRadius + 1.0, contextIDs)?.let { hit ->
            radius = (hit.hitPos - focus).length() - 0.5
            if (radius < 0.5) radius = 0.5
        }

        // Passenger-anchor у baby-horse находится выше bounds. Чтобы клиентская
        // камера оказалась на focus + direction*radius, ставим коня ниже на
        // суммарный offset: horseHipsHeight (~0.6) - sittingY-offset (~0.35) +
        // playerEyeHeight (1.62) ≈ 1.87.
        val eyeOffset = Vec3D(0.0, 1.87, 0.0)
        return focus + direction * radius - eyeOffset
    }

    private fun removeCamera(playerUuid: UUID) {
        val horse = cameraHorses.remove(playerUuid) ?: return
        mcEntityRegistry.remove(horse)
    }
}
