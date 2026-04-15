package ru.cherryngine.impl.demo

data class InstancePrefab(
    val id: String,
    val platformIds: List<String>,
    val worlds: List<WorldLayerConfig>,
    val systems: List<EcsSystemConfig>,
)

data class WorldLayerConfig(
    val name: String,
    val priority: Int,
    val mutable: Boolean = false,
)
