package com.example.annotation

import kotlin.annotation.AnnotationRetention.SOURCE
import kotlin.annotation.AnnotationTarget.*

@Retention(SOURCE)
@Target(CLASS, FIELD, FUNCTION)
annotation class MyConstant(
    val propName: String,
    val propValue: String
)