package com.github.plnice.canidropjetifier

import org.gradle.api.Project

import com.github.plnice.canidropjetifier.BlamedDependency.ChildDependency
import com.github.plnice.canidropjetifier.BlamedDependency.FirstLevelDependency
import kotlin.math.max

interface CanIDropJetifierReporter {
    val verbose: Boolean
    val includeModules: Boolean
    fun report(project: Project, blamedDependencies: List<BlamedDependency>)
}

class TextCanIDropJetifierReporter(
    override val verbose: Boolean,
    override val includeModules: Boolean
) : CanIDropJetifierReporter {

    override fun report(project: Project, blamedDependencies: List<BlamedDependency>) {
        println("=".repeat(max(40, 8 + project.name.length)))
        println("Project ${project.name}")
        println("=".repeat(max(40, 8 + project.name.length)))
        println("")

        when (blamedDependencies.size) {
            0 -> {
                println("No dependencies on old artifacts! Safe to drop Jetifier.")
                println("")
            }
            else -> {
                val moduleDependencies = blamedDependencies
                    .filterIsInstance<ChildDependency>()
                    .groupBy { it.parents.first() }
                    .filter { (parent, _) -> parent is Dependency.Module }

                val firstLevelDependencies = blamedDependencies
                    .filterIsInstance<FirstLevelDependency>()

                val externalDependencies = blamedDependencies
                    .filterIsInstance<ChildDependency>()
                    .groupBy { it.parents.first() }
                    .filter { (parent, _) -> parent is Dependency.External }

                if (includeModules && moduleDependencies.isNotEmpty()) {
                    println("Cannot drop Jetifier due to following module dependencies:")
                    println("")

                    moduleDependencies.forEach { it.print() }
                }

                if (firstLevelDependencies.isNotEmpty() || externalDependencies.isNotEmpty()) {
                    println("Cannot drop Jetifier due to following external dependencies:")
                    println("")

                    firstLevelDependencies.forEach { it.print() }
                    externalDependencies.forEach { it.print() }
                }
            }
        }
    }

    private fun FirstLevelDependency.print() {
        println("* ${dependency.name}")
        println("")
    }

    private fun Map.Entry<Dependency, List<ChildDependency>>.print() {
        val (parent, dependencies) = this
        println("* ${parent.name}")
        if (verbose) {
            dependencies.forEach {
                val parentsWithoutFirst = it.parents.subList(1, it.parents.size)
                parentsWithoutFirst.forEachIndexed { index: Int, parent: Dependency ->
                    println(" ".repeat(index + 2) + "\\-- ${parent.name}")
                }
                println(" ".repeat(parentsWithoutFirst.size + 2) + "\\-- ${it.dependency.name}")
            }
        }
        println("")
    }
}
