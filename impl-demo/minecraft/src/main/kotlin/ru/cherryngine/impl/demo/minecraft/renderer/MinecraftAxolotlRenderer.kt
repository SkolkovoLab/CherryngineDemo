package ru.cherryngine.impl.demo.minecraft.renderer

import net.kyori.adventure.text.Component
import net.minestom.server.entity.EntityType
import net.minestom.server.entity.Metadata
import net.minestom.server.entity.MetadataDef
import net.minestom.server.entity.metadata.water.AxolotlMeta
import ru.cherryngine.engine.core.instance.InstanceSingleton
import ru.cherryngine.engine.core.player.Player
import ru.cherryngine.impl.demo.renderer.AxolotlRenderer
import ru.cherryngine.lib.math.Vec3D
import ru.cherryngine.lib.math.YawPitch
import ru.cherryngine.platform.minecraft.java.entity.McEntity
import ru.cherryngine.platform.minecraft.java.entity.McEntityIds
import ru.cherryngine.platform.minecraft.java.entity.McEntityRegistry
import ru.cherryngine.platform.minecraft.java.player.MinecraftPlayer
import java.util.*

@InstanceSingleton(platform = "minecraft")
class MinecraftAxolotlRenderer(
    private val mcEntityRegistry: McEntityRegistry,
) : AxolotlRenderer<MinecraftPlayer> {
    private val entities = HashMap<UUID, McEntity>()

    override fun canHandle(target: Player): Boolean = target is MinecraftPlayer

    override fun onAdd(id: UUID) {
        val mcEntity = McEntity(
            McEntityIds.next(),
            EntityType.AXOLOTL,
        ).apply {
            metadata[MetadataDef.HAS_NO_GRAVITY.index()] = Metadata.Boolean(true)
            metadata[MetadataDef.Axolotl.VARIANT.index()] = Metadata.VarInt(AxolotlMeta.Variant.values().random().ordinal)
            metadata[MetadataDef.CUSTOM_NAME_VISIBLE.index()] = Metadata.Boolean(true)
        }
        entities[id] = mcEntity
        mcEntityRegistry.add(mcEntity)
    }

    override fun onRemove(id: UUID) {
        val mcEntity = entities.remove(id) ?: return
        mcEntityRegistry.remove(mcEntity)
    }

    override fun show(id: UUID, player: MinecraftPlayer) {
        entities[id]?.subscribers?.add(player)
    }

    override fun hide(id: UUID, player: MinecraftPlayer) {
        entities[id]?.subscribers?.remove(player)
    }

    override fun update(
        id: UUID,
        position: Vec3D,
        yawPitch: YawPitch,
        name: String?,
        hiddenFromPlayer: UUID?,
    ) {
        val mcEntity = entities[id] ?: return
        mcEntity.teleport(position, yawPitch)
        mcEntity.viewerPredicate = if (hiddenFromPlayer != null) {
            { it.uuid != hiddenFromPlayer }
        } else {
            { true }
        }
        if (name != null) {
            mcEntity.metadata[MetadataDef.CUSTOM_NAME.index()] = Metadata.OptComponent(Component.text(name))
        }
        mcEntity.resendMeta()
    }
}
