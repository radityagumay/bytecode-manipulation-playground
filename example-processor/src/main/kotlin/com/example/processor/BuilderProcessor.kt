package com.example.processor

import com.example.annotation.Builder
import com.google.auto.service.AutoService
import com.squareup.kotlinpoet.*
import java.io.File
import javax.annotation.processing.AbstractProcessor
import javax.annotation.processing.ProcessingEnvironment
import javax.annotation.processing.Processor
import javax.annotation.processing.RoundEnvironment
import javax.lang.model.SourceVersion
import javax.lang.model.element.Element
import javax.lang.model.element.ElementKind
import javax.lang.model.element.TypeElement
import javax.lang.model.type.DeclaredType
import javax.lang.model.type.PrimitiveType
import javax.lang.model.type.TypeMirror
import javax.lang.model.util.ElementFilter.fieldsIn

@AutoService(Processor::class)
class BuilderProcessor : AbstractProcessor() {

    override fun init(processingEnv: ProcessingEnvironment?) {
        super.init(processingEnv)
    }

    override fun getSupportedSourceVersion(): SourceVersion {
        return SourceVersion.latestSupported()
    }

    override fun getSupportedAnnotationTypes(): MutableSet<String> {
        return mutableSetOf(Builder::class.java.name)
    }

    override fun process(annotations: MutableSet<out TypeElement>?, roundEnv: RoundEnvironment?): Boolean {
        if (roundEnv == null) {
            processingEnv.noteMessage { "RoundEnvironment is null hence skip process." }
            return true
        }

        if (annotations == null || annotations.isEmpty()) {
            processingEnv.noteMessage { "TypeElements is null or empty hence skip process." }
            return true
        }

        val elements = roundEnv.getElementsAnnotatedWith(Builder::class.java)
        if (elements.isEmpty()) {
            processingEnv.noteMessage { "Not able to find ${Builder::class.java.name} in RoundEnvironment" }
            return true
        }

        for (element in elements) {
            when (element.kind) {
                ElementKind.CLASS -> writeForClass(element as TypeElement)
                else -> element.noteMessage {
                    """
                        Target assignment is not appropriate for ${element.simpleName}, 
                        please add ${Builder::class.java.name} either on Constructor or Class.
                    """.trimIndent()
                }
            }
        }

        return true
    }

    private fun writeForClass(element: TypeElement) {
        processingEnv.noteMessage { "simpleName : ${element.simpleName}" }
        processingEnv.noteMessage { "kind : ${element.kind}" }
        processingEnv.noteMessage { "nestingKind : ${element.nestingKind}" }
        processingEnv.noteMessage { "interfaces : ${element.interfaces}" }
        processingEnv.noteMessage { "superclass : ${element.superclass}" }
        processingEnv.noteMessage { "qualifiedName : ${element.qualifiedName}" }
        processingEnv.noteMessage { "typeParameters : ${element.typeParameters}" }
        processingEnv.noteMessage { "enclosedElements : ${element.enclosedElements}" }
        processingEnv.noteMessage { "enclosingElement : ${element.enclosingElement}" }
        processingEnv.noteMessage { "modifiers : ${element.modifiers}" }
        processingEnv.noteMessage { "asType : ${element.asType()}" }
        processingEnv.noteMessage { "annotationMirrors : ${element.annotationMirrors}" }

        val packageName = "${element.enclosingElement}"
        val fileName = "${element.simpleName}Builder".capitalize()

        val allMembers = processingEnv.elementUtils.getAllMembers(element)
        val fields = fieldsIn(allMembers)

        processingEnv.noteMessage { "All members for $packageName : $fields" }

        val classBuilder = TypeSpec.objectBuilder(fileName)
        val builderClass = ClassName(packageName, fileName)
        for (field in fields) {
            processingEnv.noteMessage { "$field type : ${field.asType()}" }
            processingEnv.noteMessage { "$field kind : ${field.kind}" }
            processingEnv.noteMessage { "$field modifiers : ${field.modifiers}" }
            processingEnv.noteMessage { "$field constantValue : ${field.constantValue}" }
            processingEnv.noteMessage { "$field enclosedElements : ${field.enclosedElements}" }
            processingEnv.noteMessage { "$field enclosingElement : ${field.enclosingElement}" }

            val propertyName = field.toString()
            val type = ClassName("${element.enclosingElement}", "String")

            // private var name: kotlin.String? = null
            val propertySpec = PropertySpec.builder(propertyName, asKotlinTypeName(field).copy(nullable = true))
                .addModifiers(KModifier.PRIVATE)
                .mutable()
                .initializer("null")

            //  public fun name(name: String): AnimalBuilder = apply { this.name = name }
            val funSpec = FunSpec.builder(propertyName)
                .addParameter(name = propertyName, type = asKotlinTypeName(field).copy(nullable = false))
                .returns(builderClass)
                .addCode("return apply { this.$propertyName = $propertyName }")

            classBuilder.addProperty(propertySpec.build())
            classBuilder.addFunction(funSpec.build())
        }

        // create build function for returning the original type
        val funSpec = FunSpec.builder("build").apply {
            val code = StringBuilder()
            for (field in fields) {
                code.append("requireNotNull($field)").appendLine()
            }
            addCode(code.toString())
        }.returns(ClassName("${element.enclosingElement}", "${element.simpleName}"))
            .apply {
                val code = StringBuilder()
                val iterator = fields.listIterator()
                while (iterator.hasNext()) {
                    val field = iterator.next()
                    code.appendLine()
                    code.append("$FOUR_SPACE$field = $field")
                    if (iterator.hasNext()) {
                        code.append("!!,")
                    } else {
                        code.append("!!")
                    }
                }
                addCode(
                    """
                    |return ${element.simpleName}($code
                    |)
                """.trimMargin()
                )
            }

        classBuilder.addFunction(funSpec.build())

        val generatedSource = processingEnv.options[KAPT_KOTLIN_GENERATED]
        val file = FileSpec.builder(packageName, fileName)
            .addType(classBuilder.build())
            .build()
        file.writeTo(File(generatedSource))
    }

    private fun asKotlinTypeName(element: Element): TypeName {
        return element.asType().asKotlinType()
    }

    private fun TypeMirror.asKotlinType(): TypeName {
        return when (this) {
            is PrimitiveType -> {
                processingEnv.noteMessage { "TypeMirror is PrimitiveType" }
                processingEnv.typeUtils.boxedClass(this).asKotlinClassName()
            }
            is DeclaredType -> {
                processingEnv.noteMessage { "TypeMirror is DeclaredType" }
                this.asTypeElement().asKotlinClassName()
            }
            else -> this.asTypeElement().asKotlinClassName()
        }
    }

    /** Returns the [TypeElement] represented by this [TypeMirror]. */
    private fun TypeMirror.asTypeElement() = processingEnv.typeUtils.asElement(this) as TypeElement

    private fun TypeElement.asKotlinClassName(): ClassName {
        val className = asClassName()
        return try {
            // ensure that java.lang.* and java.util.* etc classes are converted to their kotlin equivalents
            Class.forName(className.canonicalName).kotlin.asClassName()
        } catch (e: ClassNotFoundException) {
            // probably part of the same source tree as the annotated class
            className
        }
    }

    private fun Element.noteMessage(message: () -> String) {
        processingEnv.noteMessage(message)
    }

    companion object {
        const val KAPT_KOTLIN_GENERATED = "kapt.kotlin.generated"
        const val FOUR_SPACE = "    "
    }

}