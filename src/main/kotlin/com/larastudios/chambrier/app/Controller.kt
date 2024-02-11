package com.larastudios.chambrier.app

import com.larastudios.chambrier.app.domain.DeviceCommand

interface Controller {
    suspend fun send(commands: List<DeviceCommand>)
}
