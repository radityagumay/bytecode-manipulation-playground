package com.example

import com.example.annotation.Constant

fun main() {
    println(DoWork.Hello)
}

@Constant(propName = "Hello", propValue = "world")
fun doWork() {}