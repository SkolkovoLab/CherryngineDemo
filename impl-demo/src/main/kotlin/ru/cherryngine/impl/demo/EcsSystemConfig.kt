package ru.cherryngine.impl.demo

import com.github.quillraven.fleks.IntervalSystem
import ru.cherryngine.engine.core.instance.Instance

interface EcsSystemConfig {
    fun create(instance: Instance): IntervalSystem
}
