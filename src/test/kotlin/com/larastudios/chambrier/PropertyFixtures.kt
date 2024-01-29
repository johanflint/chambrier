package com.larastudios.chambrier

import com.larastudios.chambrier.app.domain.*
import com.larastudios.chambrier.app.domain.Unit

val booleanProperty = BooleanProperty("on", PropertyType.On, readonly = false, value = true)
val numberProperty = NumberProperty("brightness", PropertyType.Brightness, readonly = false, Unit.Percentage, 42, null, null)
val gamut = Gamut(
    red = CartesianCoordinate(0.1, 0.2),
    green = CartesianCoordinate(0.3, 0.4),
    blue = CartesianCoordinate(0.5, 0.6),
)
val colorProperty = ColorProperty("color", PropertyType.Color, readonly = false, xy = CartesianCoordinate(0.01, 0.05), gamut = gamut)
val enumProperty = EnumProperty("button", PropertyType.Button, readonly = false, values = HueButtonState.entries.toList(), value = HueButtonState.InitialPress)
