package com.example

import com.example.annotation.Builder
import com.example.annotation.Constant
import com.example.person.AddressBuilder
import com.example.person.SalaryBuilder
import com.example.person.address.CoordinateBuilder
import com.example.person.salary.BonusBuilder

fun main() {
    println(DoWork.Hello)

    AnimalBuilder
        .habitat(Habitat.AIR)
        .name("bird")
        .build().also(::println)

    PersonBuilder
        .name("raditya")
        .salary(
            SalaryBuilder
                .basic(12.0)
                .bonus(
                    BonusBuilder
                        .monthly(13.0)
                        .build()
                )
                .tax(10.0)
                .currency(Person.Salary.Currency.IDR)
                .build()
        )
        .address(
            AddressBuilder
                .city("Jakarta")
                .zipCode(1010)
                .country("Indonesia")
                .fullAddress("Monas")
                .coordinate(
                    CoordinateBuilder
                        .latitude(-6.1753924)
                        .longitude(106.8249641)
                        .build()
                )
                .build()
        )
        .build()
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
