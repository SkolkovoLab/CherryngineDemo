package ru.cherryngine.impl.demo

import kotlinx.coroutines.channels.Channel
import ru.cherryngine.engine.core.player.Player
import java.util.*

class InstanceJoinChannel(val channel: Channel<UUID>)
class InstanceLeaveChannel(val channel: Channel<Player>)
