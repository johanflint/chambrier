package com.larastudios.chambrier.adapter.hue

import com.larastudios.chambrier.app.domain.EnumPropertyChanged
import com.larastudios.chambrier.app.domain.Event

fun mapChangedButtonProperty(property: ChangedButtonProperty): Sequence<Event> = sequence {
    // Cannot use the button name as the metadata is not sent, use the property id instead
    yield(EnumPropertyChanged(property.owner.rid, property.id, property.button.buttonReport.event.toHueButtonState()))
}
