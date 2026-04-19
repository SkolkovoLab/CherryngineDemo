package ru.cherryngine.impl.demo

import kotlinx.coroutines.channels.Channel
import java.util.UUID

class InstanceJoinChannel(val channel: Channel<UUID>)
class InstanceLeaveChannel(val channel: Channel<UUID>)
