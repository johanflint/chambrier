package com.larastudios.chambrier.app

import com.larastudios.chambrier.app.domain.DeviceCommand

interface Controller {
    fun send(commands: List<DeviceCommand>)
}
