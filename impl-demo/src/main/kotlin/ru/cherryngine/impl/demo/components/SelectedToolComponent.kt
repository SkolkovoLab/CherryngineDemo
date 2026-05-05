package ru.cherryngine.impl.demo.components

import com.github.quillraven.fleks.ComponentType
import ru.cherryngine.engine.ecs.EcsComponent

enum class Tool(val displayName: String) {
    CREATE_CUBE("Create Cube"),
    CREATE_SLAB("Create Slab"),
    REMOVE_NEAREST("Remove Nearest"),
}

data class SelectedToolComponent(
    var tool: Tool = Tool.CREATE_CUBE,
) : EcsComponent<SelectedToolComponent> {
    override fun type() = SelectedToolComponent

    companion object : ComponentType<SelectedToolComponent>()
}
