package com.larastudios.chambrier

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class ChambrierApplication

fun main(args: Array<String>) {
    runApplication<ChambrierApplication>(*args)
}
