package com.example

import com.example.annotation.Builder
import com.example.annotation.Constant

fun main() {
    println(DoWork.Hello)

    AnimalBuilder
        .habitat(Habitat.AIR)
        .name("bird")
        .build().also(::println)
}

@Constant(propName = "Hello", propValue = "world")
fun doWork() { }

@Builder
data class Animal(
    val name: String,
    val habitat: Habitat
)

enum class Habitat {
    AIR,
    WATER,
    SAND
}
