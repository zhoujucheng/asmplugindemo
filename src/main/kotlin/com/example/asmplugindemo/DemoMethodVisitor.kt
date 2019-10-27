package com.example.asmplugindemo

import org.objectweb.asm.Label
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes
import org.objectweb.asm.Type
import org.objectweb.asm.commons.LocalVariablesSorter

class DemoMethodVisitor(access: Int, descriptor: String, methodVisitor: MethodVisitor) :
    LocalVariablesSorter(ASM_VERSION, access, descriptor, methodVisitor) {

    companion object {
        const val HELPER_CLASS_NAME = "com/example/asmpluginappdemo/ClickHelper"
    }

    override fun visitCode() {
        super.visitCode()

        // 创建一个局部变量
        val elapsedTimeIndex = newLocal(Type.LONG_TYPE)

        val checkClickLabel = Label()
        visitLabel(checkClickLabel)
        visitMethodInsn(Opcodes.INVOKESTATIC, "android/os/SystemClock", "elapsedRealtime", "()J", false)
        // 将栈顶的元素存入到局部变量中
        visitVarInsn(Opcodes.LSTORE, elapsedTimeIndex)
        // 将局部变量压入栈中
        visitVarInsn(Opcodes.LLOAD, elapsedTimeIndex)
        visitFieldInsn(Opcodes.GETSTATIC, HELPER_CLASS_NAME, "Companion", "L$HELPER_CLASS_NAME${'$'}Companion;")
        visitMethodInsn(Opcodes.INVOKEVIRTUAL, "$HELPER_CLASS_NAME${'$'}Companion", "getLastClickTime", "()J", false)
        // 栈顶两个long相减
        visitInsn(Opcodes.LSUB)
        // 将300L压入栈中
        visitLdcInsn(300L)
        visitInsn(Opcodes.LCMP)

        val setLastClickLabel = Label()
        // 如果大于则跳转
        visitJumpInsn(Opcodes.IFGT, setLastClickLabel)

        val returnLabel = Label()
        visitLabel(returnLabel)
        visitInsn(Opcodes.RETURN)

        visitLabel(setLastClickLabel)
        visitFieldInsn(Opcodes.GETSTATIC, HELPER_CLASS_NAME, "Companion", "L$HELPER_CLASS_NAME${'$'}Companion;")
        visitVarInsn(Opcodes.LLOAD, elapsedTimeIndex)
        visitMethodInsn(Opcodes.INVOKEVIRTUAL, "$HELPER_CLASS_NAME${'$'}Companion", "setLastClickTime", "(J)V", false)
    }
}