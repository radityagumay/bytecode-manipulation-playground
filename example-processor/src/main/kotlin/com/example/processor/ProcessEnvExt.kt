package com.example.processor

import javax.annotation.processing.ProcessingEnvironment
import javax.tools.Diagnostic

internal fun ProcessingEnvironment.noteMessage(message: () -> String) {
    this.messager.printMessage(Diagnostic.Kind.NOTE, "${message()}\r")
}

internal fun ProcessingEnvironment.errorMessage(message: () -> String) {
    this.messager.printMessage(Diagnostic.Kind.ERROR, "${message()}\r")
}