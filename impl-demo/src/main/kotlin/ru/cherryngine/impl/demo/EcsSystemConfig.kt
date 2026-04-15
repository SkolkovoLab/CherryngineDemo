package ru.cherryngine.impl.demo

import com.github.quillraven.fleks.IntervalSystem

interface EcsSystemConfig {
    fun create(scope: InstanceScope): IntervalSystem
}
