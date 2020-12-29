package com.example.annotation

import kotlin.annotation.AnnotationRetention.SOURCE
import kotlin.annotation.AnnotationTarget.CLASS
import kotlin.annotation.AnnotationTarget.CONSTRUCTOR

@Retention(SOURCE)
@Target(CLASS, CONSTRUCTOR)
annotation class Builder