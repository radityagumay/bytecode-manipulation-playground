package com.example

import com.example.annotation.Builder
import com.example.annotation.MyConstant
import com.example.processor.MyGeneratedConstant

fun main() {
    println("hello ${MyGeneratedConstant.hello}")
}

@MyConstant(propName = "hello", propValue = "world")
fun doSomething() {
    val number = 1
}

@MyConstant(propName = "number", propValue = "world")
fun doWork() { }


@Builder
data class Foo(
    val message: String
)