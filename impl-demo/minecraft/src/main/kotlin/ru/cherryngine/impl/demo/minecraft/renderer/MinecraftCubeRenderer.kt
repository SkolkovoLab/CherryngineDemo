package ru.cherryngine.impl.demo.minecraft.renderer

import net.kyori.adventure.key.Key
import net.minestom.server.entity.EntityType
import net.minestom.server.entity.Metadata
import net.minestom.server.entity.MetadataDef
import net.minestom.server.item.ItemStack
import net.minestom.server.item.Material
import ru.cherryngine.engine.core.instance.InstanceSingleton
import ru.cherryngine.engine.core.player.Player
import ru.cherryngine.impl.demo.renderer.CubeRenderer
import ru.cherryngine.lib.math.Transform
import ru.cherryngine.lib.math.Vec3D
import ru.cherryngine.lib.math.YawPitch
import ru.cherryngine.platform.minecraft.java.entity.McEntity
import ru.cherryngine.platform.minecraft.java.entity.McEntityIds
import ru.cherryngine.platform.minecraft.java.entity.McEntityRegistry
import ru.cherryngine.platform.minecraft.java.player.MinecraftPlayer
import ru.cherryngine.platform.minecraft.java.utils.minestomQuaternion
import ru.cherryngine.platform.minecraft.java.utils.minestomVec
import java.util.*

@InstanceSingleton(platform = "minecraft")
class MinecraftCubeRenderer(
    private val mcEntityRegistry: McEntityRegistry,
) : CubeRenderer<MinecraftPlayer> {
    private val entities = HashMap<UUID, McEntity>()

    override fun canHandle(target: Player): Boolean = target is MinecraftPlayer

    override fun show(id: UUID, player: MinecraftPlayer) {
        entities[id]?.subscribers?.add(player)
    }

    override fun hide(id: UUID, player: MinecraftPlayer) {
        entities[id]?.subscribers?.remove(player)
    }

    override fun onAdd(id: UUID) {
        val mcEntity = McEntity(
            McEntityIds.next(),
            EntityType.ITEM_DISPLAY,
        )
        mcEntity.metadata[MetadataDef.HAS_NO_GRAVITY.index()] = Metadata.Boolean(true)
        mcEntity.metadata[MetadataDef.Display.TRANSFORMATION_INTERPOLATION_DURATION.index()] = Metadata.VarInt(1)
        mcEntity.metadata[MetadataDef.Display.POSITION_ROTATION_INTERPOLATION_DURATION.index()] = Metadata.VarInt(1)
        entities[id] = mcEntity
        mcEntityRegistry.add(mcEntity)
    }

    override fun onRemove(id: UUID) {
        val mcEntity = entities.remove(id) ?: return
        mcEntityRegistry.remove(mcEntity)
    }

    override fun update(
        id: UUID,
        position: Vec3D,
        yawPitch: YawPitch,
        material: Key,
        transform: Transform,
    ) {
        val mcEntity = entities[id] ?: return
        mcEntity.teleport(position, yawPitch)
        val mat = Material.fromKey(material) ?: Material.AIR
        val t = transform.translation
        val s = transform.scale
        val q = transform.rotation
        mcEntity.metadata[MetadataDef.ItemDisplay.DISPLAYED_ITEM.index()] = Metadata.ItemStack(ItemStack.of(mat))
        mcEntity.metadata[MetadataDef.Display.TRANSLATION.index()] = Metadata.Vector3(t.minestomVec())
        mcEntity.metadata[MetadataDef.Display.ROTATION_LEFT.index()] = Metadata.Quaternion(q.minestomQuaternion())
        mcEntity.metadata[MetadataDef.Display.SCALE.index()] = Metadata.Vector3(s.minestomVec())
        mcEntity.metadata[MetadataDef.Display.INTERPOLATION_DELAY.index()] = Metadata.VarInt(0)
        mcEntity.resendMeta()
    }
}
