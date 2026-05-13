package ru.cherryngine.impl.demo.minecraft.renderer

import net.kyori.adventure.key.Key
import net.minestom.server.entity.EntityType
import net.minestom.server.entity.Metadata
import net.minestom.server.entity.MetadataDef
import net.minestom.server.item.ItemStack
import net.minestom.server.item.Material
import ru.cherryngine.engine.core.instance.InstanceSingleton
import ru.cherryngine.engine.core.player.Player
import ru.cherryngine.impl.demo.renderer.CarRenderer
import ru.cherryngine.lib.math.Transform
import ru.cherryngine.lib.math.Vec3D
import ru.cherryngine.lib.math.YawPitch
import ru.cherryngine.lib.math.rotation.QRot
import ru.cherryngine.platform.minecraft.java.entity.McEntity
import ru.cherryngine.platform.minecraft.java.entity.McEntityIds
import ru.cherryngine.platform.minecraft.java.entity.McEntityRegistry
import ru.cherryngine.platform.minecraft.java.player.MinecraftPlayer
import ru.cherryngine.platform.minecraft.java.utils.minestomQuaternion
import ru.cherryngine.platform.minecraft.java.utils.minestomVec
import java.util.UUID

@InstanceSingleton(platform = "minecraft")
class MinecraftCarRenderer(
    private val mcEntityRegistry: McEntityRegistry,
) : CarRenderer<MinecraftPlayer> {
    companion object {
        private val CHASSIS_MATERIAL: Key = Key.key("red_concrete")
        private val WHEEL_MATERIAL: Key = Key.key("snowball")
        private const val WHEEL_COUNT = 4
        private val WHEEL_FLIP_LEFT: QRot = QRot.fromAxisAngle(Vec3D.PLUS_X, Math.toRadians(90.0))
        private val WHEEL_FLIP_RIGHT: QRot = QRot.fromAxisAngle(Vec3D.PLUS_X, Math.toRadians(-90.0))
    }

    private data class CarEntities(val chassis: McEntity, val wheels: List<McEntity>) {
        val all: List<McEntity> get() = listOf(chassis) + wheels
    }

    private val entities = HashMap<UUID, CarEntities>()

    override fun canHandle(target: Player): Boolean = target is MinecraftPlayer

    override fun show(id: UUID, player: MinecraftPlayer) {
        entities[id]?.all?.forEach { it.subscribers.add(player) }
    }

    override fun hide(id: UUID, player: MinecraftPlayer) {
        entities[id]?.all?.forEach { it.subscribers.remove(player) }
    }

    override fun onAdd(id: UUID) {
        val chassis = createPart()
        val wheels = List(WHEEL_COUNT) { createPart() }
        val carEntities = CarEntities(chassis, wheels)
        entities[id] = carEntities
        carEntities.all.forEach { mcEntityRegistry.add(it) }
    }

    override fun onRemove(id: UUID) {
        val carEntities = entities.remove(id) ?: return
        carEntities.all.forEach { mcEntityRegistry.remove(it) }
    }

    override fun update(id: UUID, chassisTransform: Transform, wheelTransforms: List<Transform>) {
        val carEntities = entities[id] ?: return
        updatePart(carEntities.chassis, CHASSIS_MATERIAL, chassisTransform)
        wheelTransforms.forEachIndexed { i, transform ->
            val wheel = carEntities.wheels.getOrNull(i) ?: return@forEachIndexed
            // Чётные индексы (0=FL, 2=RL) — левые колёса, нечётные — правые.
            val flip = if (i % 2 == 0) WHEEL_FLIP_LEFT else WHEEL_FLIP_RIGHT
            val flipped = Transform(
                translation = transform.translation,
                rotation = transform.rotation * flip,
                scale = transform.scale,
            )
            updatePart(wheel, WHEEL_MATERIAL, flipped)
        }
    }

    private fun createPart(): McEntity {
        val mcEntity = McEntity(McEntityIds.next(), EntityType.ITEM_DISPLAY)
        mcEntity.metadata[MetadataDef.HAS_NO_GRAVITY.index()] = Metadata.Boolean(true)
        mcEntity.metadata[MetadataDef.Display.TRANSFORMATION_INTERPOLATION_DURATION.index()] = Metadata.VarInt(1)
        mcEntity.metadata[MetadataDef.Display.POSITION_ROTATION_INTERPOLATION_DURATION.index()] = Metadata.VarInt(1)
        return mcEntity
    }

    private fun updatePart(mcEntity: McEntity, material: Key, transform: Transform) {
        // Item-display'и сами не вращаются, они рендерят item с лок-трансформом
        // относительно своей позиции. Поэтому world-translation идёт в teleport(),
        // а rotation/scale — в Display.ROTATION_LEFT / Display.SCALE. teleport
        // также шлёт yaw=0 — поворот выражен через ROTATION_LEFT, без двойного
        // вращения от yaw entity.
        mcEntity.teleport(transform.translation, YawPitch.ZERO)
        mcEntity.metadata[MetadataDef.ItemDisplay.DISPLAYED_ITEM.index()] =
            Metadata.ItemStack(ItemStack.of(Material.fromKey(material) ?: Material.AIR))
        mcEntity.metadata[MetadataDef.Display.TRANSLATION.index()] = Metadata.Vector3(Vec3D.ZERO.minestomVec())
        mcEntity.metadata[MetadataDef.Display.ROTATION_LEFT.index()] = Metadata.Quaternion(transform.rotation.minestomQuaternion())
        mcEntity.metadata[MetadataDef.Display.SCALE.index()] = Metadata.Vector3(transform.scale.minestomVec())
        mcEntity.metadata[MetadataDef.Display.INTERPOLATION_DELAY.index()] = Metadata.VarInt(0)
        mcEntity.resendMeta()
    }
}
