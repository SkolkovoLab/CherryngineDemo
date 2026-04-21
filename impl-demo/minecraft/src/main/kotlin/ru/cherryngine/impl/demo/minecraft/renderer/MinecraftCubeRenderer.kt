package ru.cherryngine.impl.demo.minecraft.renderer

import net.kyori.adventure.key.Key
import net.minestom.server.coordinate.Vec
import net.minestom.server.entity.EntityType
import net.minestom.server.entity.Metadata
import net.minestom.server.entity.MetadataDef
import net.minestom.server.item.ItemStack
import net.minestom.server.item.Material
import ru.cherryngine.engine.core.instance.InstanceSingleton
import ru.cherryngine.impl.demo.renderer.CubeRenderer
import ru.cherryngine.lib.math.Transform
import ru.cherryngine.lib.math.Vec3D
import ru.cherryngine.lib.math.YawPitch
import ru.cherryngine.platform.minecraft.java.entity.McEntity
import ru.cherryngine.platform.minecraft.java.entity.McEntityRegistry
import java.util.*
import kotlin.random.Random

@InstanceSingleton(platform = "minecraft")
class MinecraftCubeRenderer(
    private val mcEntityRegistry: McEntityRegistry,
) : CubeRenderer {
    private val entities = HashMap<UUID, McEntity>()

    override fun onAdd(id: UUID) {
        val mcEntity = McEntity(
            Random.nextInt(1000, 1_000_000),
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
        viewContextIDs: Set<String>,
    ) {
        val mcEntity = entities[id] ?: return
        mcEntity.teleport(position, yawPitch)
        mcEntity.viewContextIDs = viewContextIDs
        val mat = Material.fromKey(material) ?: Material.AIR
        val t = transform.translation
        val s = transform.scale
        val q = transform.rotation
        mcEntity.metadata[MetadataDef.ItemDisplay.DISPLAYED_ITEM.index()] = Metadata.ItemStack(ItemStack.of(mat))
        mcEntity.metadata[MetadataDef.Display.TRANSLATION.index()] = Metadata.Vector3(Vec(t.x, t.y, t.z))
        mcEntity.metadata[MetadataDef.Display.ROTATION_LEFT.index()] = Metadata.Quaternion(floatArrayOf(q.x.toFloat(), q.y.toFloat(), q.z.toFloat(), q.w.toFloat()))
        mcEntity.metadata[MetadataDef.Display.SCALE.index()] = Metadata.Vector3(Vec(s.x, s.y, s.z))
        mcEntity.metadata[MetadataDef.Display.INTERPOLATION_DELAY.index()] = Metadata.VarInt(0)
        mcEntity.resendMeta()
    }
}
