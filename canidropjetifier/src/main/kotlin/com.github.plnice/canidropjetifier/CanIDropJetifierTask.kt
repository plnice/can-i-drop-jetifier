package com.github.plnice.canidropjetifier

import org.gradle.api.artifacts.Configuration
import org.gradle.api.artifacts.ResolvedDependency
import org.gradle.api.tasks.TaskAction

import com.github.plnice.canidropjetifier.BlamedDependency.ChildDependency
import com.github.plnice.canidropjetifier.BlamedDependency.FirstLevelDependency
import org.gradle.api.GradleException
import org.gradle.api.Project
import java.util.*

class CanIDropJetifierTask : AllOpenTask() {

    companion object {
        private val OLD_MODULES_PREFIXES = listOf("android.arch", "com.android.support")
    }

    var verbose: Boolean = false
    var analyzeOnlyAndroidModules = true
    lateinit var configurationRegex: String

    private val reporter by lazy { TextCanIDropJetifierReporter(verbose) }

    init {
        description = "Checks whether there are any dependencies using support library instead of AndroidX artifacts."
        group = "Help"

        outputs.upToDateWhen { false }
    }

    @TaskAction
    fun canIDropJetifier() {
        if (project.property("android.enableJetifier") == "true") {
            throw GradleException(
                "To work correctly, this task needs to be run with Jetifier turned off:" +
                        " ./gradlew -Pandroid.enableJetifier=false canIDropJetifier"
            )
        } else {
            project
                .allprojects
                .filter { it.shouldAnalyze() }
                .forEach { subproject ->
                    subproject
                        .configurations
                        .filter { it.shouldAnalyze() }
                        .map { it.getBlamedDependencies() }
                        .flatten()
                        .distinct()
                        .let {
                            reporter.report(subproject, it)
                        }
                }
        }
    }

    private fun Project.shouldAnalyze(): Boolean = with(project.plugins) {
        return if (analyzeOnlyAndroidModules) {
            hasPlugin("com.android.application") || hasPlugin("com.android.library")
        } else true
    }

    private fun Configuration.shouldAnalyze(): Boolean {
        return configurationRegex.toRegex() matches name
    }

    private fun Configuration.getBlamedDependencies(): Iterable<BlamedDependency> {
        val blamedDependencies = mutableSetOf<BlamedDependency>()
        try {
            if (isCanBeResolved) {
                resolvedConfiguration
                    .firstLevelModuleDependencies
                    .forEach { firstLevelDependency ->
                        if (firstLevelDependency.isOldArtifact()) {
                            blamedDependencies.add(FirstLevelDependency(firstLevelDependency.name))
                        } else {
                            blamedDependencies.traverseAndAddChildren(firstLevelDependency)
                        }
                    }
            }
        } catch (ignored: Throwable) {
        }
        return blamedDependencies
    }

    private data class QueueElement(val parents: List<String>, val children: Iterable<ResolvedDependency>)

    private fun MutableSet<BlamedDependency>.traverseAndAddChildren(firstLevelDependency: ResolvedDependency) {
        val queue: Queue<QueueElement> = LinkedList()

        queue.offer(QueueElement(listOf(firstLevelDependency.name), firstLevelDependency.children))

        while (queue.isNotEmpty()) {
            val (parents, children) = queue.poll()
            children.forEach { child ->
                if (child.isOldArtifact()) {
                    add(ChildDependency(name = child.name, parents = parents))
                } else {
                    queue.offer(QueueElement(parents + child.name, child.children))
                }
            }
        }
    }

    private fun ResolvedDependency.isOldArtifact(): Boolean {
        return OLD_MODULES_PREFIXES.any { moduleGroup.startsWith(it) }
    }
}
