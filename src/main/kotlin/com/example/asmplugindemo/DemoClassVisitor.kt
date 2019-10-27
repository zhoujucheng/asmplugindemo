package com.example.asmplugindemo

import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.MethodVisitor

class DemoClassVisitor(classVisitor: ClassVisitor) : ClassVisitor(ASM_VERSION,classVisitor) {

    override fun visitMethod(
        access: Int,
        name: String,
        descriptor: String,
        signature: String?,
        exceptions: Array<out String>?
    ): MethodVisitor {
        val mv = super.visitMethod(access, name, descriptor, signature, exceptions)
        // 正则匹配并且返回值的类型为void
        if (name.matches(Regex("on.*Click.*")) && descriptor[descriptor.length - 1] == 'V'){
            return DemoMethodVisitor(access, descriptor, mv)
        }
        return mv
    }
}