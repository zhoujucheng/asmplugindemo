package com.example.asmplugindemo

import com.android.build.api.transform.*
import com.android.build.gradle.internal.pipeline.TransformManager
import org.objectweb.asm.ClassReader
import org.objectweb.asm.ClassWriter
import java.io.*

class DemoTransform : Transform() {
    override fun getName() = "ASMPluginDemoTransform"

    override fun getInputTypes(): MutableSet<QualifiedContent.ContentType> {
        return TransformManager.CONTENT_CLASS
    }

    override fun isIncremental(): Boolean {
        return true
    }

    override fun getScopes(): MutableSet<in QualifiedContent.Scope> {
        return TransformManager.SCOPE_FULL_PROJECT
    }

    override fun transform(transformInvocation: TransformInvocation) {

        val outputProvider = transformInvocation.outputProvider
        val isIncremental = transformInvocation.isIncremental

        if (!isIncremental) {
            outputProvider.deleteAll()
        }

        transformInvocation.inputs.forEach { transformInput ->
            transformInput.directoryInputs.forEach {

                val dest = outputProvider.getContentLocation(it.name, it.contentTypes, it.scopes, Format.DIRECTORY)
                val pathLength = it.file.absolutePath.length
                if (isIncremental) {
                    it.changedFiles.forEach { (file, status) ->
                        if (status == Status.CHANGED || status == Status.ADDED) {
                            val destPath = dest.absolutePath + file.absolutePath.substring(pathLength)
                            transformDir(file, File(destPath))
                        } else if (status == Status.REMOVED) {
                            file.deleteRecursively()
                        }
                    }
                } else {
                    transformDir(it.file, dest)
                }

            }

            transformInput.jarInputs.forEach {
                val dest = outputProvider.getContentLocation(it.name, it.contentTypes, it.scopes, Format.JAR)
                if (isIncremental) {
                    if (it.status == Status.CHANGED || it.status == Status.ADDED) {
                        it.file.copyTo(dest, true)
                    } else if (it.status == Status.REMOVED) {
                        dest.delete()
                    }
                } else {
                    it.file.copyTo(dest, true)
                }
            }
        }
    }

    private fun transformDir(src: File, dest: File) {
        if (src.isDirectory) {
            if (!dest.exists()) {
                dest.mkdir()
            }

            src.listFiles()?.forEach {
                val childDestPath = dest.absolutePath + File.separator + it.name
                transformDir(it, File(childDestPath))
            }
            return
        }

        if (!src.name.endsWith(".class")) {
            src.copyTo(dest, true)
            return
        }

        handleClassFile(src, dest)

    }

    private fun handleClassFile(src: File, dest: File) {
        BufferedInputStream(FileInputStream(src)).use { fi ->
            val classReader = ClassReader(fi)
            val classWriter = ClassWriter(classReader, ClassWriter.COMPUTE_MAXS)
            val demoClassVisitor = DemoClassVisitor(classWriter)
            classReader.accept(demoClassVisitor, ClassReader.EXPAND_FRAMES)
            val bytes = classWriter.toByteArray()
            BufferedOutputStream(FileOutputStream(dest)).use {
                it.write(bytes)
            }
        }

    }
}