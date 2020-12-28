package com.example

import com.example.annotation.Constant

fun main() {
    println(ConstantGenerated.Hello)
}

@Constant(propName = "Hello", propValue = "world")
fun doWork() {}