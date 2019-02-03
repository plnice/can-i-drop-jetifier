package com.github.plnice.canidropjetifier

import org.gradle.api.artifacts.Configuration
import org.gradle.api.artifacts.ResolvedDependency
import org.gradle.api.tasks.TaskAction

import com.github.plnice.canidropjetifier.BlamedDependency.ChildDependency
import com.github.plnice.canidropjetifier.BlamedDependency.FirstLevelDependency
import org.gradle.api.GradleException

class CanIDropJetifierTask : AllOpenTask() {

    companion object {
        private val OLD_MODULES_PREFIXES = listOf("android.arch", "com.android.support")
    }

    private val reporter = TextCanIDropJetifierReporter()

    init {
        description = "Checks whether there are any dependencies using support library instead of AndroidX artifacts."
        group = "Help"

        outputs.upToDateWhen { false }
    }

    @TaskAction
    fun canIDropJetifier() {
        if (project.property("android.enableJetifier") == "true") {
            throw GradleException("To work correctly, this task needs to be run with Jetifier turned off:" +
                    " ./gradlew -Pandroid.enableJetifier=false canIDropJetifier")
        } else {
            project.allprojects.forEach { subproject ->
                subproject
                    .configurations
                    .map { it.getBlamedDependencies() }
                    .flatten()
                    .distinct()
                    .let {
                        reporter.report(subproject, it)
                    }
            }
        }
    }

    private fun Configuration.getBlamedDependencies(): Iterable<BlamedDependency> {
        val blamedDependencies = mutableSetOf<BlamedDependency>()
        if (isCanBeResolved) {
            resolvedConfiguration
                .firstLevelModuleDependencies
                .forEach { firstLevelDependency ->
                    if (firstLevelDependency.isOldArtifact()) {
                        blamedDependencies.add(FirstLevelDependency(firstLevelDependency.name))
                    } else {
                        blamedDependencies.traverseAndAddChildren(
                            listOf(firstLevelDependency.name),
                            firstLevelDependency.children
                        )
                    }
                }
        }
        return blamedDependencies
    }

    private fun MutableSet<BlamedDependency>.traverseAndAddChildren(
        parents: List<String>,
        children: Iterable<ResolvedDependency>
    ) {
        children.forEach { child ->
            if (child.isOldArtifact()) {
                add(ChildDependency(name = child.name, parents = parents))
            } else {
                traverseAndAddChildren(parents + child.name, child.children)
            }
        }
    }

    private fun ResolvedDependency.isOldArtifact(): Boolean {
        return OLD_MODULES_PREFIXES.any { moduleGroup.startsWith(it) }
    }
}
