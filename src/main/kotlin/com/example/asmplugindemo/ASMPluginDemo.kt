package com.example.asmplugindemo

import org.gradle.api.Plugin
import org.gradle.api.Project
import com.android.build.gradle.AppExtension

class ASMPluginDemo : Plugin<Project>{
    override fun apply(project: Project) {
        val appExtension = project.extensions.getByType(AppExtension::class.java)
        val demoTransform = DemoTransform()
        appExtension.registerTransform(demoTransform)
    }
}