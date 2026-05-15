package ru.cherryngine.impl.demo.components

import com.github.quillraven.fleks.ComponentType
import ru.cherryngine.engine.ecs.EcsComponent

class CreateCubeToolComponent : EcsComponent<CreateCubeToolComponent> {
    override fun type() = CreateCubeToolComponent

    companion object : ComponentType<CreateCubeToolComponent>()
}

class CreateSlabToolComponent : EcsComponent<CreateSlabToolComponent> {
    override fun type() = CreateSlabToolComponent

    companion object : ComponentType<CreateSlabToolComponent>()
}

class RemoveToolComponent : EcsComponent<RemoveToolComponent> {
    override fun type() = RemoveToolComponent

    companion object : ComponentType<RemoveToolComponent>()
}

class GrabToolComponent : EcsComponent<GrabToolComponent> {
    override fun type() = GrabToolComponent

    companion object : ComponentType<GrabToolComponent>()
}

class SpawnCarToolComponent : EcsComponent<SpawnCarToolComponent> {
    override fun type() = SpawnCarToolComponent

    companion object : ComponentType<SpawnCarToolComponent>()
}

class InteractToolComponent : EcsComponent<InteractToolComponent> {
    override fun type() = InteractToolComponent

    companion object : ComponentType<InteractToolComponent>()
}
