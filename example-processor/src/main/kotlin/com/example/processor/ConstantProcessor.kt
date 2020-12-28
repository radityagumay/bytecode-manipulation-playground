package com.example.processor

import com.example.annotation.Constant
import com.google.auto.service.AutoService
import com.squareup.kotlinpoet.*
import java.io.File
import javax.annotation.processing.AbstractProcessor
import javax.annotation.processing.ProcessingEnvironment
import javax.annotation.processing.Processor
import javax.annotation.processing.RoundEnvironment
import javax.lang.model.SourceVersion
import javax.lang.model.element.TypeElement
import javax.tools.Diagnostic

@AutoService(Processor::class)
class ConstantProcessor : AbstractProcessor() {

    override fun getSupportedSourceVersion(): SourceVersion {
        return SourceVersion.latestSupported()
    }

    override fun getSupportedAnnotationTypes(): MutableSet<String> {
        return mutableSetOf(Constant::class.java.name)
    }

    override fun process(annotations: MutableSet<out TypeElement>?, roundEnv: RoundEnvironment?): Boolean {
        if (roundEnv == null) {
            processingEnv.noteMessage { "RoundEnvironment is null hence skip process." }
            return false
        }

        if (annotations == null || annotations.isEmpty()) {
            processingEnv.noteMessage { "TypeElements is null or empty hence skip process." }
            return false
        }

        val elements = roundEnv.getElementsAnnotatedWith(Constant::class.java)
        if (elements.isEmpty()) {
            processingEnv.errorMessage { "Not able to find elements which annotated with ${Constant::class.java.name}" }
            return false
        }

        val generatedSourceRoot = processingEnv.options[KAPT_KOTLIN_GENERATED] ?: run {
            processingEnv.errorMessage { "Can't find source kotlin generated directory" }
            return false
        }

        processingEnv.noteMessage { "Generating ${Constant::class.java.name} size ; ${annotations.size}" }

        val packageName = "com.example"
        var fileName = ""
        var objectBuilder: TypeSpec.Builder? = null

        for (element in elements) {
            fileName = "${element.simpleName}"
            val annotated = element.getAnnotation(Constant::class.java)
            val propName = annotated.propName
            val propValue = annotated.propValue
            val propertyBuilder = PropertySpec.builder(
                name = propName,
                type = ClassName("kotlin", "String"),
                modifiers = arrayOf(KModifier.FINAL, KModifier.CONST)
            ).mutable(false).initializer("\"$propValue\"")

            objectBuilder = TypeSpec.objectBuilder(fileName)
                .apply {
                    addProperty(propertyBuilder.build())
                }
        }

        val file = FileSpec.builder(packageName, fileName)
            .addType(objectBuilder!!.build())
            .build()
        file.writeTo(File(generatedSourceRoot))

        return true
    }

    private fun ProcessingEnvironment.noteMessage(message: () -> String) {
        this.messager.printMessage(Diagnostic.Kind.NOTE, "${message()}\r")
    }

    private fun ProcessingEnvironment.errorMessage(message: () -> String) {
        this.messager.printMessage(Diagnostic.Kind.ERROR, "${message()}\r")
    }

    companion object {
        const val KAPT_KOTLIN_GENERATED = "kapt.kotlin.generated"
    }

}