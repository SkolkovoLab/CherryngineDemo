package ru.cherryngine.impl.demo.bedrock.input

import ru.cherryngine.engine.core.instance.InstanceSingleton
import ru.cherryngine.engine.core.player.Player
import ru.cherryngine.impl.demo.input.MovementSnapshot
import ru.cherryngine.impl.demo.input.MovementSource
import ru.cherryngine.platform.minecraft.bedrock.BedrockPlayer

@InstanceSingleton(platform = "bedrock")
class BedrockMovementSource : MovementSource<BedrockPlayer> {
    override fun canHandle(target: Player): Boolean = target is BedrockPlayer

    // Bedrock не шлёт onGround отдельным флагом — оставляем false, потребители это знают.
    override fun pollMovement(player: BedrockPlayer): MovementSnapshot =
        MovementSnapshot(player.clientPosition, player.clientYawPitch, false)
}
