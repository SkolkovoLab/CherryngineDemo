package ru.cherryngine.impl.demo.systems

import com.github.quillraven.fleks.IteratingSystem
import com.github.quillraven.fleks.World.Companion.family
import ru.cherryngine.impl.demo.EcsSystemConfig
import ru.cherryngine.impl.demo.InstanceScope
import ru.cherryngine.engine.core.instance.ServerWorld
import ru.cherryngine.engine.core.player.PlayerOutputProvider
import ru.cherryngine.engine.ecs.EcsEntity
import ru.cherryngine.engine.ecs.components.PlayerComponent
import ru.cherryngine.engine.ecs.components.PositionComponent
import ru.cherryngine.engine.physics.PhysicsSpace
import ru.cherryngine.engine.physics.terrain.ActiveBodyInfo
import ru.cherryngine.engine.physics.terrain.LayerWithContext
import ru.cherryngine.engine.physics.terrain.TerrainGenerator
import ru.cherryngine.engine.ecs.components.ViewableComponent
import ru.cherryngine.impl.demo.components.CubeModelComponent
import ru.cherryngine.impl.demo.components.HitboxVisualizationComponent
import ru.cherryngine.impl.demo.components.PhysicsComponent
import ru.cherryngine.lib.math.Vec3D

class PhysicsSystem(
    private val physicsSpace: PhysicsSpace,
    private val terrainGenerator: TerrainGenerator,
    private val serverWorld: ServerWorld,
    private val outputProvider: PlayerOutputProvider,
) : IteratingSystem(
    family { all(PhysicsComponent) }
) {
    companion object {
        // Смещение центра капсулы относительно ног игрока (половина высоты 1.8)
        private val PLAYER_HITBOX_OFFSET = Vec3D(0.0, 0.9, 0.0)
    }

    override fun onTick() {
        physicsSpace.beginTick()
        val delta = 50f / 1000f

        // 1. keepAlive + создание тел
        family.forEach { entity ->
            val comp = entity[PhysicsComponent]
            val pos = entity.getOrNull(PositionComponent)?.position ?: Vec3D.ZERO
            physicsSpace.keepAlive(comp.physicsId)
            physicsSpace.getOrCreateBody(comp.physicsId, comp.physContextIDs) {
                when (comp.bodyInfo) {
                    is PhysicsComponent.BodyInfo.Cube -> physicsSpace.addCube(pos, Vec3D.ONE)
                    is PhysicsComponent.BodyInfo.Player -> physicsSpace.addPlayer(pos + PLAYER_HITBOX_OFFSET)
                }
            }
        }

        // 2. Устанавливаем velocity хитбокса к игроку ДО physics update
        family.forEach { entity ->
            val comp = entity[PhysicsComponent]
            if (comp.bodyInfo !is PhysicsComponent.BodyInfo.Player) return@forEach
            val playerPos = entity.getOrNull(PositionComponent)?.position ?: return@forEach
            val targetPos = playerPos + PLAYER_HITBOX_OFFSET
            val body = physicsSpace.getOrCreateBody(comp.physicsId, comp.physContextIDs) {
                physicsSpace.addPlayer(targetPos)
            }

            // Синхронизируем контексты хитбокса с актуальными контекстами игрока
            val currentContexts = entity.getOrNull(ViewableComponent)?.viewContextIDs
            if (currentContexts != null) {
                physicsSpace.updateBodyContexts(body, currentContexts)
            }

            val hitboxPos = body.getTransform().translation
            val diff = targetPos - hitboxPos

            // Если игрок телепортировался — телепортируем хитбокс вместе с ним
            if (diff.length() > 2.0) {
                body.teleport(targetPos)
                return@forEach
            }

            val pullVelocity = diff * (1.0 / delta)
            body.setLinearVelocity(pullVelocity)
            body.setAngularVelocity(Vec3D.ZERO)
        }

        // 3. TerrainGenerator + physics update
        val activeBodies = family.mapNotNull { entity ->
            val comp = entity[PhysicsComponent]
            if (comp.physContextIDs.isEmpty()) return@mapNotNull null
            val body = physicsSpace.getOrCreateBody(comp.physicsId, comp.physContextIDs) {
                when (comp.bodyInfo) {
                    is PhysicsComponent.BodyInfo.Cube -> physicsSpace.addCube(Vec3D.ZERO, Vec3D.ONE)
                    is PhysicsComponent.BodyInfo.Player -> physicsSpace.addPlayer(Vec3D.ZERO)
                }
            }
            val contextIDs = entity.getOrNull(ViewableComponent)?.viewContextIDs ?: comp.physContextIDs
            ActiveBodyInfo(body.getWorldBounds(), body.getLinearVelocity(), contextIDs)
        }

        val layers = serverWorld.getLayersByContext()
            .flatMap { (contextID, entries) ->
                val dt = serverWorld.dimensionType ?: return@flatMap emptyList()
                entries.map { LayerWithContext(it, setOf(contextID), dt) }
            }
        terrainGenerator.step(delta, activeBodies, layers)
        physicsSpace.update(delta)

        // 4. ПОСЛЕ update — механика точки встречи
        family.forEach { entity ->
            val comp = entity[PhysicsComponent]
            if (comp.bodyInfo !is PhysicsComponent.BodyInfo.Player) return@forEach
            val playerPos = entity.getOrNull(PositionComponent)?.position ?: return@forEach
            val targetPos = playerPos + PLAYER_HITBOX_OFFSET
            val body = physicsSpace.getOrCreateBody(comp.physicsId, comp.physContextIDs) {
                physicsSpace.addPlayer(targetPos)
            }
            val hitboxPos = body.getTransform().translation
            val diff = targetPos - hitboxPos

            if (diff.length() > 0.05) {
                // Хитбокс не смог догнать игрока — препятствие на пути
                val meetingPoint = hitboxPos + diff * 0.5

                // Тянем игрока к точке встречи (переводим обратно в координаты ног)
                val pushToPlayer = (meetingPoint - PLAYER_HITBOX_OFFSET) - playerPos
                entity.getOrNull(PlayerComponent)?.uuid?.let { uuid ->
                    outputProvider.setVelocity(uuid, pushToPlayer * 20.0)
                }

                // Тянем хитбокс к точке встречи
                val pushToHitbox = meetingPoint - hitboxPos
                body.setLinearVelocity(pushToHitbox * (1.0 / delta))
            }

            // Обновляем визуализацию хитбокса
            val uuid = entity.getOrNull(PlayerComponent)?.uuid
            if (uuid != null) {
                val playerContexts = entity.getOrNull(ViewableComponent)?.viewContextIDs
                world.family { all(HitboxVisualizationComponent) }.forEach { visEntity ->
                    if (visEntity[HitboxVisualizationComponent].ownerUuid == uuid) {
                        visEntity[PositionComponent].position = hitboxPos
                        if (playerContexts != null) {
                            visEntity.getOrNull(ViewableComponent)?.viewContextIDs = playerContexts
                        }
                    }
                }
            }
        }

        // 5. Sync Cube transforms → ECS
        family.forEach { onTickEntity(it) }

        physicsSpace.endTick()
    }

    override fun onTickEntity(entity: EcsEntity) {
        val comp = entity[PhysicsComponent]
        if (comp.bodyInfo !is PhysicsComponent.BodyInfo.Cube) return
        val body = physicsSpace.getOrCreateBody(comp.physicsId, comp.physContextIDs) {
            physicsSpace.addCube(Vec3D.ZERO, Vec3D.ONE)
        }
        entity.configure {
            val transform = body.getTransform()
            it.getOrNull(PositionComponent)?.position = transform.translation
            it.getOrNull(CubeModelComponent)?.transform = transform.copy(translation = Vec3D.ZERO)
        }
    }

    object Config : EcsSystemConfig {
        override fun create(scope: InstanceScope) =
            PhysicsSystem(scope.physicsSpace, scope.terrainGenerator, scope.serverWorld, scope.outputProvider)
    }
}
