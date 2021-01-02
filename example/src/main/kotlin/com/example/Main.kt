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
fun doWork() {
}

@Builder
data class Animal(
    val name: String,
    val habitat: Habitat
)

@Builder
data class Person(
    val name: String,
    val address: Address,
    val salary: Salary
) {

    @Builder
    data class Salary(
        val basic: Double,
        val bonus: Bonus,
        val tax: Double,
        val currency: Currency
    ) {

        @Builder
        data class Bonus(
            val monthly: Double
        )

        enum class Currency {
            IDR,
            USD,
            RUPEE,
            EURO
        }
    }

    @Builder
    data class Address(
        val fullAddress: String,
        val city: String,
        val zipCode: Int,
        val country: String,
        val coordinate: Coordinate
    ) {
        @Builder
        data class Coordinate(
            val longitude: Double,
            val latitude: Double
        )
    }


}

enum class Habitat {
    AIR,
    WATER,
    SAND
}
