package com.example.processor

import com.example.annotation.MyConstant
import com.google.auto.service.AutoService
import com.squareup.kotlinpoet.*
import java.io.File
import javax.annotation.processing.AbstractProcessor
import javax.annotation.processing.Processor
import javax.annotation.processing.RoundEnvironment
import javax.lang.model.SourceVersion
import javax.lang.model.element.Element
import javax.lang.model.element.TypeElement

/**
 * Generate for [ConstantProcessor]
 */
@AutoService(Processor::class)
class ConstantProcessor : AbstractProcessor() {
    companion object {
        const val KAPT_KOTLIN_GENERATED_OPTION_NAME = "kapt.kotlin.generated"
    }

    override fun getSupportedAnnotationTypes(): MutableSet<String> {
        return mutableSetOf(MyConstant::class.java.name)
    }

    override fun getSupportedSourceVersion(): SourceVersion {
        return SourceVersion.latestSupported()
    }

    override fun process(annotations: MutableSet<out TypeElement>?, roundEnv: RoundEnvironment?): Boolean {
        if (roundEnv == null) {
            processingEnv.noteMessage { "RoundEnvironment is null hence skip the process" }
            return false
        }

        if (annotations == null || annotations.isEmpty()) {
            processingEnv.noteMessage { "TypeElements is null or empty hence skip the process" }
            return false
        }

        val elements: MutableSet<out Element> = roundEnv.getElementsAnnotatedWith(MyConstant::class.java)
        processingEnv.noteMessage { "$elements" }

        val packageName = "com.example.processor"
        val fileName = "MyGeneratedConstant"
        val objectBuilder = TypeSpec.objectBuilder(fileName)

        for (element in elements) {
            processingEnv.noteMessage { "$element" }
            processingEnv.noteMessage { "asType :${element.asType()}" }
            processingEnv.noteMessage { "kind :${element.kind}" }
            processingEnv.noteMessage { "modifier :${element.modifiers}" }
            processingEnv.noteMessage { "name :${element.simpleName}" }
            processingEnv.noteMessage { "enclosingElement :${element.enclosingElement}" }
            processingEnv.noteMessage { "enclosedElements :${element.enclosedElements}" }
            processingEnv.noteMessage { "annotationMirrors :${element.annotationMirrors}" }

            val annotation = element.getAnnotation(MyConstant::class.java)
            val propName = annotation.propName
            val propValue = annotation.propValue
            val propBuilder = PropertySpec.builder(
                name = propName,
                type = ClassName("kotlin", "String"),
                modifiers = arrayOf(KModifier.FINAL, KModifier.CONST)
            ).mutable(false).initializer("\"$propValue\"")
            objectBuilder.addProperty(propBuilder.build())
        }

        val file = FileSpec.builder(packageName, fileName)
            .addType(objectBuilder.build())
            .build()
        val generatedDirectory = processingEnv.options[KAPT_KOTLIN_GENERATED_OPTION_NAME]
        file.writeTo(File(generatedDirectory))
        return false
    }

}