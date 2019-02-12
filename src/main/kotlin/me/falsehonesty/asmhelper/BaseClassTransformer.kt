package me.falsehonesty.asmhelper

import net.minecraft.launchwrapper.IClassTransformer
import org.apache.logging.log4j.LogManager
import org.objectweb.asm.ClassReader
import org.objectweb.asm.ClassWriter
import org.objectweb.asm.tree.ClassNode

abstract class BaseClassTransformer : IClassTransformer {
    private val logger = LogManager.getLogger("AsmHelper")
    var calledTransformers = false

    /**
     * This is where you would place all of your asm helper dsl magic
     *
     */
    abstract fun makeTransformers()

    override fun transform(name: String?, transformedName: String?, basicClass: ByteArray?): ByteArray? {
        if (basicClass == null) return null

        if (!calledTransformers) {
            makeTransformers()
            calledTransformers = true
        }

        AsmHelper.classReplacers[transformedName]?.let { classFile ->
            logger.info("Completely replacing {} with data from {}.", transformedName, classFile)

            return loadClassResource(classFile)
        }

        val writers = AsmHelper.asmWriters
            .filter { it.className == transformedName }
            .ifEmpty { return basicClass }

        val classReader = ClassReader(basicClass)
        val classNode = ClassNode()
        classReader.accept(classNode, ClassReader.EXPAND_FRAMES)

        writers.forEach {
            logger.info("Applying AsmWriter {} to class {}", it, transformedName)

            it.transform(classNode)
        }

        val classWriter = ClassWriter(ClassWriter.COMPUTE_FRAMES or ClassWriter.COMPUTE_MAXS)
        try {
            classNode.accept(classWriter)
        } catch (e: Throwable) {
            logger.error("Exception when transforming {} : {}", transformedName, e.javaClass.simpleName)
            e.printStackTrace()
        }


        return classWriter.toByteArray()
    }

    private fun loadClassResource(name: String): ByteArray {
        return this::class.java.classLoader.getResourceAsStream(name).readBytes()
    }
}