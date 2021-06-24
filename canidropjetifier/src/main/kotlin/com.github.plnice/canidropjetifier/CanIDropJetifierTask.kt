package com.github.plnice.canidropjetifier

import org.gradle.api.artifacts.Configuration
import org.gradle.api.artifacts.ResolvedDependency
import org.gradle.api.tasks.TaskAction

import com.github.plnice.canidropjetifier.BlamedDependency.ChildDependency
import com.github.plnice.canidropjetifier.BlamedDependency.FirstLevelDependency
import org.gradle.api.GradleException
import org.gradle.api.Project
import org.gradle.api.tasks.Internal
import java.util.*
import java.util.concurrent.ForkJoinPool
import java.util.function.Consumer

class CanIDropJetifierTask : AllOpenTask() {

    companion object {
        private val OLD_MODULES_PREFIXES = listOf("android.arch", "com.android.support")
    }

    @Internal var verbose: Boolean = false
    @Internal var includeModules: Boolean = true
    @Internal var analyzeOnlyAndroidModules = true
    @Internal lateinit var configurationRegex: String
    @Internal var parallelMode = false
    @Internal var parallelModePoolSize: Int? = null

    private val reporter by lazy { TextCanIDropJetifierReporter(verbose, includeModules) }

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
            val subprojectsToAnalyze = project.allprojects.filter { it.shouldAnalyze() }
            when {
                parallelMode -> subprojectsToAnalyze.analyzeInParallel()
                else -> subprojectsToAnalyze.forEach { it.doAnalyze() }
            }
        }
    }

    private fun List<Project>.analyzeInParallel() {
        ForkJoinPool(parallelModePoolSize ?: (Runtime.getRuntime().availableProcessors() - 1)).submit(Runnable {
            parallelStream().forEach(Consumer { subproject ->
                subproject.doAnalyze()
            })
        }).get()
    }

    private fun Project.doAnalyze() {
        configurations
            .filter { it.shouldAnalyze() }
            .map { it.getBlamedDependencies() }
            .flatten()
            .distinct()
            .let {
                reporter.report(this, it)
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
                            blamedDependencies.add(FirstLevelDependency(firstLevelDependency.toDependency()))
                        } else {
                            blamedDependencies.traverseAndAddChildren(firstLevelDependency)
                        }
                    }
            }
        } catch (ignored: Throwable) {
        }
        return blamedDependencies
    }

    private data class QueueElement(val parents: List<Dependency>, val children: Iterable<ResolvedDependency>)

    private fun MutableSet<BlamedDependency>.traverseAndAddChildren(firstLevelDependency: ResolvedDependency) {
        val queue: Queue<QueueElement> = LinkedList()

        queue.offer(QueueElement(listOf(firstLevelDependency.toDependency()), firstLevelDependency.children))

        while (queue.isNotEmpty()) {
            val (parents, children) = queue.poll()
            children.forEach { child ->
                if (child.isOldArtifact()) {
                    add(ChildDependency(dependency = child.toDependency(), parents = parents))
                } else {
                    queue.offer(QueueElement(parents + child.toDependency(), child.children))
                }
            }
        }
    }

    private fun ResolvedDependency.isOldArtifact(): Boolean {
        return OLD_MODULES_PREFIXES.any { moduleGroup.startsWith(it) }
    }

    private fun ResolvedDependency.toDependency() = when {
        configuration.endsWith("RuntimeElements") && moduleGroup == project.rootProject.name ->
            Dependency.Module("$moduleName (module)")
        else -> Dependency.External(name)
    }
}
