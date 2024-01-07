package com.larastudios.chambrier.app.domain

interface Event

data class DiscoveredDevices(val devices: List<Device>) : Event
