package com.example.processor

import javax.annotation.processing.ProcessingEnvironment
import javax.tools.Diagnostic

internal fun ProcessingEnvironment.noteMessage(message: () -> String) {
    this.messager.printMessage(Diagnostic.Kind.NOTE, "RADITYAGUMAAAY : ${message()}\r")
}

internal fun ProcessingEnvironment.errorMessage(message: () -> String) {
    this.messager.printMessage(Diagnostic.Kind.ERROR, "RADITYAGUMAAAY : ${message()}\r")
}
