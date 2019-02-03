package com.github.plnice.canidropjetifier

import org.gradle.api.Project

import org.gradle.kotlin.dsl.*

class CanIDropJetifierPlugin : AllOpenPlugin<Project> {

    override fun apply(project: Project): Unit = project.run {
        tasks {
            register("canIDropJetifier", CanIDropJetifierTask::class)
        }
    }
}
