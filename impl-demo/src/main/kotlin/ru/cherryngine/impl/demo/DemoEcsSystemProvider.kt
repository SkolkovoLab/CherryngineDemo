package ru.cherryngine.impl.demo

interface DemoEcsSystemProvider {
    fun addEarlySystems(cfg: SystemConfiguration) {}
    fun addLateSystems(cfg: SystemConfiguration) {}
}

typealias SystemConfiguration = com.github.quillraven.fleks.SystemConfiguration
