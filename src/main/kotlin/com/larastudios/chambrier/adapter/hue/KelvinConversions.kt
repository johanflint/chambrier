package com.larastudios.chambrier.adapter.hue

import com.larastudios.chambrier.app.domain.div

// See https://en.wikipedia.org/wiki/Mired
fun mirekToKelvin(mirek: Int) = 1_000_000 / mirek

fun kelvinToMirek(kelvin: Number) = 1_000_000 / kelvin
