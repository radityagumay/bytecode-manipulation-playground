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
            processingEnv.noteMessage { "RoundEnvironment is null hence skip the process." }
            return true // exit process
        }

        if (annotations == null || annotations.isEmpty()) {
            processingEnv.noteMessage { "TypeElements is null or empty hence skip the process." }
            return true
        }

        val elements = roundEnv.getElementsAnnotatedWith(Constant::class.java)
        if (elements.isEmpty()) {
            processingEnv.noteMessage { "Not able to find ${Constant::class.java} in RoundEnvironment." }
            return true
        }

        val generatedSource = processingEnv.options[KAPT_KOTLIN_GENERATED] ?: run {
            processingEnv.errorMessage { "Can't find target source." }
            return true
        }

        val packageName = "com.example"
        var fileName = ""

        // create object builder
        var objectBuilder: TypeSpec.Builder? = null

        for (element in elements) {
            val annotated = element.getAnnotation(Constant::class.java)
            val propName = annotated.propName
            val propValue = annotated.propValue
            fileName = "${element.simpleName}".capitalize()

            // crate property
            val propBuilder = PropertySpec.builder(
                name = propName,
                type = ClassName("kotlin", "String"),
                modifiers = arrayOf(KModifier.CONST, KModifier.FINAL)
            ).mutable(false).initializer("\"$propValue\"")

            objectBuilder = TypeSpec.objectBuilder(fileName).apply {
                addProperty(propBuilder.build())
            }
        }

        // create a file
        val file = FileSpec.builder(packageName, fileName)
            .addType(objectBuilder!!.build())
            .build()
        file.writeTo(File(generatedSource))

        return false
    }

    private fun ProcessingEnvironment.noteMessage(message: () -> String) {
        this.messager.printMessage(Diagnostic.Kind.NOTE, message())
    }

    private fun ProcessingEnvironment.errorMessage(message: () -> String) {
        this.messager.printMessage(Diagnostic.Kind.ERROR, message())
    }

    companion object {
        const val KAPT_KOTLIN_GENERATED = "kapt.kotlin.generated"
    }
}